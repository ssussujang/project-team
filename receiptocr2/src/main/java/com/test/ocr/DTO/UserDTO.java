package com.test.ocr.DTO;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class UserDTO {
	

	@NotBlank(message = "ID를 입력해주세요.")
	@Size(min=2, max=10, message="ID는 2~10자 사이로 입력해주세요.")
	private String u_id;
	
	@NotBlank(message = "PW를 입력해주세요.")
	@Size(min=2, message="PW는 2글자 이상으로 입력해주세요.")
	private String u_pw;
	
	@NotBlank(message = "이름을 입력해주세요.")
	private String u_name;
	
	@NotBlank(message = "성별을 입력해주세요.")
	private String gender;
	
	@NotBlank(message = "이메일을 입력해주세요.")
	private String u_email;
	
	@NotBlank(message = "생년월일을 입력해주세요(YYMMDD)")
	private String u_birthday;
	
	private Role role;
	private LocalDate u_date;
	
}
