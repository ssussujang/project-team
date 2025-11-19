package com.example.practice.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.practice.dto.DTO;
import com.example.practice.mapper.TestMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DAO implements MyService{
	@Autowired
	private TestMapper testmapper;
	
	@Override
	public void register(DTO t) {
		testmapper.InsertUser(t);
		
	}
	
	@Override
	public void update(DTO t) {
	   testmapper.UpdateUser(t);
	   
	}

	@Override
	public boolean exists(String name) {
		
		return testmapper.exists(name) > 0;
	}


}
