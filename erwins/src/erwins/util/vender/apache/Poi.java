
package erwins.util.vender.apache;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFHyperlink;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Hyperlink;

import erwins.util.lib.Formats;
import erwins.util.lib.Strings;
import erwins.util.tools.Mapp;

/**
 * POI 패키지의 HSSF를 편리하게.. 헤더칸은 1칸 이라고 일단 고정 사각 박스를 예쁘게 채울려면 반드시 null에 ""를 채워 주자~
 * @author  erwins(my.pojo@gmail.com)
 */
public class Poi extends PoiRoot{
    
    // ===========================================================================================
    //                                    생성자
    // ===========================================================================================
    
    public Poi(HSSFWorkbook wb){
        this.wb = wb;
        init();
    }
    
    public Poi(){
        this.wb = new HSSFWorkbook();
        init();
    }
    
    public Poi(String fileName){
    	try {
            stream = new FileInputStream(fileName);
            POIFSFileSystem filesystem = new POIFSFileSystem(stream);        
            wb = new HSSFWorkbook(filesystem);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }finally{
        	close();
        }
        init();
    }
    
    public Poi(File file){
        try {
            stream = new FileInputStream(file);
            POIFSFileSystem filesystem = new POIFSFileSystem(stream);        
            wb = new HSSFWorkbook(filesystem);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }finally{
        	close();
        }
        init();
    }
    
    /**
     * 스트림을 닫아준다.. ㅠㅠ
     * File로 POI를 만들때 반드시 닫아주자.. 뭐 안해도 되긴 한디..
     */
    public void close(){
        if(stream!=null) try {
            stream.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }    
    
    
    // ===========================================================================================
    //                                     간편쓰기.
    // ===========================================================================================
    
    private HSSFSheet nowSheet;
    
    /**
     * 1. 시트를 만들고 0번째 로우에 헤더를 만든다.
     * 2. 시트의 가로 , 세로 열이 같다면 merge한다.
     */
    public void addSheet(String sheetname,String[] ... titless){
    	nowSheet = wb.createSheet(sheetname);
        HSSFRow row ;
        for(String[] titles : titless){
            row = createNextRow();
            for(int j=0;j<titles.length;j++)
                row.createCell(j).setCellValue(new HSSFRichTextString(titles[j]));    
        }
        headerRowCount.add(titless.length);
    }
    
    public HSSFRow createNextRow() {
        int i = nowSheet.getPhysicalNumberOfRows(); //시트가 순수 createRow로 생성한 로우 수를 반환한다. 즉 중간에 공백이 있으면 안된다.
        HSSFRow row = nowSheet.createRow(i);
        return row;
    }
    
    /**
     * 간단한 시트를 완성한다.
     * 기본 입력은 하이버네이트 기본인  List<Object[]> 이다.
     * 즉.. 순서가 있는 2차원 배열이어야 한다. (map이나 bean은 사용 못함)
     */
    public void makeSimpleSheet(List<Object[]> list){
        int sheetNum = wb.getActiveSheetIndex();
        HSSFSheet sheet = wb.getSheetAt(sheetNum);
        HSSFRow row = null;
        int header = headerRowCount.get(sheetNum);
        for(int i=0;i<list.size();i++){
            Object[] obj = list.get(i);
            row = sheet.createRow(i+header);
            for(int j=0;j<obj.length;j++){
                row.createCell(j).setCellValue(new HSSFRichTextString(Strings.toString(obj[j])));
            }
        }
    }    
    
    /** 기생성된 row에 i번째 컬럼 부터 value를 입력한다. */
    public void setValues(int i,Object ... values){
        HSSFRow row = createNextRow();
        for(Object each : values){
            String value = null;
            if(each==null) value="";
            else if(each instanceof Number) value = Formats.DOUBLE2.get((Number)each);
            else value = each.toString();
            row.createCell(i++).setCellValue(new HSSFRichTextString(value));    
        }
    }
    
    public void temp(){
    	nowSheet = wb.createSheet("aa");
    	HSSFRow row = createNextRow();
    	HSSFCell cell =  row.createCell(0);
    	cell.setCellValue("1111");
    	cell =  row.createCell(1);
    	cell.setCellValue("2222");
    	HSSFHyperlink l = new HSSFHyperlink(HSSFHyperlink.LINK_DOCUMENT);
    	l.setAddress("aa!A1");
    	cell.setHyperlink(l);
    	
    }
    
    /** sheet의 마지막에 row를 생성하고 value를 입력한다. */    
    public void addValues(Object ... values){
        setValues(0,values);
    }
    
    /** 컬럼 순서같은건 없다. 간단메소드로서 사용에 주의할것. */
    @SuppressWarnings("unchecked")
	public void setListedMap(String sheetname,List<Map> list){
		if(list.size()==0) return;
		String[] colums = (String[]) list.get(0).keySet().toArray(new String[list.get(0).keySet().size()]);
		this.addSheet(sheetname, colums);
		
		for(Map each : list){
			String[] values = new String[colums.length]; 
			for(int i=0;i<colums.length;i++){
				Object value = each.get(colums[i]);
				values[i] = value == null? "" : value.toString() ;
			}
			this.addValuesArray(values);
		}
    }    
    public void setListedMapp(String sheetname,List<Mapp> list){
		if(list.size()==0) return;
		String[] colums =  list.get(0).keySet().toArray(new String[list.get(0).keySet().size()]);
		this.addSheet(sheetname, colums);
		
		for(Mapp each : list){
			String[] values = new String[colums.length]; 
			for(int i=0;i<colums.length;i++){
				values[i] = each.getStr(colums[i]);
			}
			this.addValuesArray(values);
		}
    }
    
    public void addValuesArray(Object[] values){
        setValues(0,values);
    }
    
}