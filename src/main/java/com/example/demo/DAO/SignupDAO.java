package com.example.demo.DAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.DTO.SignupDTO;
import com.example.demo.mapper.SignMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SignupDAO {

	@Autowired
	private SignMapper signmapper;
	
	
	public void register(SignupDTO t) {
		signmapper.InsertSignup(t);
		
	}
	
}
