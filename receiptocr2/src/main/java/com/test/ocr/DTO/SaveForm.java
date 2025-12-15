package com.test.ocr.DTO;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class SaveForm {
	private List<SaveDTO> receipts = new ArrayList<>();
}
