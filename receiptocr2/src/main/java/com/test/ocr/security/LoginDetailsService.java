package com.test.ocr.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.test.ocr.DTO.UserDTO;
import com.test.ocr.mapper.UserMapper;

@Service
public class LoginDetailsService implements UserDetailsService {

	private final UserMapper userMapper;
	private final PasswordEncoder passwordEncoder;
	
	public LoginDetailsService(UserMapper um, PasswordEncoder pwe) {
		this.userMapper = um;
		this.passwordEncoder = pwe;
	}
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserDTO userdto = userMapper.findUser(username);
		if (userdto == null) {
			throw new UsernameNotFoundException("사용자 없음");
		}
		System.out.println("로그인 성공! 사용자: " + username);
		System.out.println("loadUserByUsername: " + username);
		
		
		
		return new LoginDetails(userdto);
		
		
	}
	
	
}
