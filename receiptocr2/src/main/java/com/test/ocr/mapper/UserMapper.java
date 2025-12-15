package com.test.ocr.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.test.ocr.DTO.SaveDTO;
import com.test.ocr.DTO.UserDTO;

@Mapper
public interface UserMapper {

	void signup(UserDTO u);

    UserDTO findUser(String u_id);


    void insertSave(
        @Param("user_id") String user_id,
        @Param("s_shop") String shop,
        @Param("s_adrr") String addr,
        @Param("s_date") String date,
        @Param("items_name") String names,
        @Param("items_price") String prices,
        @Param("items_count") String counts
    );
    
    List<SaveDTO> getSavedReceipts(@Param("user_id") String user_id);
    
    
    List<SaveDTO> getSavedReceiptsDate(
        @Param("user_id") String user_id,
        @Param("yearMonth") String yearMonth
    );
	
}
