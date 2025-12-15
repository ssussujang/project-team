package com.test.ocr.DTO;

import lombok.Data;

@Data
public class ItemDTO {

    private Long i_id;          // 품목 ID (PK)
    private Long r_id;          // 어떤 영수증에 포함된 품목인지 (FK)
    private String i_name;      
    private int i_price;        // 가격 → INT 추천
    private int i_count;        // 개수 → INT 추천
    private String i_category;  
}
