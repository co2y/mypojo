package erwins.util.lib;


import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

import org.apache.commons.lang.StringUtils;

import erwins.util.root.EntityId;
import erwins.util.root.StringIdEntity;

/**
 * Collection과 Array에 관한 Util이다.
 * java.util.EnumSet을 잘 활용할것!
 * @author erwins(my.pojo@gmail.com)
 */
public abstract class Sets {
    
    
    /**
     * null safe하게 IdEntity를 비교한다.
     */
    public static <ID extends Serializable> boolean isEqualIdEntity(EntityId<ID> a, EntityId<ID> b){
        if(a==null || b==null) return false;
        if(a.getId().equals(b.getId())) return true;
        return false;
    }
    
    /**
     * 얕은 복사를 수행한다.
     */
    public static <T> List<T> copy(List<T> c) {
        List<T> result = new ArrayList<T>(c.size());
        result.addAll(c);
        return result;
    }    
    
    
    /**
     * 빈 컬렉션인지? 
     */
    public static boolean isEmpty(Collection<?> c) {
        if(c==null || c.size()==0) return true;
        return false;
    }
    
    /**
     * List를 짧게 만든다. 별 의미 없음. ㅋ 
     */
    public static <T> List<T> makeList(T ... objs) {
        List<T> list = new ArrayList<T>();
        for(T obj: objs) list.add(obj);
        return list;
    }
    
    /**
     * 소팅을 해 보아요~ 조건이 간단하고 순차 졍렬만 지원되지만 그래도 좋아~ 
     */
    public static <T extends Comparable<T>> List<T> sort(List<T> list) {
        Collections.sort(list, new Comparator<T>(){
            public int compare(T dom1, T dom2) {
                return dom1.compareTo(dom2);
            }
        });
        return list;
    }
    
    /**
     * List를 Map으로 교체한다.
     */
    public static <T extends StringIdEntity> Map<String,T> getMap(List<T> list) {
        Map<String,T> map = new HashMap<String,T>();
        for(T obj : list) map.put(obj.getId(), obj);
        return map;
    }    
    
    /**
     * DataAccessUtils과 유사함.
     * Collection에서 Unique값을 추출해 낸다.
     * 수정 핋요할듯..
     */
    public static Integer getResultInt(List<Object> list) {
        if(list==null) throw new RuntimeException("list is null . collection must be not null");
        if(list.size()!=1) throw new RuntimeException(list.size() + " collection must be unique");
        Object obj = list.get(0);
        if(obj instanceof BigDecimal) return ((BigDecimal) obj).intValue();
        return (Integer)list.get(0);
    }
    
    /**
     * DataAccessUtils과 유사함.
     * Collection에서 Unique값을 추출해 낸다.
     */
    public static Number getResultCount(List<Object> list) {
        if(list==null) throw new RuntimeException("list is null . collection must be not null");
        if(list.size()!=1) throw new RuntimeException(list.size() + " collection must be unique");
        Object obj = list.get(0);
        if(obj instanceof BigDecimal) return ((BigDecimal) obj).longValue();
        return (Number)obj;
    }
    
    /**
     * DataAccessUtils과 유사함.
     * Collection에서 Unique값을 추출해 낸다.
     */
    public static <T> T getResultUnique(List<T> list) {
        if(list==null) throw new RuntimeException(" collection is null! ");
        else if(list.size()!=1) throw new RuntimeException(list.size() + " collection nust be unique");
        else return list.get(0);
    }
    
    /**
     * 마지막 객체를 반환한다. 
     */
    public static <T> T getLast(List<T> list) {
        if(list==null || list.size()==0) return null;
        return list.get(list.size()-1);
    }
    public static <T> T getLast(T[] list) {
        return list[list.length-1];
    }
    
    // ===========================================================================================
    //                                    비교하기  각기 3종류를 가진다.
    // ===========================================================================================
    
    /**
     * ==으로 비교한다. 
     */
    public static <T> boolean isSameAny(T body ,T ... items) {
        if(body==null || items.length==0) return false;
        for(T item:items) if(body == item) return true;
        return false;
    }
    
    /**
     * 배열에 해당 물품을 가지고 있는지 검사한다. 
     * 하나라도 있으면 true를 리턴한다.
     */
    public static <T> boolean isEqualsAny(T[] bodys ,T ... items) {
        if(bodys==null || items.length==0) return false;
        for(T body : bodys) for(T item:items) if(item.equals(body)) return true;
        return false;
    }
    
    /**
     * 단일 물품의 값과 배열내의 값을. 비교한다. 
     * 하나라도 있으면 true를 리턴한다.
     */
    public static <T> boolean isEqualsAny(Collection<T> bodys ,T ... items) {
        if(bodys==null || items.length==0) return false;
        for(T body:bodys) for(T item:items) if(item.equals(body)) return true;
        return false;
    }
    
    /**
     * 단일 물품의 값과 배열내의 값을. 비교한다. 
     * 하나라도 있으면 true를 리턴한다.
     */
    public static <T> boolean isEqualsAny(T body ,T ... items) {
        if(body==null || items.length==0) return false;
        for(T item:items) if(item.equals(body)) return true;
        return false;
    }
    
    /**
     * 배열에 null이 있는지 확인한다. 
     * 하나라도 있으면 true를 리턴한다. 배열의 size가 0이면 false이다.
     */
    public static <T> boolean isNullAny(T ... items) {
        for(T item:items) if(item == null) return true;
        return false;
    }
    
