package erwins.util.root;

import java.io.Serializable;


/**
 * Hibernate를 이용해서 Db에 저장되는 Enum 등에 사용된다.
 * @author     erwins(my.pojo@gmail.com)
 */
public interface Pair extends Serializable{
	
    /** DB에 저장되는 값을 반환한다. */    
    public String getValue();
    
    /** 화면에 표시될 논리적 이름을 반환한다. */    
    public String getName();
    
    public static class PairEnum{
    	/** Pair를 구현한 Enum값을 가져온다. */
    	public static <T extends Pair> T getEnum(Class<T> clazz,String value){
    		for(T pair : clazz.getEnumConstants()){
    			if(value.equals(pair.getValue())) return pair;
    		}
    		return null;
    	}
    }
    
    /** Pair를 Cache하고있는   */
    public static interface PairCache{
    	
    }
    
}