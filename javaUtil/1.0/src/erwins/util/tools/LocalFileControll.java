
package erwins.util.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.filefilter.IOFileFilter;

import erwins.util.lib.Files;
import erwins.util.lib.Strings;
import erwins.util.vender.apache.Log;
import erwins.util.vender.apache.LogFactory;

/** 로컬 파일들을 일괄삭제, 이동 시킬때 사용한다. 
 * 추가된 파일들은 모두 명령 메소드에거 실행된다. 주의!*/
public class LocalFileControll{
	
	protected Log log = LogFactory.instance(this.getClass());
	
	private List<File> list = new ArrayList<File>();
	
	public LocalFileControll addByExt(String directory,String ... exts){
		Iterator<File> i = Files.iterateFiles(new File(directory), true, exts);
		while(i.hasNext()) list.add(i.next());
		return this;
	}
	
	public LocalFileControll add(String directory,IOFileFilter filter){
		Iterator<File> i = Files.iterateFiles(directory,filter);
		while(i.hasNext()) list.add(i.next());
		return this;
	}
	
	public LocalFileControll add(String directory){
		return add(directory,Files.ALL_FILES);
	}
	
	/* ===================================================================================== */
	/*                                    run                                                */
	/* ===================================================================================== */
	
	public void moveToTargetDirectory(String directory){
		File to  = new File(directory);
		for(File each : list){
			File dest = new File(to,each.getName());
			dest = Files.uniqueFileName(dest);
			each.renameTo(dest);
		}
	}
	
	/** 파일의 양이 많아서 1000개씩 끊어서 저장해야 할경우. => 이름은 순차적으로 변경됨. */
	public void moveToSeparatedDirectory(String directory){
		int i = 0;
		File root  = new File(directory);
		File eachDir = new File(root,"each(001)");
		eachDir.mkdir();
		for(File each : list){
			if(++i%1000==0) {
				Files.uniqueFileName(eachDir);
				eachDir.mkdir();
			}
			String name = Strings.leftPad(String.valueOf(i),6,"0") + ".";
			File newEachFile = new File(eachDir,name+ Strings.getExtention2(each.getName()));
			each.renameTo(newEachFile);
		}
	}
	
	public void remove(boolean real){
		for(File each : list){
			if(real) Files.delete(each);
			log.debug("[{0}] is deleted", each.getAbsolutePath());
		}		
	}
    
    
}