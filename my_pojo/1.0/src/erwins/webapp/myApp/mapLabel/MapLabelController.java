
package erwins.webapp.myApp.mapLabel;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.View;

import erwins.util.morph.MapToBean;
import erwins.util.web.WebUtil;
import erwins.webapp.myApp.AjaxView;
import erwins.webapp.myApp.Current;
import erwins.webapp.myApp.RequestToMapForApp;
import erwins.webapp.myApp.RootController;
import erwins.webapp.myApp.user.SessionInfo;

@Controller
@RequestMapping("/mapLabel/*")
public class MapLabelController extends RootController{
	
	@Autowired private MapLabelService mapLabelService ; 
	@Autowired private MapToBean mapToBean;
	@Autowired private RequestToMapForApp requestToMap;
	
	@RequestMapping("/page")
	public String page(HttpServletRequest req) {
		if(WebUtil.isMobile(req)) return "mobile/mapLabel";
		else return "mapLabel/page";
	}
	@RequestMapping("/normal")
	public String normal() {
		return "mapLabel/page";
	}
	@RequestMapping("/mobile")
	public String mobile() {
		return "mobile/mapLabel";
	}

    /**  관리자 권한을 고려하지 않고 만든다.  */
    @RequestMapping("/save")
    public View save(HttpServletRequest req) {
    	SessionInfo info = Current.getInfo();
    	info.constraintLogin();
    	MapLaebl suser = mapToBean.build(requestToMap.toMap(req), MapLaebl.class);
    	suser.setGoogleUserId(info.getUser().getId());
    	suser = mapLabelService.saveOrMerge(suser);
    	return new AjaxView(suser.getId());
    }
    
    @RequestMapping("/remove")
    public View remove(@RequestParam String id) {
    	MapLaebl entity = mapLabelService.getById(id);
    	Current.getInfo().constraintAdminOrUser(entity); //일케하면 안된다. 나중에 서비스를 공용으로 하던가 할것 
    	mapLabelService.delete(entity.getId());
    	return new AjaxView("정상적으로 삭제되었습니다.");
    }
    
    @RequestMapping("/search")
    public View search(HttpServletRequest req,HttpServletResponse resp) {
    	Collection<MapLaebl> list = mapLabelService.findAll();
    	initGoogleUser(list);
    	return new AjaxView(list);
    }

}
