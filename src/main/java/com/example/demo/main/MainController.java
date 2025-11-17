package com.example.demo.main;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.DAO.SignupMapper;
import com.example.demo.DTO.SignupDTO;

@Controller
@RequestMapping("/Signup")
public class MainController {
	
	@Autowired
	private SignupMapper sm;
	
	@GetMapping("/form")
	public String showForm() {
		return "Signup";
	}
	
	
	@PostMapping("/register")
	public String register(SignupDTO t) {
		sm.register(t);
		return "Signup";
	}
	
	
}
