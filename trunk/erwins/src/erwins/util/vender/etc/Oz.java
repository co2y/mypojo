package erwins.util.vender.etc;

import java.util.*;

import javax.servlet.http.HttpServletResponse;

import org.apache.ecs.xml.XML;
import org.apache.ecs.xml.XMLDocument;
import org.apache.log4j.Logger;

import erwins.util.lib.Encoders;
import erwins.util.lib.Strings;

/**
 * 국산 레포팅 툴 오즈의 XML문을 생성합니다.
 */
public class Oz extends ResponseEditor{
    
    private Logger log = Logger.getLogger(this.getClass());
    
    private static final String DATA_SET_NODE_NAME = "sql_retireflow";    
    private static final String RECORD_SET_NODE_NAME =  "flow";

    public Oz(HttpServletResponse response){
        super(response);
    }
    
    private HashMap<String,String> fieldInfos = new HashMap<String,String>();
    private List<HashMap<String,String>> datas = new ArrayList<HashMap<String,String>>();
    private HashMap<String,String> thisList;
    
    public void addNewData(){
        thisList = new HashMap<String,String>();
        datas.add(thisList);
    }
    
    public void addData(String name,String value){
        value = Strings.nvl(value);
        value = Encoders.escapeXml(value);
        fieldInfos.put(name,"string"); //대부분 String만을 사용함으로 추후 확장성만 남기고 나머지는 생략한다. 
        thisList.put(name,value);
    }
    
    /**
     * DATA_SET_NODE_NAME = "sql_retireflow";   
     * RECORD_SET_NODE_NAME =  "flow";
     */
    public void out(){
        
        //response.setContentType("text/xml; charset=utf-8");
        //PrintWriter out = getWriter();            
        
        //str.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        XML xmlBody = new XML(DATA_SET_NODE_NAME);
        
        //1. 필드정보 생성
        XML fieldinfo = new XML("fieldinfo");
        makeFileInfo(fieldinfo);
        xmlBody.addElement(fieldinfo);
        
        //2. 데이타 정보 생성
        XML retireflow = new XML("retireflow");
        makeRetireflow(retireflow);
        xmlBody.addElement(retireflow);

        //3. 문서 출력
        //수정..
        XMLDocument x = new XMLDocument();
        x.setCodeset("UTF-8");
        x.addElement(xmlBody);
        x.output(out);
        
        //out.print("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        //out.print(xmlBody);        
        if(log.isDebugEnabled()) log.debug(xmlBody);
        
    }
    
    /**
     * 오즈레포트 전용의 fileInfo를 생성한다.
     */
    private void makeFileInfo(XML fieldinfo){
        for(String  key : fieldInfos.keySet()){
            XML xml = new XML("field");
            xml.addAttribute("name",key);
            xml.addAttribute("type",fieldInfos.get(key));                   
            fieldinfo.addElement(xml);
        }
    }
    /**
     * 오즈레포트 전용의 retireflow를 생성한다.
     */
    private void makeRetireflow(XML retireflow){
        for(HashMap<String,String> fieldInfos : datas){
            XML dataTag = new XML(RECORD_SET_NODE_NAME);
            for(String key : fieldInfos.keySet() ){
                XML xml = new XML(key);
                xml.setTagText(fieldInfos.get(key));            
                dataTag.addElement(xml);
            }
            retireflow.addElement(dataTag);
        }
    }
    
}
