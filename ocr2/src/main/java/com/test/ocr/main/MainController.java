package com.test.ocr.main;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.test.ocr.DTO.ReceiptSaveDTO;

import lombok.Getter;
import lombok.Setter;

@Controller
@Getter
@Setter
public class MainController {
	
	@GetMapping("/")
	
	public String ocr(Model model) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (!model.containsAttribute("receiptForm")) {
	        model.addAttribute("receiptForm", new ReceiptSaveDTO());
	    }
        // 로그인 여부 구분
        boolean isLogin = !auth.getPrincipal().equals("anonymousUser");

        model.addAttribute("isLogin", isLogin);
		return "home";
	}
}
