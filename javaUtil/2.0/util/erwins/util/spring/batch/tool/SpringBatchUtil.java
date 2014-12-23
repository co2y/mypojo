package erwins.util.spring.batch.tool;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.sql.Driver;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.internal.StringMap;

import erwins.util.lib.ReflectionUtil;
import erwins.util.root.exception.PropagatedRuntimeException;


/**
 * 간단 유틸 or 예제 모음
 */
public abstract class SpringBatchUtil{
	
	public static final ExitStatus COMPLETED = new ExitStatus("COMPLETED");
	private static final Gson GSON = new Gson();
	
	/** ex) oracle.jdbc.OracleDriver */
	public static <T extends Driver> BasicDataSource createDataSource(Class<T> driver,String url,String id,String password){
		BasicDataSource from = new BasicDataSource();
    	from.setUsername(id);
    	from.setPassword(password);
    	from.setDriverClassName(driver.getName());
    	from.setUrl(url);
		return from;
	}
	
	public static void openIfAble(Object itemSreeam,ExecutionContext executionContext){
		if(itemSreeam instanceof ItemStream) ((ItemStream)itemSreeam).open(executionContext);
	}
	public static void updateIfAble(Object itemSreeam,ExecutionContext executionContext){
		if(itemSreeam instanceof ItemStream) ((ItemStream)itemSreeam).update(executionContext);
	}
	public static void closeIfAble(Object itemSreeam){
		if(itemSreeam instanceof ItemStream) ((ItemStream)itemSreeam).close();
	}

    /** Step의 ExitCode접두어가 하나라도 일치한다면 Job종료코드를 변경한다. */
    public static void changeJobExitCodeByStepPrefix(JobExecution je,String prefix,ExitStatus status) {
        Collection<StepExecution> steps = je.getStepExecutions();
        for(StepExecution stepEx : steps){
            String code = stepEx.getExitStatus().getExitCode();
            if(code.startsWith(prefix)){
                je.setExitStatus(status);
                break;
            }
        }
    }
    
    /** 스프링 배치의 shortContext를 map으로 변경해준다. (GSON 버전)  */
    @SuppressWarnings("unchecked")
    public static Map<String,Object> shortContextToGMap(String shortContext){
    	Map<String,Object> result = Maps.newLinkedHashMap();
    	if(Strings.isNullOrEmpty(shortContext)) return result;
    	
		Map<String,Object> json =  GSON.fromJson(shortContext, Map.class);
		
		Object mapObject = json.get("map");
		if(mapObject==null) return result;
		if(!(mapObject instanceof StringMap)) return result;
		
		StringMap<String> map = (StringMap<String>) mapObject;
		Object entry =  map.get("entry");
		if(entry instanceof List){
			List<Object> list = (List<Object>) entry;
			for(Object each : list) gsonObjectToValue(result,each);
		}else{
			gsonObjectToValue(result,entry);
		}
		return result;
    }
    
    /** 스프링배치 컨텍스트를 GSON으로 읽은 값을, 일반 value로 변경해준다 **/
    @SuppressWarnings("unchecked")
    private static void gsonObjectToValue(Map<String,Object> result, Object each) {
    	Preconditions.checkState(each instanceof StringMap,each.getClass().getSimpleName());
    	
    	StringMap<Object> map = (StringMap<Object>) each;
    	int size = map.size();
    	Preconditions.checkState(size==1 || size == 2);
    	
    	if(size==1){
    		List<String> array = (List<String>)map.get("string");
    		Preconditions.checkState(array.size() == 2);
    		result.put(array.get(0), array.get(1));
    	}else{
    		String key = (String) map.get("string");
    		Object value = null;
    		if(map.containsKey("int"))  {
    			value = map.get("int");
    			if(value instanceof Double)  value = ((Double)value).intValue();
    		}
    		else if(map.containsKey("long")){
    			value = map.get("long"); 
    			if(value instanceof Double)  value = ((Double)value).longValue();
    		}
    		else if(map.containsKey("double")){
    			value = map.get("double");//요건 확인안됨
    		}
            result.put(key,  value);
    	}
    }
    
    /** 정상 완료된 상태인가?
     * @AfterStep 등에서 사용한다
     * ExitCode 가 동일하면 equals도 동일하다.
     *  */
    public static boolean isCompleted (StepExecution se){
        return COMPLETED.equals(se.getExitStatus());
    }
    
	/**
	 * 대용량 벌크입력시 사용된다. 해당 예외일 경우 슬립 했다가 다시 시도한다. 
	 * ex)DuplicateKeyException */
	public static int retry(Runnable run,int currentTry,int limitTry,int sleepSec,Class<?>  clazz){
		try {
			run.run();
		} catch (RuntimeException e) {
			boolean able = clazz.isInstance(e);
			if(!able){
				Throwable cause = e.getCause();
				if(cause!=null) able = clazz.isInstance(e);
			}
			if(!able) throw e;
			currentTry++;
			if(currentTry > limitTry) throw new IllegalStateException("최대 try횟수를 넘었습니다. limitTry : " + limitTry,e);
			retry(run,currentTry,limitTry,sleepSec,clazz);
		}
		return currentTry;
	}
	
	/** @see retry */
	public static int retry(Runnable run,int limitTry,int sleepSec,Class<?>  clazz){
		return retry(run,0,limitTry,sleepSec,clazz);
	}
	
	/** 앞위에 간단한 페이징 구문을...? */
	public static String appendOraclePagingSql(String sql,String option){
		return "";
	}
	
	/** 비포가 있다면 실행해둔다... 별걸 다 만들게 되넹 ㅠㅠ */
	public static void beforeStepIfAble(Object batchObject,StepExecution executionContext){
		stepIfAble(batchObject, executionContext,BeforeStep.class);
	}
	public static void afterStepIfAble(Object batchObject,StepExecution executionContext){
		stepIfAble(batchObject, executionContext,AfterStep.class);
	}

	private static void stepIfAble(Object batchObject,StepExecution executionContext,Class<? extends Annotation> clazz) {
		if(batchObject==null) return;
		
		List<Method> beforeSteps = ReflectionUtil.findMethod(batchObject.getClass(),clazz);
		if(beforeSteps.size()==0) return;
		if(beforeSteps.size() > 1) throw new IllegalArgumentException(clazz.getSimpleName()+ " 는 1개만 허용됩니다");
		
		Method beforeStep = beforeSteps.get(0);
		Class<?>[] parameterTypes = beforeStep.getParameterTypes();
		if(parameterTypes==null || parameterTypes.length != 1) throw new IllegalArgumentException(clazz.getSimpleName()+ " 적합한 파라메터가 아닙니다");
		if(parameterTypes[0] != executionContext.getClass()) throw new IllegalArgumentException(clazz.getSimpleName()+ " 적합한 파라메터가 아닙니다");
		
		try {
			beforeStep.invoke(batchObject, executionContext);
		} catch (Exception e) {
			throw new PropagatedRuntimeException(e);
		}
	}
	
    
}
