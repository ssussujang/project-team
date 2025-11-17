package com.example.demo.DAO;
import java.util.ArrayList;

import org.apache.ibatis.annotations.Mapper;

import com.example.demo.DTO.SignupDTO;

@Mapper
public interface SignupMapper {
	void register(SignupDTO t);
}
