package com.test.ocr.security;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.test.ocr.DTO.UserDTO;

import lombok.Getter;

@Getter
public class LoginDetails implements UserDetails {

	private static final long serialVersionUID = 1L;
	
	private UserDTO userdto;
	
	public LoginDetails(UserDTO userdto) {
		this.userdto = userdto;
	}
	
	public UserDTO getUserDTO() {
		return userdto;
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities(){
		Collection<GrantedAuthority> collections = new ArrayList<>();
		collections.add(() -> {
			return userdto.getRole().name();
		});
		
		return collections;
	}
	
	// get Username 메서드 (생성한 User의 loingID 사용)
	@Override
	public String getUsername() {
		return userdto.getU_id();
	}
	
	
	// get Password 메서드
	@Override
	public String getPassword() {
		return userdto.getU_pw();
	}
	

	public String getUserNameReal() {
	    return userdto.getU_name();
	}
	
	// 계정이 만료 되었는지 (true: 만료x)
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}
	
	// 계정이 잠겼는지 (true: 잠기지 않음)
	@Override
	public boolean isAccountNonLocked() {
		return true;
	}
	
	// 비밀번호가 만료되었는지 (true: 만료 x)
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}
	
	// 계정이 활성화(사용가능)인지 (true: 활성화)
	@Override
	public boolean isEnabled() {
		return true;
	}
	
	
}
