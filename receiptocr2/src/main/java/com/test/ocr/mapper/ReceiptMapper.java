package com.test.ocr.mapper;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.test.ocr.DTO.ReceiptDTO;
import com.test.ocr.DTO.SaveDTO;

@Mapper
public interface ReceiptMapper {

    List<ReceiptDTO> getReceiptsByMonth(@Param("yearMonth") String yearMonth,
                                        @Param("user_id") String user_id);

    List<SaveDTO> selectByPeriod(@Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("userId") String userId);

    void insertReceipt(SaveDTO dto);
}