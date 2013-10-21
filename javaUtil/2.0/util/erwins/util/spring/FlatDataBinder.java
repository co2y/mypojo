package erwins.util.spring;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import lombok.Data;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import erwins.util.lib.ReflectionUtil;

/** 스프링 DataBinder 를 사용하는 컬렉션 바인더. 로우별 변환 / 역변환을 제공
 *  1. CSV / XLS 등의 Flat 데이터를 VO로 변환 & 예외처리
 *  2. DB or 로직에서 가져온 VO를 Flat 데이터로 변환 (JSTL은 상관없으니 JS용 GRID or Flex 등에서는 Flat화 해주어야 하는 경우가 많다.)
 * 이러한 바인더는 비용이 높기 때문에 VO변환비용조차 아까운 대량 처리에는 적합하지 않다.
 * 입력/반환되는 array size는 maxArraySize보다 크거나 같아야 한다.
 *  */
@Data
public class FlatDataBinder implements InitializingBean{

	private Class<?> clazz;
	private Integer maxArraySize;
	private List<LineMetadata> lineMetadatas;
	private Validator validator;
	private ConversionService conversionService;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		Preconditions.checkNotNull(clazz, "clazz is required");
		Preconditions.checkNotNull(lineMetadatas, "lineMetadatas is required");
		Preconditions.checkState(lineMetadatas.size() > 0);
		Collections.sort(lineMetadatas);
		if(maxArraySize==null) maxArraySize = lineMetadatas.get(lineMetadatas.size()-1).getIndex() + 1;
		if(validator==null){
			LocalValidatorFactoryBean tempValidator = new LocalValidatorFactoryBean(); 
			tempValidator.afterPropertiesSet();
			validator = tempValidator;
		}
		if(conversionService==null) conversionService = new DefaultConversionService();
	}
	
	@SuppressWarnings("unchecked")
	public <T> T bind(String[] array,int lineNumber) throws BindException{
		Preconditions.checkState(array.length >= maxArraySize, "too small array : " + maxArraySize);
		MutablePropertyValues kv = new MutablePropertyValues();
		for(LineMetadata each :lineMetadatas){
			String value = array[each.getIndex()]; 
			kv.add(each.getFieldName(), value);
		}
		DataBinder  binder = new DataBinder(ReflectionUtil.newInstance(clazz),clazz.getSimpleName() + " - " + lineNumber);
		binder.setConversionService(conversionService);
		binder.setValidator(validator);
		binder.bind(kv);
		binder.validate();
		BindingResult result = binder.getBindingResult();
		binder.close();
		return (T) result.getTarget();
	}
	
	/** 프로퍼티 에디터 적용은 나중에 만들자 */
	public <T> String[] toStringArray(T vo){
		String[] result = new String[maxArraySize];
		ExpressionParser parser = new SpelExpressionParser();
		for(LineMetadata each :lineMetadatas){
			String stringValue = findStringValue(parser,each,vo);
			result[each.getIndex()] = stringValue;
		}
		return result;
	}
	
	/** 프로퍼티 에디터 적용은 나중에 만들자. map의 경우 인덱스는 무시한다. */
	public <T> Map<String,String> toMap(T vo){
		Map<String,String> result = Maps.newConcurrentMap();
		ExpressionParser parser = new SpelExpressionParser();
		for(LineMetadata each :lineMetadatas){
			String stringValue = findStringValue(parser,each,vo);
			result.put(each.getFieldName(), stringValue);
		}
		return result;
	}
	
	private <T> String findStringValue(ExpressionParser parser,LineMetadata each,T vo){
		Expression exp = parser.parseExpression(each.getFieldName());
		Object value = exp.getValue(vo);
		if(value==null) return null;
		if(conversionService.canConvert(value.getClass(), String.class)){
			return conversionService.convert(value, String.class);
		}
		return value.toString();
	}
	
}
