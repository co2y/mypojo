package erwins.util.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * 미검증~
 * @author erwins(my.pojo@gmail.com)
 **/
public class Runtimes{
    private String path;
    private String exec;
    private List<String> arguments = new ArrayList<String>();
    
    
    public Runtimes(String path,String exec){
        this.path = path; 
        this.exec = exec; 
    }
    
    public void addArg(String arg){
        arguments.add(arg);
    }
    
    public void excc(String arg){
        try {
            Runtime.getRuntime().exec(path+exec, arguments.toArray(new String[arguments.size()]));
        }
        catch (IOException e) {
            throw new RuntimeException(e.getMessage(),e);
        }
    }
    

}