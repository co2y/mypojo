package erwins.util.spring.batch.tool;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.sql.DataSource;

import lombok.Data;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.mapping.PassThroughLineMapper;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import erwins.util.jdbc.ToStringArrayRowMapper;
import erwins.util.nio.ThreadUtil;
import erwins.util.root.Incompleted;
import erwins.util.root.exception.PropagatedRuntimeException;
import erwins.util.spring.SpringUtil;
import erwins.util.spring.batch.component.CsvItemMultiMapWriter;
import erwins.util.spring.batch.component.CsvItemReader;
import erwins.util.spring.batch.component.CsvItemWriter;
import erwins.util.spring.batch.component.CsvItemReader.PassThroughCsvMapper;
import erwins.util.spring.batch.component.CsvItemWriter.PassThroughCsvAggregator;
import erwins.util.text.StringUtil;

/** 스프링 배치를 간단히 로컬에서 돌려볼 수 있는 테스트기 */
@Data
public class SpringBatchMock<T>{
	
	private ItemReader<T> itemReader;
	private ItemWriter<T> itemWriter;
	private ItemProcessor<T,T> itemProcessor;
	private int commitInterval = 1000;
	private ExecutionContext executionContext = new ExecutionContext();
	private JobParameters jobParameter = new JobParametersBuilder().addDate("date", new Date()).toJobParameters();
	private JobInstance jobInstance = new JobInstance(0L,"tempJob");
	private JobExecution jobExecution = new JobExecution(jobInstance,jobParameter);
	
	/** 리더/라이터를 조합해서 실행한다. */
	public ExecutionContext run(){
		SpringBatchUtil.openIfAble(itemReader,executionContext);
		SpringBatchUtil.openIfAble(itemWriter,executionContext);
		int processSkipCount = 0;
		try{
			List<T> list = Lists.newArrayList();
			while(true){
				T item =  itemReader.read();
				if(item==null) break;
				if(itemProcessor!=null ) {
					//신규추가!.  프로세서 결과가 null이면 스킵한다.
					T newItem = itemProcessor.process(item);
					if(newItem==null){
						processSkipCount++;
						continue;
					}
					item = newItem;
				}
				list.add(item);
				if(list.size() >= commitInterval){
					itemWriter.write(list);
					SpringBatchUtil.updateIfAble(itemReader,executionContext);
					SpringBatchUtil.updateIfAble(itemWriter,executionContext);
					list = Lists.newArrayList();
				}
			}
			itemWriter.write(list);
			SpringBatchUtil.updateIfAble(itemReader,executionContext);
			SpringBatchUtil.updateIfAble(itemWriter,executionContext);
		}catch(Throwable e){
			Throwables.propagate(e);
		}finally{
			SpringBatchUtil.closeIfAble(itemReader);
			SpringBatchUtil.closeIfAble(itemWriter);
		}
		executionContext.putInt("processSkipCount", processSkipCount);
		return executionContext;
	}
	
	/** DB작업을 할 경우 반드시 ThreadLocal당 커넥션을 유지해야 한다. */
	@Incompleted
	public ExecutionContext run(int corePoolSize){
		SpringBatchUtil.openIfAble(itemReader,executionContext);
		SpringBatchUtil.openIfAble(itemWriter,executionContext);
		
		StepExecution sec = new StepExecution("sec",jobExecution);
		
		SpringBatchUtil.beforeStepIfAble(itemReader,sec);
		SpringBatchUtil.beforeStepIfAble(itemProcessor,sec);
		SpringBatchUtil.beforeStepIfAble(itemWriter,sec);
		
        ThreadPoolTaskExecutor ex = ThreadUtil.defaultPool(corePoolSize);
        
        Callable<Long> callable = new Callable<Long>() {
			@Override
			public Long call() throws Exception {
				List<T> list = Lists.newArrayList();
				long countSum = 0;
				try {
					while(true){
						T item =  itemReader.read();
						if(item==null) break;
						if(itemProcessor!=null ) item = itemProcessor.process(item);
						list.add(item);
						if(list.size() >= commitInterval){
							itemWriter.write(list);
							SpringBatchUtil.updateIfAble(itemReader,executionContext);
							SpringBatchUtil.updateIfAble(itemWriter,executionContext);
							countSum += list.size();
							list = Lists.newArrayList();
						}
					}
					itemWriter.write(list);
					SpringBatchUtil.updateIfAble(itemReader,executionContext);
					SpringBatchUtil.updateIfAble(itemWriter,executionContext);
					countSum += list.size();
				} catch (Exception e) {
					throw new PropagatedRuntimeException(e);
				}
				return countSum;
			}
		}; 
        
		try{
			List<Future<Long>> fs = Lists.newArrayList();
			for(int i=0;i<corePoolSize;i++){
				fs.add(ex.submit(callable));
			}
			Long sum = ThreadUtil.sum(fs);
			executionContext.putLong("totalThreadCountSum", sum);
			
			SpringBatchUtil.afterStepIfAble(itemReader,sec);
			SpringBatchUtil.afterStepIfAble(itemProcessor,sec);
			SpringBatchUtil.afterStepIfAble(itemWriter,sec);
			
		} catch (Exception e) {
			throw new PropagatedRuntimeException(e);
		}finally{
			SpringBatchUtil.closeIfAble(itemReader);
			SpringBatchUtil.closeIfAble(itemWriter);
		}
		
		return executionContext;
	}
	
