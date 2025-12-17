package com.test.ocr.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.test.ocr.DTO.ReceiptDTO;

@Mapper
public interface ReceiptAnalyzeMapper {

    // 0) 영수증 저장
    void insertReceipt(ReceiptDTO receipt);

    // 1) 일별 지출
    List<Map<String, Object>> selectDailyTotalByUser(@Param("uId") String uId);

    // 2) 주별 지출
    List<Map<String, Object>> selectWeeklyTotalByUser(@Param("uId") String uId);

    // 3) 월별 지출
    List<Map<String, Object>> selectMonthlyTotalByUser(@Param("uId") String uId);

    // 4) 성별별 지출
    List<Map<String, Object>> selectTotalByGender();

    // 5) 나의 평균
    int selectMyAverage(@Param("uId") String uId);

    // 6) 전체 평균
    int selectAllAverage();
}
