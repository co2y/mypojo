
package erwins.webapp.myApp.mtgo;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.View;

import erwins.util.morph.MapToBean;
import erwins.webapp.myApp.AjaxView;
import erwins.webapp.myApp.RequestToMapForApp;
import erwins.webapp.myApp.RootController;

@Controller
@RequestMapping("/mtgo/*")
public class MTGOController extends RootController{
	
	@Autowired private MapToBean mapToBean;
	@Autowired private RequestToMapForApp requestToMap;
	@Autowired private DeckService deckService;
	@Autowired private CardService cardService;
	
	@RequestMapping("/page")
	public String normal() {
		return "mtgo/page";
	}
	
    @RequestMapping("/list")
    public View list(HttpServletRequest req) {
    	Collection<Deck> list = deckService.findAll();
    	return new AjaxView(list);
    }
    
    @RequestMapping("/save")
    public View save(HttpServletRequest req) {
    	Deck deck = mapToBean.build(requestToMap.toMap(req), Deck.class);
    	deckService.saveOrUpdate(deck);
    	return new AjaxView("정상적으로 저장되었습니다.");
    }
    
    @RequestMapping("/updateWinRate")
    public View updateWinRate(HttpServletRequest req) {
    	String id = req.getParameter("id");
    	boolean isWin =  requestToMap.getBoolean(req, "isWin");
    	boolean isMinus =  requestToMap.getBoolean(req, "isMinus");
    	deckService.updateWinRate(id,isWin,isMinus);
    	return new AjaxView("");
    }

}
