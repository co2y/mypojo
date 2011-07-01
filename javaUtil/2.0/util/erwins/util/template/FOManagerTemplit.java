
package erwins.util.template;

import java.io.File;
import java.io.Serializable;

import erwins.util.exception.BusinessException;
import erwins.util.lib.FileUtil;
import erwins.util.root.Singleton;

/**
 * FileOnjectManager입니다.
 * File로 캐싱하는 Object입니다. NAS에 파일을 넣어주세요.
 * 싱글톤 입니다. (싱글톤은 상속해서 구현 or 스프링을 사용하세요)
 * 응???? 이거 머지. 왜만들었지?
 */
@Singleton
public abstract class FOManagerTemplit<T extends Serializable> implements UpdateAble{
    
    private T object;
    
    abstract protected File getFile();
    
    /**
     * 최초로 설정될 객체를 등록하자. 
     */
    abstract protected T setInitObject();
    
    /**
     * load도중 오류 발생(시리얼 불일치)시 파일을 삭제하고 {@link #setInitObject()}를 호출한다.
     * 최초 로드시  {@link #setInitObject()}를 호출한다.
     */
    public T get(){
        if(object==null){
            try {
                load();
            }
            catch (Exception e) {
                if(getFile().exists()) getFile().delete();
                initObject();
            }
            if(object==null) initObject();
        }
        return object;
    }
    
    public void update(Observer observer,Object arg){
        object = null;
    }
    
    public void save(){
        FileUtil.setObject(getFile(), object);
    }
    public void save(T newObj){
        FileUtil.setObject(getFile(), newObj);
    }
    
    private void load(){
        object =  FileUtil.getObject(getFile());
    }
    
    private void initObject(){
        object =  setInitObject();
        save();
    }
    
    public void delete(){
        if(!getFile().delete()) throw new BusinessException("삭제할 수 없습니다.");
    }

}
