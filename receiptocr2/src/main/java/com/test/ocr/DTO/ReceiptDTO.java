package com.test.ocr.DTO;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class ReceiptDTO {

    private Long r_id;          // 영수증 ID (PK)
    private String user_id;     // 어떤 사용자의 영수증인지
    private String r_shop;      // 가게 이름
    private String r_adrr;      // 주소
    private LocalDate r_date;   // 날짜 
    private int r_total;        // 총 금액
    private List<ItemDTO> items;
    
    
}
