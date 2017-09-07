package com.AGI.salvo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class WebController {

	@RequestMapping(path = "/", method = RequestMethod.GET)
	public String index(){
		return "redirect:/web/games.html";
	}
}
