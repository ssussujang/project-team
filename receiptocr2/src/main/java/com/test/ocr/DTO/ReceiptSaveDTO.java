package com.test.ocr.DTO;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ReceiptSaveDTO {

	@NotBlank(message = "가게 이름을 입력해주세요.")
    @Size(max = 50, message = "가게 이름은 50자 이내로 입력해주세요.")
    private String s_shop;

    @NotBlank(message = "주소를 입력해주세요.")
    @Size(max = 100, message = "주소는 100자 이내로 입력해주세요.")
    private String s_adrr;

    @NotNull(message = "날짜를 입력해주세요.")
    private LocalDate s_date;

    // 최소 1개 이상
    @NotEmpty(message = "상품 이름을 최소 1개 이상 입력해주세요.")
    private List<@NotBlank(message = "상품 이름을 입력해주세요.") String> itemNames;

    @NotEmpty(message = "상품 가격을 최소 1개 이상 입력해주세요.")
    private List<@NotNull(message = "가격을 입력해주세요.")
                 @PositiveOrZero(message = "가격은 0 이상이어야 합니다.") Integer> itemPrices;

    @NotEmpty(message = "상품 수량을 최소 1개 이상 입력해주세요.")
    private List<@NotNull(message = "수량을 입력해주세요.")
                 @PositiveOrZero(message = "수량은 0 이상이어야 합니다.") Integer> itemCounts;
    
    
}
