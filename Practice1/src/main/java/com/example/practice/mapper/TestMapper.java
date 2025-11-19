package com.example.practice.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.example.practice.dto.DTO;

@Mapper
public interface TestMapper {
	void InsertUser(DTO m);
	void UpdateUser(DTO m);
	int exists(String name); 
}

