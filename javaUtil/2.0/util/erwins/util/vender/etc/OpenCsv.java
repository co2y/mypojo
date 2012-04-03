package erwins.util.vender.etc;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.map.ListOrderedMap;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * 간단 래핑한다. 스트림 기능을 지원하지 않는듯 하다. ㅅㅂ
 * CSVReader reader = new CSVReader(new FileReader("yourfile.csv"), '\t', '\'', 2); 처럼 옵션 조절 가능
 */
public abstract class OpenCsv{
    
	/** key를 첫번째 열에 담는다.
	 * Date의 경우 숫자형으로 담지만, 읽을땨는 역변환이 안된다. 알아서 처리할것.
	 * 적절한? 컨버터가 필요해 보인다. */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void writeMap(File file,List<Map> list){
		if(list.size()==0) return;
		List<String[]> entries = new ArrayList<String[]>();
		List colList = new ArrayList();
		Map sample = list.get(0);
		for(Object o : sample.entrySet()){
			Entry e = (Entry)o;
			if(e.getValue() instanceof Collection) continue;
			colList.add(e.getKey());
		}
		String[] colums = (String[]) colList.toArray(new String[colList.size()]);
		entries.add(colums);
		
		for(Map each : list){
			String[] values = new String[colums.length]; 
			for(int i=0;i<colums.length;i++){
				Object value = each.get(colums[i]);
				if(value instanceof Collection) continue;
				
				if(value instanceof Date) values[i] = String.valueOf(((Date)value).getTime());
				else values[i] = value == null? "" : value.toString() ;
			}
			entries.add(values);
		}
		writeAll(file,entries);
    } 
	
	
    public static void writeAll(File file,List<String[]> entries){
    	CSVWriter writer = null;
    	try {
            writer = new CSVWriter(new FileWriter(file));
            writer.writeAll(entries);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally{
			try {
				if (writer != null) writer.close();
			} catch (Exception e2) {
				
			}
		}
    }
    
    public static List<String[]> readAll(File file){
    	CSVReader reader = null;
    	try {
			reader = new CSVReader(new FileReader(file));
			return reader.readAll();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}finally{
			try {
				if (reader != null) reader.close();
			} catch (Exception e2) {
				
			}
		}
    }
    
    @SuppressWarnings("unchecked")
	public static List<Map<String,String>> readAsMap(File file){
    	List<Map<String,String>> result = new ArrayList<Map<String,String>>();
    	List<String[]> datas = readAll(file);
    	if(datas.size() <= 1) return result;
    	String[] header = datas.get(0);
    	for (int i = 1; i < datas.size(); i++) {
			String[] data = datas.get(i);
    		Map<String,String> map = new ListOrderedMap();
    		for (int j = 0; j < header.length; j++) {
    			map.put(header[j], data[j]);	
    		}
    		result.add(map);
		}
    	return result;
    }
    
    /**
     * 	private CSVWriter writer;
	
	public OpenCsv(String fileName){
		try {
			this.writer = new CSVWriter(new FileWriter(fileName));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public OpenCsv(File file){
		try {
			this.writer = new CSVWriter(new FileWriter(file));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
     */
    
    
}