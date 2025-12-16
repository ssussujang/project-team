package com.test.ocr.DTO;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class ReceiptDTO {

	private Long r_no;			// 영수증 번호
    private String r_u;     	// 어떤 사용자의 영수증인지
    private String r_place;     // 가게 이름
    private String r_price;		// 금액
    private LocalDate r_date;   // 날짜
    private String r_goods;    // r_goods : 상품명
    private String category;  // category : 카테고리
    private Gender gender;		//  enum : 성별

    public enum Gender {
        MALE, FEMALE
    }
    
    private List<ItemDTO> items;
    
    
}
