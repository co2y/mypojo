package erwins.util.text;

import org.apache.commons.io.IOUtils;

/** 
 * 쓸모없어 보이지만, 가끔 쓴다
 * MessageBuilder가 더 나은듯..  여기저기서 많이 쓰길래 일단 놔둠 
 *  */
public class StringAppender{

    private final StringBuilder the = new StringBuilder();

    @Override
	public String toString() {
		return the.toString();
	}
    
    public StringAppender append(Object append) {
    	the.append(append);
    	return this;
    }

    public StringAppender appendLine(Object append) {
    	the.append(append);
    	the.append(IOUtils.LINE_SEPARATOR);
        return this;
    }
    
    public StringAppender appendWord(Object append) {
    	the.append(append);
    	the.append(" ");
    	return this;
    }
    public StringAppender appendComma(Object append) {
    	the.append(append);
    	the.append(",");
    	return this;
    }
}