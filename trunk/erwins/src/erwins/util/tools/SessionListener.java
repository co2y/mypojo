
package erwins.util.tools;

import java.util.Collection;
import java.util.HashMap;

import javax.servlet.http.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import erwins.util.lib.Days;

//<listener><listener-class>클래스풀경로</listener-class></listener>
/**
 * 세션 생성을 감시한다. WEB-INF에 등록 후 사용하자.
 * @author  erwins(quantum.object@gmail.com)
 */
public class SessionListener implements HttpSessionListener {
    
    protected static Log log = LogFactory.getLog(SessionListener.class);

    /**
     * @uml.property  name="serverStartTime"
     */
    private static String serverStartTime ;
    /**
     * @uml.property  name="nowSessionCount"
     */
    private static long nowSessionCount = 0;
    /**
     * @uml.property  name="cumulatedSessionCount"
     */
    private static long cumulatedSessionCount = 0;    
    private static HashMap<String, HttpSession> sessionMap = new HashMap<String, HttpSession>();
    
    static{
        serverStartTime = Days.DATE_SIMPLE.get();
    }

    public synchronized void  sessionCreated(HttpSessionEvent evt) {
        HttpSession session = evt.getSession();
        nowSessionCount++;
        cumulatedSessionCount++;
        sessionMap.put(session.getId(), session);
        log.debug("session Created");
    }

    public synchronized void sessionDestroyed(HttpSessionEvent evt) {
        HttpSession session = evt.getSession();
        nowSessionCount--;
        sessionMap.remove(session.getId());
        log.debug("session Destroyed");
    }


    /**
     * 현재 생성되어있는 세션 수
     * @uml.property  name="nowSessionCount"
     */
    public static long getNowSessionCount() {
        return nowSessionCount;
    }
    
    /**
     * 누적 생성되어있는 세션 수 : 즉 전체 방문자 수
     * @uml.property  name="cumulatedSessionCount"
     */
    public static long getCumulatedSessionCount() {
        return cumulatedSessionCount;
    }

    /**
     * @return
     * @uml.property  name="serverStartTime"
     */
    public static String getServerStartTime() {
        return serverStartTime;
    }
    
    public static Collection<HttpSession> getSessions(){
        return sessionMap.values();
    }
    
}
