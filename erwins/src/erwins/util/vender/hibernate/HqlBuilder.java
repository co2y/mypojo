
package erwins.util.vender.hibernate;

import org.hibernate.Query;
import org.hibernate.Session;

public interface HqlBuilder {

    /** distinct는 걍 옵션처럼 붙여준다 */
    public abstract HqlBuilder select(String str);

    public abstract HqlBuilder from(String str);

    public abstract HqlBuilder join(String str, boolean fetch);

    /** 기본값은 fetch 가 false */
    public abstract HqlBuilder join(String str);

    public abstract HqlBuilder leftJoin(String str, boolean fetch);

    /** 기본값은 fetch 가 false */
    public abstract HqlBuilder leftJoin(String str);

    /**
     * @param and and조건이면 true/or조건이면 false
     */
    public abstract HqlBuilder open();
    public abstract HqlBuilder openSubQuery(String id);
    public abstract HqlBuilder close();
    public abstract HqlBuilder closeSubQuery();

    
    public abstract HqlBuilder groupBy(String groupby);
    public abstract HqlBuilder orderBy(String ... orderBy);
    public abstract HqlBuilder orderBy(String orderBy,boolean desc);
    

    public abstract HqlBuilder in(String field, Object[] obj);
    
    public abstract HqlBuilder eq(String field, Object obj);
    public abstract HqlBuilder ne(String field, Object obj);
    
    public abstract HqlBuilder isNull(String field);
    
    public abstract HqlBuilder isNotNull(String field);

    public abstract HqlBuilder like(String field, String obj);

    public abstract HqlBuilder iLike(String field, String obj);

    public abstract Query count(Session session);

    public abstract Query query(Session session);
    
    public String test1();
    public String test2();
    

}