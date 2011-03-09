package erwins.util.collections;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import net.sf.json.JSONObject;

/** 통계 등에 상용된다. */
public class MappedJson implements Iterable<Entry<String,JSONObject>>{
	private Map<String,JSONObject> map;
	
	private MappedJson(){}
	
	public static MappedJson hashInstance(){
		MappedJson instance = new MappedJson();
		instance.map = new HashMap<String,JSONObject>();
		return instance;
	}
	public static MappedJson treeInstance(){
		MappedJson instance = new MappedJson();
		instance.map = new TreeMap<String,JSONObject>();
		return instance;
	}
	/** hibernate를 위한 간단 변환기. 반드시 3개의 인자만 와야 하며 0,1번째는 null이 아니다.
	 * 트리형 통계에 사용하자. */
	public static MappedJson treeInstance(Iterable<Object[]> result){
		MappedJson instance = new MappedJson();
		instance.map = new TreeMap<String,JSONObject>();
		for(Object[] each : result){
			instance.add(each[0].toString(), each[1].toString(), each[2]);
		}
		return instance;
	}
	
	public MappedJson add(String key,String jsonKey,Object jsonValue){
		JSONObject json = map.get(key);
		if(json==null){
			json = new JSONObject();
			map.put(key, json);
		}
		json.put(jsonKey, jsonValue);
		return this;
	}

	public Iterator<Entry<String, JSONObject>> iterator() {
		return map.entrySet().iterator();
	}
}