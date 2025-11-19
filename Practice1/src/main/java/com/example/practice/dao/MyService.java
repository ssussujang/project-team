package com.example.practice.dao;

import com.example.practice.dto.DTO;

public interface MyService {
	void register(DTO t);
	boolean exists(String name);
	void update(DTO t); 
}