    /**
     * 배열에 해당 물품의 클래스를 가지고 있는지 검사한다. 
     * 하나라도 있으면 true를 리턴한다.
     * ex) if(Sets.isInstance(annos,Hidden.class)) continue;
     */
    public static <T> boolean isInstanceAny(T[] bodys , Class<? extends T> ...  clazzs) {
        if(bodys==null || clazzs.length==0) return false;
        for(T each : bodys) for(Class<? extends T> clazz : clazzs) if(clazz.isInstance(each)) return true;
        return false;
    }
    
    
    // ===========================================================================================
    //                                    null safe
    // ===========================================================================================
    
    /**
     * null sasfe하게 list의 사이즈를 구한다. 
     */
    public static Integer getSize(List<?> list) {
        if(list==null) return 0;
        return list.size();
    }
    
    /**
     * 배열을 List로 반환한다.
     */
    public static <T> List<T> toList(T ... a) {
        if (a == null) return Collections.emptyList();
        //list = Arrays.asList(a);
        List<T> list = new ArrayList<T>();
        for(T each : a) list.add(each);
        return list;
    }
    
    // ===========================================================================================
    //                                    etc
    // ===========================================================================================
    
    /**
     * 형을 알 수 없는 obj를 List로 바꾼다.
     * toList를 형을 알 수 없은 상태로 사용할 경우 배열채로 List에 들어가 버린다.
     * 따라서 이 메소드를 사용하자.
     */
    public static List<String> toStringList(Object obj) {
        if(obj instanceof String) return  toList((String)obj);
        else if(obj instanceof String[]) return  toList((String[])obj);
        else return Collections.emptyList();
    }
    
    /**
     * null safe하게 list의 개체를 반환한다. 
     * i가 최대값보다 크거나 음수일 경우 null을 리턴한다.
     */
    public static <T> T getObject(List<T> a,int i) {
        int size = a.size();
        if(i >= size || i < 0) return null;
        return a.get(i);
    }

    /**
     * Map에 담긴 List(문자배열형식)를  => List에 담긴 Map으로 변환한다.
     * getElementsByTagName등으로 개별 속성을 배열로 가져올 경우 이것을 객체별로 분류할때 사용된다.
     * Map에 담긴 List의 사이즈가 모두 동일해야 한다.
     */
    public static List<HashMap<String,String>> swap(HashMap<String,List<String>> map){
        List<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
        
        //초기화
        int maxSize = map.values().iterator().next().size();
        for(int i=0;i<maxSize;i++) list.add(new HashMap<String,String>());
        
        //변환작업
        for(String key : map.keySet()){
            List<String> thisList = map.get(key);
            int thisSize = thisList.size();
            if(thisSize != maxSize) throw new RuntimeException(maxSize + " : " +thisSize + "사이즈가 균일하지 않음");
            for(int i=0;i<thisSize;i++){
                list.get(i).put(key,thisList.get(i));
            }
        }
        
        return list;
    }

    /**
     * 오라클 CLOB대신 VC를 사용함
     * getByte()는 너무 많은 리소스 사용하기때문에 한글 800자 기준으로 문자열을 쪼개줌 
     * vc(4000) 800은 경험치임..  UTF-8은 한글이 3바이트
     */
    public static List<String> getOracleStr(String str){
        if(str.length()==0) return Collections.emptyList();
        List<String> list = new ArrayList<String>();
        int oracleMaxCaracter = 800;
        int totalCount = str.length()/oracleMaxCaracter +1;
        for(int i=0;i<totalCount;i++){
            if(i==totalCount-1) list.add(str.substring(i*oracleMaxCaracter,str.length()));
            else list.add(str.substring(i*oracleMaxCaracter,(i+1)*oracleMaxCaracter));
        }
        return list;
    }

    /**
     * 오라클 CLOB대신 VC를 사용함
     * 분산된 컬럼을 List에 담아 하나의 String으로 반환함 
     */
    public static String getOracleStr(List<String> list){                
        if(list==null) return StringUtils.EMPTY;
        StringBuffer stringBuffer = new StringBuffer();
        for(String string : list) stringBuffer.append(string);        
        return  stringBuffer.toString();
    }

    /**
     * Map으로 부터 List<T>를 가져온다. <br>
     * null일 경우 새로운 new ArrayList<T>를 반환한다.
     */
    public static <T> List<T>  getList(Map<String,List<T>> map, String key){
        List<T> list = map.get(key);
        if(list == null){
            list = new ArrayList<T>();
            map.put(key, list);
        }
        return list;
    }

    /**
     * List로 부터  XmlTag을 가져온다.
     * 더 나은 방법이 있는지 모르겠다.
     * 자주 사용하지 말것
     */
    @SuppressWarnings("unchecked")
    public static <T> T getEntity(List<T> tags , int index , Class<?> clazz){
        T tag ;
        if(tags.size() <= index){     
            try {
                tag = (T) clazz.newInstance();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
            tags.add(tag);
        }else{
            tag = tags.get(index);
        }
        return tag;
    }
    
    /**
     * 배열을 역산해서 리턴한다. 
     */
    public static <T> List<T> inverse(List<T> list) {
        List<T> inversed = new ArrayList<T>();
        for (int i = list.size(); i > 0; i--) {
            inversed.add(list.get(i-1));
        }
        return inversed;
    }

    
    /**
     * value들을 모두 합해서 리턴한다. 
     */
    public static <T> BigDecimal getSum(HashMap<T,BigDecimal> map) {
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal value : map.values())
            sum = sum.add(value);
        return sum;
    }
    
    /**
     * 일치하는 값이 없을때만 obj를 추가한다.
     * 자료에 순사가 있어 Set을 사용할 수 없을때 사용한다.
     * 자료가 작을때만 사용 가능하다. 
     */
    public static <T> void addIfNotFound(List<T> list,T obj) {
        for (T value : list)
            if(value.equals(obj)) return;
        list.add(obj);
    }
    
}