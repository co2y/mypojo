
package erwins.util.web;

import java.io.File;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import erwins.util.exception.Throw;
import erwins.util.lib.Files;

/**
 * common을 이용한 파일 업로드.
 * Apache Commons를 이용, req에서 파일을 추출한다.
 * ProgressListener는 사용하지 않는다.
 * 추후 command를 넣어서 밸리데이션 체크를 하자.
 * 인코딩은 브라우저 jsp설정과 연관되는듯 하다.
 */
public class FileUploader {
    
    private HttpServletRequest req;
    private Map<String,String> map = new HashMap<String,String>();
    private FileFilter filter;
    private String encoding = "UTF-8";
    private int maxMb = 1024*2;
    
    public FileUploader(HttpServletRequest req){
        boolean isMultipart = ServletFileUpload.isMultipartContent(req);
        if(!isMultipart) throw new IllegalArgumentException("request is not multi/part");
        this.req = req;
    }

    public Map<String,String> upload(File repositoryPath){
        
        int yourMaxMemorySize = 1024 * 200;                 // threshold  값 설정 (0.2M?) 초기값은 0.01메가
        long yourMaxRequestSize = 1024 * 1024 * maxMb;   
        
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(yourMaxMemorySize);
        factory.setRepository(repositoryPath);
        
        ServletFileUpload upload = new ServletFileUpload(factory);
        //upload.setHeaderEncoding("EUC-KR"); //수정!!
        upload.setHeaderEncoding(encoding);
        upload.setSizeMax(yourMaxRequestSize);  // 임시 업로드 디렉토리 설정
        //upload.setProgressListener(listener);  //리스너는 사용하지 않는다.
        
        FileItem item = null;
        List<?> items;
        try {
            items = upload.parseRequest(req);
            for(Object aItem : items) {
                item = (FileItem)aItem;
                if(item.isFormField()) {
                    map.put(item.getFieldName(), item.getString());
                }else{
                    String file = item.getName();
                    file = file.substring(file.lastIndexOf(File.separator) + 1);
                    
                    File uploadedFile = new File(repositoryPath,file);
                    uploadedFile = Files.uniqueFileName(uploadedFile);
                    if(filter != null && !filter.isStorable(uploadedFile)) continue;
                    item.write(uploadedFile);
                    //fileItem.get();  //메모리에 모두 할당
                }
            }
        } catch (Exception e) {
            Throw.wrap(e);
        }
        return map;
    }
    
    /** 디폴트 값은 UTF-8 */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /** 디폴트 값은 1024*2 인 2기가. */
    public void setMaxMb(int maxMb) {
        this.maxMb = maxMb;
    }

    public void setFilter(FileFilter filter) {
        this.filter = filter;
    }

    public static interface FileFilter{
        /** false를 리턴하면 저장하지 않는다. */
        public boolean isStorable(File file);
    }

}
