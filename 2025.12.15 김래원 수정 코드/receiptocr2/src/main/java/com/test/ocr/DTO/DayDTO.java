package com.test.ocr.DTO;

import java.util.List;

import lombok.Data;

@Data
public class DayDTO {
    private int day;
    private List<ItemDTO> items;
}