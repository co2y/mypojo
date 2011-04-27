package erwins.util.webapp;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;

import javax.annotation.Resource;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import org.datanucleus.exceptions.NucleusObjectNotFoundException;
import org.springframework.orm.jdo.JdoObjectRetrievalFailureException;
import org.springframework.orm.jdo.support.JdoDaoSupport;

import erwins.util.lib.StringUtil;
import erwins.util.root.EntityId;
import erwins.util.root.EntityInit;
import erwins.util.root.EntityMerge;

public abstract class GenericAppEngineDao<T extends EntityId<String>>  extends JdoDaoSupport{
	
	private Class<T> persistentClass;
    
    @SuppressWarnings("unchecked")
	public GenericAppEngineDao() {
        this.persistentClass = (Class<T>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }
    
    public Class<T> getPersistentClass() {
        return persistentClass;
    }
	
    /** persistenceManagerFactory라는 이름으로 bean이 등록되어 있어야 한다. */
	@Resource(name="persistenceManagerFactory")
    public void init(PersistenceManagerFactory persistenceManagerFactory){
		setPersistenceManagerFactory(persistenceManagerFactory);
    }
	
	@SuppressWarnings("unchecked")
	protected Collection<T> search(JqlBuilder jql,String order){
		Query q = getPersistenceManager().newQuery(getPersistentClass());
		String where = jql.getWhere();
		if(!StringUtil.isEmpty(where)){
			q.setFilter(where);
			q.declareParameters(jql.getParameterInfo());
		}
		if(jql.isPaging()){
			q.setRange(jql.getSkipResults(), jql.getSkipResults()+jql.getPagingSize());
		}
		if(order!=null) q.setOrdering(order);
		return (Collection<T>) q.executeWithArray(jql.getParam());
	}
	protected Collection<T> search(JqlBuilder jql){
		return search(jql,null);
	}
	
	public Collection<T> findAll(){
		Collection<T> result = getJdoTemplate().find(getPersistentClass()); 
		return  result;
	}
	protected Collection<T> findAll(String order){
		return  getJdoTemplate().find(getPersistentClass(),null,order);
	}
	/** 없으면 null을 리턴한다. */
	public T getById(String id){
		T entity;
		try {
			entity = getJdoTemplate().getObjectById(getPersistentClass(), id);
		} catch (JdoObjectRetrievalFailureException e) {
			if(e.getRootCause() instanceof NucleusObjectNotFoundException ) return null;
			throw e;
		} 
		return  entity;
	}
	/** 웬만하면 update용으로는 사용하지 말것!! 다이렉트update는 좋지않다. */
	public T saveOrUpdate(T entity) {
		if(entity instanceof EntityInit) ((EntityInit) entity).initValue();
		return getJdoTemplate().makePersistent(entity);
	}
	/** 입력은 insert / 변경은 기존 객체를 불러와 수동으로 업데이트 한다. 이렇게 해야 createDate가 유지된다 ㅅㅂ.. */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public T saveOrMerge(T entity) {
		if(entity.getId()==null) return saveOrUpdate(entity);
		else{
			EntityMerge server = (EntityMerge) getById(entity.getId());
			server.mergeByClientValue(entity);
			((EntityInit) server).initValue();
			return (T)server;
		}
	}
	
	public void delete(T entity) {
		getJdoTemplate().deletePersistent(entity);
	}
	/** 해당 */
	public void delete(String id) {
		T entity = getJdoTemplate().getObjectById(getPersistentClass(), id);
		getJdoTemplate().deletePersistent(entity);
	}
	
}
