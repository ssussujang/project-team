package com.example.demo.main;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
	
	@GetMapping("/")
	public String showForm() {
		return "Signups";
	}
	
	
	
}
