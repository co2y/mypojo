
package erwins.util.observer;

import java.util.ArrayList;
import java.util.List;



/**
 * Observer패턴시 사용하는 컨테이너 입니다. 많아지면 enum으로 관리하세요
 * @author erwins(my.pojo@gmail.com)
 */
public class Observer{
    
    private List<UpdateAble> list = new ArrayList<UpdateAble>();
    
    public synchronized void add(UpdateAble reloadAble){
        list.add(reloadAble);
    }
    
    public synchronized void remove(UpdateAble reloadAble){
        list.remove(reloadAble);
    }
    
    public void updateAll(Object arg){
        for(UpdateAble reloadAble : list) reloadAble.update(this,arg);
    }

}
