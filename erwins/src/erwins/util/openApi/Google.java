
package erwins.util.openApi;

import java.text.MessageFormat;
import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.ecs.html.Script;
import org.apache.ecs.wml.Img;

import erwins.util.lib.CharSets;
import erwins.util.lib.Strings;
import erwins.util.tools.SystemInfo;
import erwins.util.vender.apache.RESTful;


/**
 * GoogleWeather를 래핑한다.
 * @author erwins(my.pojo@gmail.com)
 */
public class Google{
    
    private static final String GOOGLE_MAP_URL = "http://maps.google.com/maps";
    private static final String GOOGLE_CHART_URL = "http://chart.apis.google.com/chart";
    private static final String GOOGLE_WEATHER_URL = "http://www.google.co.kr/ig/api";
    private static final String GOOGLE_LOCAL_MAP_KEY = "ABQIAAAAbnojiUetDB2JPnAr7msMxhT2yXp_ZAY8_ufC3CFXhHIE1NvwkxSSUTahXG1TgB4AL-LL3DXVwdGvXg";
    private static final String GOOGLE_SERVER_MAP_KEY = "ABQIAAAAbnojiUetDB2JPnAr7msMxhSJ9_8WeQAaXDHeBdc7dwvnuUSw7BQOdEokh8Ck27mfaJWo36FaoQVUpQ";
    
    /**
     * 지정된 서버IP가 아니면 LOCAL Key를 , 지정된 서버이면 서버 key를 리턴한다.
     */
    public static String getGoogleMapKey(){
        return SystemInfo.isServer() ? GOOGLE_SERVER_MAP_KEY : GOOGLE_LOCAL_MAP_KEY;
    }

    /**
     * 주소 변경시 일괄 적용 위함 
     */
    public static String getGoogleScript(){
        Script js = new Script();
        js.setSrc(GOOGLE_MAP_URL+"?file=api&amp;v=2&amp;key="+getGoogleMapKey());
        js.setType("text/JavaScript");
        return js.toString();
    }
    
    // ===========================================================================================
    //                                   API
    // ===========================================================================================    
    
    private static enum ChartMode{
        p3;
        public String get(){
            return "cht=" + this.toString();
        }
    }
    
    /**
     * 구글 차트 정보를 img태그로 리턴한다.
     */
    public static String getChart(Map<Object,Object> map,int width,int height){
        String size = MessageFormat.format("chs={0}x{1}", width,height);
        List<String> label = new ArrayList<String>();
        List<String> value = new ArrayList<String>();
        
        for(Entry<Object,Object> entry : map.entrySet()){
            label.add(entry.getKey().toString() +"("+ entry.getValue().toString()+")");
            value.add(entry.getValue().toString());
        }
        String a = "chl=" + Strings.joinTemp(label,"|");
        String b = "chd=t:" + Strings.joinTemp(value,",");
        
        Img img = new Img(GOOGLE_CHART_URL+"?"+size+"&"+a+"&"+b+"&"+ChartMode.p3.get());
        return img.toString();
    }
    
    /** 구글 날씨를 XML로 리턴한다. */
    public static String getWeatherXml(String city){
        String cityName = Strings.nvl(city,"seoul");
        NameValuePair[] parmas = new NameValuePair[]{new NameValuePair("weather",cityName)};
        return new RESTful().query(parmas).build(GOOGLE_WEATHER_URL).asString(CharSets.EUC_KR);
    }
 

}
