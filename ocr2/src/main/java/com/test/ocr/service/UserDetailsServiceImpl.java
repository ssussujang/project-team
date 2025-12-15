package com.test.ocr.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.test.ocr.DTO.UserDTO;
import com.test.ocr.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

	private final UserMapper userMapper;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		UserDTO user = userMapper.findUser(username);
		if (user == null) {
			throw new UsernameNotFoundException("존재하지 않는 사용자입니다.");
		}
		
		return org.springframework.security.core.userdetails.User.builder()
				.username(user.getU_id())
				.password(user.getU_pw())
				.build();
		
	}
	
}
