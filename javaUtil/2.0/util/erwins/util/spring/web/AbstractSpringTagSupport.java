package erwins.util.spring.web;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import erwins.util.text.StringUtil;


/**
 * 스프링 bean을 가져오는 코드 추가
 * getBean()의 소스를 하드코딩하는게 싫어서 만듬.
 * @author sin 
 */
@SuppressWarnings("serial")
public class AbstractSpringTagSupport extends TagSupport {
	
	@SuppressWarnings("unchecked")
	protected <T> T getBean(String beanName){
		WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(pageContext.getServletContext());
    	T bean = (T) ctx.getBean(beanName);
    	return bean;
	}
	
	/** 예외를 래핑하기 위한 간편 메소드  */
	protected void write(String text) throws JspException{
		try {
			pageContext.getOut().write(text);
		} catch (IOException e) {
			throw new JspException(e);
		}
	}
	
	protected <T> T getBean(Class<T> clazz){
    	return getBean(StringUtil.uncapitalize(clazz.getSimpleName()));
	}
	
}