	/** CSV를 읽어서 특정 라인값의 정수 합계를 리턴하는 간단 도우미 (주로 ID를 합계내는 검증에 사용) 
	 * ex) final File dir = new File("C:/DATA/download");
		System.out.println(SpringBatchMock.readCsvAndSum(CharEncodeUtil.C_MS949, new File(dir,"AD.csv"), 0)); */
	public static long readCsvAndSum(Charset encoding,File file,int columnIndex){
		return readCsv(encoding,file,new CsvColumnSumProcessor(columnIndex));
	}
	
	/** 숫자형만 분리해서 파싱한다. */
	public static class CsvColumnSumProcessor implements ItemProcessor<String[],Long>{
		private final int column;
		private CsvColumnSumProcessor(int column) {
			this.column = column;
		}
		@Override
		public Long process(String[] arg0) throws Exception {
			try {
				String value = arg0[column];
				if(Strings.isNullOrEmpty(value)) return 0L;
				return Long.parseLong(arg0[column]);
			} catch (Exception e) {
				String value = CharMatcher.DIGIT.retainFrom(arg0[column]);
				if(Strings.isNullOrEmpty(value)) return 0L;
				return Long.parseLong(value);
			}
		}
	}
	
	/** CSV를 읽어서 숫자형 처리를 하는 간단 도우미. (처리할게 없으면 리턴하지 않아도 된다)  */
	public static long readCsv(Charset encoding,File in,ItemProcessor<String[],Long> processor){
		CsvItemReader<String[]> itemReader = new CsvItemReader<String[]>();
		itemReader.setResource(new FileSystemResource(in));
		itemReader.setCsvMapper(new PassThroughCsvMapper());
		itemReader.setEncoding(encoding.name());
		
		long sum = 0;
		try{
			itemReader.open(new ExecutionContext());
			while(true){
				String[] line = itemReader.read();
				if(line==null) break;
				Long result = processor.process(line);
				if(result!=null) sum += result;
			}	
		}catch(Exception e){
			throw new PropagatedRuntimeException(e);
		}finally{
			itemReader.close();	
		}
		return sum;
	}
	
	/** SQL로 CSV를 생성하는 간단 샘플
	 * ==> select로 리팩토링 하자
	 * ex)
	 *  DataSource dataSource = SpringBatchUtil.createDataSource(OracleDriver.class, "jdbc:oracle:thin:@182.162.16.51:1521:ORCL", "id", "pass");
		File out = new File("C:/DATA/download/AD.csv");
		System.out.println(SpringBatchMock.sqlToCsv(dataSource, out, "SELECT * FROM AD WHERE REG_TIME > ?", "20131100000000000"));  */
	public static ExecutionContext sqlToCsv(DataSource dataSource,File out,String sql,Object ... param) throws Exception{
		ToStringArrayRowMapper mapper = new ToStringArrayRowMapper();
		
		JdbcCursorItemReader<String[]> itemReader = new JdbcCursorItemReader<String[]>();
		itemReader.setDataSource(dataSource);
		itemReader.setRowMapper(mapper);
		itemReader.setFetchSize(10000); 
		itemReader.setSql(sql);
		itemReader.setPreparedStatementSetter(SpringUtil.buildPreparedStatement(param));
		itemReader.afterPropertiesSet();
		
		CsvItemWriter<String[]> itemWriter = new CsvItemWriter<String[]>();
    	itemWriter.setCsvAggregator(new PassThroughCsvAggregator());
    	itemWriter.setResource(new FileSystemResource(out));
    	itemWriter.setCsvHeaderCallback(mapper);
    	itemWriter.setCsvRead(true); //주의
    	
		SpringBatchMock<String[]> mock = new SpringBatchMock<String[]>();
		mock.setCommitInterval(10000);
		mock.setItemReader(itemReader);
		mock.setItemWriter(itemWriter);
		mock.run();
		return mock.getExecutionContext();
	}
	
