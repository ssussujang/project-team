package com.example.demo.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.example.demo.DTO.SignupDTO;

@Mapper
public interface SignMapper {
	int InsertSignup(SignupDTO t);
}



