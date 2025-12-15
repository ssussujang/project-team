package com.test.ocr.service;

import com.test.ocr.DTO.SaveDTO;
import com.test.ocr.DTO.UserDTO;

public interface Service {
	void signup (UserDTO u);
	UserDTO findUser (String u_id);
	void save(SaveDTO dto);
}


