package com.test.ocr.service;


import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.test.ocr.DTO.ItemDTO;
import com.test.ocr.DTO.ReceiptDTO;
import com.test.ocr.DTO.SaveDTO;
import com.test.ocr.DTO.UserDTO;
import com.test.ocr.mapper.ReceiptMapper;
import com.test.ocr.mapper.UserMapper;

import lombok.AllArgsConstructor;

@org.springframework.stereotype.Service
@AllArgsConstructor
public class ServiceIMP implements Service {
	
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ReceiptMapper receiptMapper;
    
    
    private static final Logger log = LoggerFactory.getLogger(ServiceIMP.class);
    @Override
    public void signup(UserDTO u) {
        System.out.println("DB 등록 시도: " + u);
        userMapper.signup(u);
        System.out.println("DB 등록 완료");
    }

    @Override
    public UserDTO findUser(String u_id) {
        return userMapper.findUser(u_id);
    }

    public List<ReceiptDTO> getReceiptsByMonth(String yearMonth, String user_id) {
        return receiptMapper.getReceiptsByMonth(yearMonth, user_id);
    }

    public List<ItemDTO> findItemsByDate(int day, List<ReceiptDTO> receipts) {
        List<ItemDTO> items = new ArrayList<>();

        for (ReceiptDTO receipt : receipts) {
            if (receipt.getR_date().getDayOfMonth() == day) {
                if (receipt.getItems() != null) {
                    items.addAll(receipt.getItems());
                }
            }
        }
        return items;
    }

    @Transactional
    public void save(SaveDTO dto) {

        if (dto.getItemNames() == null || dto.getItemPrices() == null || dto.getItemCounts() == null) {
            log.warn("SAVE 실패: 리스트 값이 비어있음 dto={}", dto);
            return;
        }

        // 리스트 → 문자열 변환
        dto.setItems_name(String.join(",", dto.getItemNames()));
        dto.setItems_price(dto.getItemPrices().stream().map(String::valueOf).collect(Collectors.joining(",")));
        dto.setItems_count(dto.getItemCounts().stream().map(String::valueOf).collect(Collectors.joining(",")));

        log.warn("DB 저장 데이터 = {}", dto);

        receiptMapper.insertReceipt(dto);
    }
    
    public List<SaveDTO> getSavedReceipts(String user_id) {

        List<SaveDTO> list = userMapper.getSavedReceipts(user_id);

        for (SaveDTO dto : list) {

            dto.setItemNames(Arrays.asList(dto.getItems_name().split(",")));

            dto.setItemPrices(
                Arrays.stream(dto.getItems_price().split(","))
                    .map(Integer::valueOf)
                    .collect(Collectors.toList())
            );

            dto.setItemCounts(
                Arrays.stream(dto.getItems_count().split(","))
                    .map(Integer::valueOf)
                    .collect(Collectors.toList())
            );
        }

        return list;
    }
    
    

    public List<SaveDTO> getSavedReceiptsDate(String user_id, String yearMonth) {
        return userMapper.getSavedReceiptsDate(user_id, yearMonth);
    }
    
    // 1) 달력용 날짜 리스트
    public List<Integer> buildCalendar(String yearMonth) {

        YearMonth ym;
        if (yearMonth == null || yearMonth.isBlank()) {
            ym = YearMonth.now();
        } else {
            ym = YearMonth.parse(yearMonth);  // "2025-12"
        }

        int lastDay = ym.lengthOfMonth();

        List<Integer> days = new ArrayList<>();
        for (int i = 1; i <= lastDay; i++) {
            days.add(i);
        }
        return days;
    }

    // 2) 해당 월 + 유저의 영수증 전체 조회
    public List<SaveDTO> getMonthData(String yearMonth, String userId) {
        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDate start = ym.atDay(1);
        LocalDate end   = ym.atEndOfMonth();

        List<SaveDTO> list = receiptMapper.selectByPeriod(start, end, userId);
        log.warn("getMonthData: {}, {}, size = {}", start, end, list.size());
        log.warn("getMonthData list = {}", list);
        return list;
    }

 // 🔹 리턴 타입 바꾸기
    public Map<String, List<String>> buildMenuMap(List<SaveDTO> list) {

        Map<String, List<String>> result = new HashMap<>();

        for (SaveDTO dto : list) {

            if (dto == null) {
                log.warn("buildMenuMap: dto is null, skip");
                continue;
            }
            if (dto.getS_date() == null) {
                log.warn("buildMenuMap: s_date is null, dto = {}", dto);
                continue;
            }

            // 🔹 날짜를 "5", "10" 이런 문자열로 통일
            String dayKey = String.valueOf(dto.getS_date().getDayOfMonth());
            List<String> menus = result.computeIfAbsent(dayKey, k -> new ArrayList<>());

            String namesStr  = dto.getItems_name();
            String pricesStr = dto.getItems_price();
            String countsStr = dto.getItems_count();

            if (namesStr == null || namesStr.isBlank()) {
                continue;
            }

            String[] names  = namesStr.split(",");
            String[] prices = (pricesStr != null) ? pricesStr.split(",") : new String[0];
            String[] counts = (countsStr != null) ? countsStr.split(",") : new String[0];

            for (int i = 0; i < names.length; i++) {
                String name  = names[i].trim();
                String price = (i < prices.length) ? prices[i].trim() : "";
                String cnt   = (i < counts.length) ? counts[i].trim() : "1";

                String display = name
                        + " (" + cnt + "개) - "
                        + price + "원"
                        + " (" + dto.getS_shop() + ")";

                menus.add(display);
            }
        }

        log.warn("buildMenuMap: list size = {}", list.size());
        log.warn("buildMenuMap: result keySet = {}", result.keySet());
        return result;
    }
    
	 // 4) 특정 날짜 상세 내역 (하단 디테일 영역용)
	//  yearMonth : "2025-12" 이런 형식
	//  day       : 1 ~ 31
	//  userId    : 로그인한 유저 아이디
	public List<SaveDTO> getDayDetail(String yearMonth, int day, String user_id) {
	
	  // 일단 해당 달 전체 데이터를 가져오고
	  List<SaveDTO> monthList = getMonthData(yearMonth, user_id);
	
	  // 그 중에서 day 에 해당하는 것만 골라서 리턴
	  List<SaveDTO> result = new ArrayList<>();
	
	  for (SaveDTO dto : monthList) {
	      if (dto.getS_date() == null) {
	          continue;
	      }
	
	      if (dto.getS_date().getDayOfMonth() == day) {
	          result.add(dto);
	      }
	  }
	
	  log.warn("getDayDetail: yearMonth={}, day={}, userId={}, size={}",
	          yearMonth, day, user_id, result.size());
	
	  return result;
	}

    
}
