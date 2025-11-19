package com.example.practice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.practice.dao.MyService;
import com.example.practice.dto.DTO;

@Controller
@RequestMapping("/test")
public class MainController {
	
	@Autowired
	private MyService myService;
	
	@GetMapping("/form")
	public String showForm() {
		return "index";
	}
	
	@PostMapping("/register")
	public String register(DTO t, RedirectAttributes ra) {
	     try {
	            myService.register(t);
	            ra.addFlashAttribute("msg", "가입이 완료되었습니다.");
	        } catch (DuplicateKeyException e) {
	            ra.addFlashAttribute("msg", "이미 존재하는 이름입니다.");
	        }
	        return "redirect:/test/form";
	}
	
	@GetMapping("/change")
	public String changePage() {
	    return "myidchange"; 
	}
	
	@GetMapping("/check")
	public String checkName(@RequestParam("m_name") String m_name, 
	                        RedirectAttributes ra) {

	    boolean exists = myService.exists(m_name);

	    if (!exists) {
	        ra.addFlashAttribute("msg", "존재하지 않는 이름입니다.");
	        return "redirect:/test/change";
	    }

	    // 이름이 존재하면, 수정 폼에서 hidden으로 쓰려고 이름을 넘김
	    ra.addFlashAttribute("name", m_name);

	    return "redirect:/test/change";
	}

	
	
	@PostMapping("/update")
	public String update(DTO t,  RedirectAttributes ra) {
		myService.update(t);
		
		ra.addFlashAttribute("msg", "수정이 완료되었습니다.");
		 return "redirect:/test/form";  
	}
	

}