	/**  대용량 플랫 파일을 합치는 간단 샘플 */
	public static ExecutionContext fileMerge(Resource[] resources,Charset encoding,File out) throws Exception{
		FlatFileItemReader<String> delegate = new FlatFileItemReader<String>();
		delegate.setLineMapper(new PassThroughLineMapper());
		delegate.setEncoding(encoding.name());
		delegate.afterPropertiesSet();
		
		MultiResourceItemReader<String> itemReader = new MultiResourceItemReader<String>();
		itemReader.setResources(resources);
		itemReader.setDelegate(delegate);
		
		FlatFileItemWriter<String> itemWriter = new FlatFileItemWriter<String>();
        itemWriter.setResource(new FileSystemResource(out));
        itemWriter.setLineAggregator(new  PassThroughLineAggregator<String>());
        itemWriter.setEncoding(encoding.name());
        
        SpringBatchMock<String> mock = new SpringBatchMock<String>();
		mock.setCommitInterval(10000);
		mock.setItemReader(itemReader);
		mock.setItemWriter(itemWriter);
		return mock.run();
	}
	
	/** 인메모리만큼 쪼개서 나누기 */
	public static ExecutionContext fileSplit(final File largeFile,final Charset encoding,int commitInterval) throws Exception{
		FlatFileItemReader<String> itemReader = new FlatFileItemReader<String>();
		itemReader.setLineMapper(new PassThroughLineMapper());
		itemReader.setEncoding(encoding.name());
		itemReader.setResource(new FileSystemResource(largeFile));
		itemReader.afterPropertiesSet();
		
		ItemWriter<String> itemWriter = new ItemWriter<String>() {
			int count = 0;
			@Override
			public void write(List<? extends String> items) throws Exception {
				String fileExt =  Files.getFileExtension(largeFile.getName());
				String name = Files.getNameWithoutExtension(largeFile.getName());
				String countName = StringUtil.leftPad(count++, 4);
				File newFile = new File(largeFile.getParentFile(),name + "_" + countName + "." + fileExt);
				
				FlatFileItemWriter<String> flatWriter = new FlatFileItemWriter<String>();
		        flatWriter.setResource(new FileSystemResource(newFile));
		        flatWriter.setLineAggregator(new  PassThroughLineAggregator<String>());
		        flatWriter.setEncoding(encoding.name());
		        flatWriter.afterPropertiesSet();
		        flatWriter.open(new ExecutionContext());
		        flatWriter.write(items);
		        flatWriter.close();
			}
		};
        
        SpringBatchMock<String> mock = new SpringBatchMock<String>();
		mock.setCommitInterval(commitInterval);
		mock.setItemReader(itemReader);
		mock.setItemWriter(itemWriter);
		return mock.run();
	}
	
	/** CSV 파일을  대용량 파일 조건에 따라 나누기 샘풀 */
	public static ExecutionContext fileSplit(Charset encoding,File out,Converter<String[], String> idConverter,Converter<String, String> IdToFileNameConverter){
		CsvItemReader<String[]> itemReader = new CsvItemReader<String[]>();
		itemReader.setResource(new FileSystemResource(out));
		itemReader.setCsvMapper(new PassThroughCsvMapper());
		itemReader.setEncoding(encoding.name());
		itemReader.setLinesToSkip(1);
		
		CsvItemWriter<String[]> delegate = new CsvItemWriter<String[]>();
		delegate.setCsvAggregator(new PassThroughCsvAggregator());
		delegate.setEncoding(encoding.name());
		delegate.setAppend(true);
		delegate.setCsvRead(true);
		
		CsvItemMultiMapWriter<String,String[]> itemWriter = new CsvItemMultiMapWriter<String,String[]>();
		itemWriter.setDelegate(delegate);
		itemWriter.setMaxFileLineCount(10000);
		itemWriter.setIdConverter(idConverter);
		itemWriter.setIdToFileNameConverter(IdToFileNameConverter);
        SpringBatchMock<String[]> mock = new SpringBatchMock<String[]>();
		mock.setCommitInterval(10000);
		mock.setItemReader(itemReader);
		mock.setItemWriter(itemWriter);
		return mock.run();
	}
	
