
package erwins.util.lib;

import java.math.BigDecimal;

import org.junit.Test;

import erwins.util.exception.Val;
import erwins.util.root.StringCallback;
import erwins.util.tools.Mapp;

public class RegExTest {

    @Test
    public void tag() throws Exception {
        String org = "영감님<tag>정말로</tag>좋아요";
        String result = RegEx.TAG_TEXT.replace(org,"@@");
        Val.isEquals(result,"영감님<tag>@@</tag>좋아요");
        
        String result2 = RegEx.TAG.replace(org,"@@");
        Val.isEquals(result2,"영감님@@정말로@@좋아요");
    }
    
    @Test
    public void simpleMatch() throws Exception {
        Val.isTrue(RegEx.simpleMatch("/base/**/123/*", "/base/qwe/ss/123/save.do"));
    }
    
    @Test
    public void filterText() throws Exception {
        String text = "영감님 fuck oh shit! 이 멍멍이야";
        Val.isEquals(RegEx.filterText(text, "fuck","shit","멍멍이"),"영감님 f*** oh s***! 이 멍**야");
    }
    
    @Test
    public void map() throws Exception {
        Mapp map = new Mapp();
        map.put("submitDate","2009/57/87");
        map.put("submitDate2",new BigDecimal("123.145"));
        map.put("apache","2009/57/87");
        Val.isEquals(map.get("submitDate"),"2009/57/87");
        map.replace("date",RegEx.NOT_NUMERIC,"");
        Val.isEquals(map.get("submitDate"),"20095787");
        Val.isEquals(map.get("apache"),"2009/57/87");
        Val.isEquals(map.get("submitDate2"),new BigDecimal("123.145"));
    }
    
    @Test
    public void comment() throws Exception {
        //qwe
        /* q
         asdasdawe */
        /** java doc  */
        RegEx.COMMENT.file("D:/PROJECT/workspace/erwins/src/erwins/util/lib/RegExTest.java",new StringCallback(){
            public void process(String line){
                Val.isTrue(Strings.isMatch(line, "//","/*"));
            }
        });
    }

    @Test
    public void emial() {
        Val.isTrue(RegEx.E_MAIL.isFullMatch("my.pojo@gmail.co.kr"));
        Val.isTrue(RegEx.E_MAIL.isFullMatch("my.po.jo@gmail.co.kr"));
        Val.isTrue(RegEx.E_MAIL.isFullMatch("my.po.jo@gmail.co.kr.com"));
        Val.isTrue(!RegEx.E_MAIL.isFullMatch("my.pojo.@gmail.co.kr"));
        Val.isTrue(!RegEx.E_MAIL.isFullMatch("my.po.jo@gmail.co.kr.com."));
        Val.isTrue(!RegEx.E_MAIL.isFullMatch("my.pojo@gmail..co.kr"));
    }


    
}
