package com.test.ocr.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.test.ocr.mapper.ReceiptAnalyzeMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReceiptAnalyzeService {

    // Mapper 의존성 주입
	private final ReceiptAnalyzeMapper mapper;

	// 사용자 일일 소비 데이터 조회
	public List<Map<String, Object>> getDaily(String uId) {
		return mapper.selectDailyTotalByUser(uId);
	}

	 // 사용자 주간 소비 데이터 조회
	public List<Map<String, Object>> getWeekly(String uId) {
		return mapper.selectWeeklyTotalByUser(uId);
	}

	  // 사용자 월간 소비 데이터 조회
	public List<Map<String, Object>> getMonthly(String uId) {
		return mapper.selectMonthlyTotalByUser(uId);
	}

	// 전체 성별별 소비 데이터 조회
	public List<Map<String, Object>> getGender() {
		return mapper.selectTotalByGender();
	}

	// 사용자 개인 평균 소비 금액 조회
	public int getMyAvg(String uId) {
		return mapper.selectMyAverage(uId);
	}


    // 전체 사용자 평균 소비 금액 조회
	public int getAllAvg() {
		return mapper.selectAllAverage();
	}
}