    /**  대용량 CSV를 처리. (라이터는 아마 Groovy일것임)
     * ex) csvBatchProcess(SpringUtil.antToResources(AntResourceType.file, "C:/DATA/download/utf8_hqlResult_*.csv"),CharEncodeUtil.C_UTF_8,new ItemWriter<String[]>() {
			@Override
			public void write(List<? extends String[]> items) throws Exception {
				운영_백업.insertList("REQ_RAW", items);
			}
		}); */
	public static ExecutionContext csvBatchProcess(Resource[] resources,Charset encoding,ItemWriter<String[]> itemWriter,Character separator){
		CsvItemReader<String[]> delegate = new CsvItemReader<String[]>();
		delegate.setCsvMapper(new PassThroughCsvMapper());
		delegate.setEncoding(encoding.name());
		if(separator!=null) delegate.setSeparator(separator); //  \t 인 경우도 있음
		
		
		MultiResourceItemReader<String[]> itemReader = new MultiResourceItemReader<String[]>();
		itemReader.setResources(resources);
		itemReader.setDelegate(delegate);
        
        SpringBatchMock<String[]> mock = new SpringBatchMock<String[]>();
		mock.setCommitInterval(1000); //Groovy에서 만들어놓은거 기본 주기
		mock.setItemReader(itemReader);
		mock.setItemWriter(itemWriter);
		return mock.run();
	}
	
	/** 완성된 리더/라이터를 연결만 해준다. */
	public static <T> ExecutionContext csvReadWrite(ItemReader<T> itemReader,ItemWriter<T> itemWriter,int commitInterval){
		SpringBatchMock<T> mock = new SpringBatchMock<T>();
		mock.setCommitInterval(commitInterval);
		mock.setItemReader(itemReader);
		mock.setItemWriter(itemWriter);
		return mock.run();
	}
	
	/** 간이 SQL 실행기 */
	public static <T> List<T> select(DataSource dataSource,RowMapper<T> rowMapper,String sql,Object ... param) throws Exception{
		final List<T> result = Lists.newArrayList();
		ItemWriter<T> itemWriter = new ItemWriter<T>() {
			@Override
			public void write(List<? extends T> arg0) throws Exception {
				result.addAll(arg0);
			}
		};
		select(dataSource,rowMapper,itemWriter,1000,sql,param);
		return result;
	}
	
	/**  itemWriter에 콜백을 넣어서 사용하자. */
	public static <T> ExecutionContext select(DataSource dataSource,RowMapper<T> rowMapper,ItemWriter<T> itemWriter,int fetchSize,String sql,Object ... param) throws Exception{
		JdbcCursorItemReader<T> itemReader = new JdbcCursorItemReader<T>();
		itemReader.setDataSource(dataSource);
		itemReader.setRowMapper(rowMapper);
		itemReader.setFetchSize(fetchSize); 
		itemReader.setSql(sql);
		itemReader.setPreparedStatementSetter(SpringUtil.buildPreparedStatement(param));
		itemReader.afterPropertiesSet();
		
		SpringBatchMock<T> mock = new SpringBatchMock<T>();
		mock.setCommitInterval(fetchSize);
		mock.setItemReader(itemReader);
		mock.setItemWriter(itemWriter);
		return mock.run();
	}
	
	public static <T> List<T> toList(ItemReader<T> itemReader) {
		
		SpringBatchMock<T> mock = new SpringBatchMock<T>();
    	final List<T> list = Lists.newArrayList();
		mock.setCommitInterval(1000);
		mock.setItemReader(itemReader);
		mock.setItemWriter(new ItemWriter<T>() {
			@Override
			public void write(List<? extends T> items) throws Exception {
				list.addAll(items);
			}
		});
		mock.run();
		return list;
	}
	
	/**  
	 * value와 일치하는 라인 카운트 가져오기
	 * Flat만 해당된다.
	 *  */
	public static int findLineCount(File file,String findValue) throws Exception{
		FlatFileItemReader<String> itemReader = new FlatFileItemReader<String>();
    	itemReader.setLineMapper(new PassThroughLineMapper());
    	itemReader.setEncoding("UTF-8");
    	itemReader.setResource(new FileSystemResource(file));
    	itemReader.afterPropertiesSet();
    	
    	itemReader.open(new ExecutionContext());
    	int i=0;
    	while(true){
    		String line = itemReader.read();
    		i++;
    		if(line==null) break;
    		if(line.equals(findValue)) return i;
    	}
		return -1;
	}
	
	
}
