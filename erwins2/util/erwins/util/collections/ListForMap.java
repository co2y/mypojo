package erwins.util.collections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import erwins.util.root.PairObject;


/**
 * 이하의 간단 버전이다.
 * private List<Map<String,T>> list = new ArrayList<Map<String,T>>();
 */

public class ListForMap<T> implements Iterable<Map<String,T>>{
	
	private List<Map<String,T>> list;
	
	public ListForMap(List<Map<String,T>> list){
		this.list = list;
	}
	public ListForMap(){
		this.list = new ArrayList<Map<String,T>>();
	}
	
	/** key기준으로 현재의 값에   others를 추가한다. key에 속한 value는 LIST내에서 유니크 해야 한다. 
	 * 속도는 책임 못짐. */
	public void mergeByKey(final String key,ListForMap<T> ... others ){
		for(Map<String,T> eachMap : list){
			T keyValue = eachMap.get(key);
			for(ListForMap<T> each : others){
				for(Map<String,T> eachOtherMap : each){
					T nowKeyValue = eachOtherMap.get(key);
					if(keyValue.equals(nowKeyValue)){
						eachMap.putAll(eachOtherMap);
						break;
					}
				}
			}
		}
	}	
	
	/*
	public void mergeByKey(String key,String mergeKey){
		List<Map<String,T>> newList = new ArrayList<Map<String,T>>();
		
		T exist = null;
		Map<String,T> currentMap = null;
		int matchCount = 0;
		
		for(Map<String,T> each : list){
			T keyValue = each.get(key); 
			if(keyValue.equals(exist)){
				currentMap.put(mergeKey+ ++matchCount, each.get(mergeKey));
			}else{
				matchCount = 0;
				currentMap = each;
				exist = keyValue;
				currentMap.put(mergeKey+ ++matchCount, currentMap.get(mergeKey));
				currentMap.remove(mergeKey);
				newList.add(currentMap);
			}
		}
		this.list = newList;
	}
	*/
	
	/** iBatis 등에서 통계를 변환하는 목적. 이는 GROUP BY문장을 여러번 만들어야 하는 수고를 덜기 위함이다.
	 * 정해진 PK를 기준으로 동일하다면 nameKey를 새로운 key로 대체하는 value를 생성한다.
	 * pairs의 name이 이름으로 사용될 맵의 key이며 value가 값으로 사용될 맵의 이름이다. 
	 * pkKey는 List상에 유일값이며, nameKey은 새로 생성될 map의 key (값이 문자열이야 한다.), valueKey 값이 될 값의 키이다.
	 * 데이터는 PK순으로 소팅되어 있어야 한다 */
	public void toFlatData(String pkKey,PairObject ... pairs){
		List<Map<String,T>> newList = new ArrayList<Map<String,T>>();
		T exist = null;
		Map<String,T> currentMap = null;
		for(Map<String,T> each : list){
			T keyValue = each.get(pkKey); 
			if(!keyValue.equals(exist)){
				currentMap = each;
				exist = keyValue;
				newList.add(currentMap);
			}
			for(PairObject pair : pairs) currentMap.put(each.get(pair.getName()).toString(),each.get(pair.getValue()));
		}
		this.list = newList;
	}
	
	/** rownum을 지정해 준다. 화면을 고려한 것임으로 걍 Integer로 통일해 준다.
	 * 물론 T가 틀리다면 오류가 날것이다. */
	@SuppressWarnings("unchecked")
	public void rownum(String keyName){
		for(int i=0;i<list.size();i++){
			list.get(i).put(keyName, (T)new Integer(i+1));
		}
	}
	
	/* ================================================================================== */
	/*                                  이하 2개는 사용할지 미지수~                            */
	/* ================================================================================== */
	private String keyField;
	public void setKeyField(String keyField) {
		this.keyField = keyField;
	}
	
	public Map<String, T> getByKey(T value){
		for(Map<String,T> each : list){
			T keyValue = each.get(keyField);
			if(value.equals(keyValue)) return each;
		}
		Map<String, T> instance = new HashMap<String, T>();
		instance.put(keyField, value);
		list.add(instance);
		return instance;
	}
	
	@Override
	public Iterator<Map<String, T>> iterator() {
		return list.iterator();
	}
    
}