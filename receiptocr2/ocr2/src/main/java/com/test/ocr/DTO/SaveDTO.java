package com.test.ocr.DTO;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class SaveDTO {

    private int s_id;

    // ★ 필드 이름을 user_id 로 사용
    private String user_id;

    private String s_shop;
    private String s_adrr;
    private LocalDate s_date;

    private String items_name;
    private String items_price;
    private String items_count;

    // save()에서 쓰는 리스트들
    private List<String> itemNames;
    private List<Integer> itemPrices;
    private List<Integer> itemCounts;
}







