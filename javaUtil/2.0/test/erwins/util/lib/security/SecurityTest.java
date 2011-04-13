
package erwins.util.lib.security;

import java.io.File;

import javax.crypto.SecretKey;

import org.apache.commons.lang.Validate;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import erwins.util.exception.Check;
import erwins.util.lib.FileUtil;
import erwins.util.lib.TextFileUtil;
import erwins.util.lib.security.Cryptor.Mode;

public class SecurityTest{
	
	private static File objectKeyDES = new File("D:/tempKey1.ser");
	private static File fileKeyDESede = new File("D:/tempKey2.ser");
	
	private static String orgText  = "690518-2046611";
	
	@BeforeClass
	public static void beforeClass(){
		Cryptor cryptor1 = new Cryptor();
		SecretKey key1 = cryptor1.setMode(Mode.DES).generateKey().getKey();
		FileUtil.setObject(objectKeyDES, key1); //일반적인 시리얼로 저장
		
		Cryptor cryptor2 = new Cryptor();
		cryptor2.setMode(Mode.DESede).generateKey().writeKey(fileKeyDESede);
	}
	
	@AfterClass
	public static void afterClass(){
		Check.isTrue(objectKeyDES.delete());
		Check.isTrue(fileKeyDESede.delete());
	}
	
	@Test
	public void cryptDES(){
		Cryptor cryptor = new Cryptor().setMode(Mode.DES).readKeyForObject(objectKeyDES);
		String en = cryptor.encryptBase64(orgText);
		String de = cryptor.decryptBase64(en);
		Validate.isTrue(de.equals(orgText));
	}
	
	@Test
	public void cryptDESede(){
		Cryptor cryptor = new Cryptor().setMode(Mode.DESede).readKey(fileKeyDESede);
		String en = cryptor.encryptBase64(orgText);
		String de = cryptor.decryptBase64(en);
		Validate.isTrue(de.equals(orgText));
	}
	
	@Test /** 걍 일반 문자열로 암호 만들어도 된다. */
	public void cryptInstance(){
		String key = "quantum.object@hotmail.com";
		String en = new Cryptor().generateKey(key).encryptBase64(orgText);
		String de = new Cryptor().generateKey(key).decryptBase64(en);
		Validate.isTrue(de.equals(orgText));
	}	
	
    @Test  /** 파일도 통으로 암호화 가능 */
    public void cryptFile(){
    	File org = buildOrgdile();
    	File sealed = new File("/sealed.txt");
    	File unsealed = new File("/unsealed.txt");
    	
    	Cryptor cryptor = new Cryptor().setMode(Mode.DESede).readKey(fileKeyDESede);
    	cryptor.encrypt(org, sealed);
    	cryptor.decrypt(sealed, unsealed);
    	Check.isTrue(TextFileUtil.read(org).toString().equals(TextFileUtil.read(unsealed).toString()));
    	Check.isTrue(org.delete());
		Check.isTrue(sealed.delete());
		Check.isTrue(unsealed.delete());
    }

	private File buildOrgdile() {
		File org = new File("/org.txt");
    	
    	StringBuilder buff = new StringBuilder();
    	for(int i=0;i<10000;i++){
    		buff.append("qweqwe");
    		buff.append("한글*^%$");
    		buff.append(i*i);
    		buff.append("\r\n");
    	}
    	FileUtil.writeStr(buff,org);
		return org;
	}
    
}
