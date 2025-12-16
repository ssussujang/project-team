package com.test.ocr.receipt;

import java.security.Principal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.test.ocr.DTO.DayDTO;
import com.test.ocr.DTO.ItemDTO;
import com.test.ocr.DTO.ReceiptDTO;
import com.test.ocr.DTO.SaveDTO;
import com.test.ocr.DTO.SaveForm;
import com.test.ocr.service.OcrService;
import com.test.ocr.service.ServiceIMP;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Controller
@RequestMapping("/receipt")
public class ReceiptController {
	
	@Autowired
	private OcrService ocrService;
	
	@Autowired
	private ServiceIMP service;
	
	
	
	
	@PostMapping("/uploadReceipt")
	public String uploadReceipt(@RequestParam("receipt") List<MultipartFile> files, Model model) {

	    if (files == null || files.isEmpty()) {
	        model.addAttribute("msg", "파일을 선택해 주세요.");
	        return "home";
	    }

	    List<SaveDTO> list = new ArrayList<>();

	    for (MultipartFile file : files) {
	        if (file == null || file.isEmpty() || file.getSize() == 0) {
	            log.warn("빈 파일 스킵됨");
	            continue; // ⭐ 빈 파일은 건너뛰기
	        }

	        try {
	            JSONObject json = ocrService.callClovaOCR(file);
	            ReceiptDTO receipt = ocrService.parseReceipt(json);

	            SaveDTO dto = new SaveDTO();
	            dto.setS_shop(receipt.getR_shop());
	            dto.setS_adrr(receipt.getR_adrr());
	            dto.setS_date(receipt.getR_date());

	            List<String> names = new ArrayList<>();
	            List<Integer> prices = new ArrayList<>();
	            List<Integer> counts = new ArrayList<>();

	            // ✅ 메뉴가 없으면 스킵하지 말고 "빈 값"으로라도 DTO를 list에 넣기
	            if (receipt.getItems() == null || receipt.getItems().isEmpty()) {
	                log.warn("인식된 메뉴 없음: {}", file.getOriginalFilename());

	                dto.setItemNames(names);   // 빈 리스트
	                dto.setItemPrices(prices); // 빈 리스트
	                dto.setItemCounts(counts); // 빈 리스트

	                list.add(dto);
	                continue; // 다음 파일 처리
	            }

	            // ✅ 메뉴가 있으면 기존처럼 채움
	            for (ItemDTO item : receipt.getItems()) {
	                names.add(item.getI_name());
	                prices.add(item.getI_price());
	                counts.add(item.getI_count());
	            }

	            dto.setItemNames(names);
	            dto.setItemPrices(prices);
	            dto.setItemCounts(counts);

	            list.add(dto);

	        } catch (Exception e) {
	            log.error("OCR 실패 파일: {}", file.getOriginalFilename(), e);
	            // 실패한 파일만 스킵하고 계속
	        }
	    }

	    if (list.isEmpty()) {
	        model.addAttribute("msg", "유효한 영수증 파일이 없거나 인식에 실패했습니다.");
	        return "home";
	    }

	    SaveForm saveForm = new SaveForm();
	    saveForm.setReceipts(list);

	    model.addAttribute("saveForm", saveForm);
	    model.addAttribute("mode", "many");
	    return "home";
	}


	
	@GetMapping("/writeReceipt")
	public String writeReceiptForm(Model model, Principal principal) {

	    // 1) 빈 receipt 객체 생성
	    ReceiptDTO receipt = new ReceiptDTO();
	    receipt.setR_shop("");   // 가게
	    receipt.setR_adrr("");   // 주소
	    receipt.setR_date(LocalDate.now());   // 날짜 (문자열이면 "" / LocalDate면 LocalDate.now() 등등)

	    // 2) 상품 정보도 "빈 줄" 하나 만들어서 넣기
	    List<ItemDTO> items = new ArrayList<>();

	    ItemDTO item = new ItemDTO();
	    item.setI_name("");     // 상품명
	    item.setI_price(0);  // 가격 (Integer면 null로 두면 input에는 빈칸으로 나옴)
	    item.setI_count(0);  // 수량
	    items.add(item);

	    receipt.setItems(items);

	    // 3) 모델에 넣기 (view에서 쓰는 이름이 지금 'receipt' 니까 그대로 맞춤)
	    model.addAttribute("receipt", receipt);

	    // 필요하면 yearMonth 같은 것도 같이 넣어줘도 됨
	    // model.addAttribute("yearMonth", ...);

	    // 4) 기존에 이 폼이 있는 html (home or info) 리턴
	    System.out.println("테스트");
	    return "home";   // 현재 저 테이블이 있는 템플릿 이름으로 바꿔줘
	}

	
	
	
	@PostMapping("/mypage/data")
	public String loadMonthData(@RequestParam("yearMonth") String yearMonth, Model model, 
	                            Principal principal) {

	    String userId = principal.getName();   // 로그인한 유저 ID
	    List<ReceiptDTO> receipts = service.getReceiptsByMonth(yearMonth, userId);

	    model.addAttribute("receipts", receipts);
	    model.addAttribute("yearMonth", yearMonth);

	    return "info";
	}
	
	
	public List<List<DayDTO>> buildCalendar(String yearMonth, List<ReceiptDTO> receipts) {

	    YearMonth ym = YearMonth.parse(yearMonth);
	    int lastDay = ym.lengthOfMonth();
	    LocalDate firstDay = ym.atDay(1);

	    int firstWeek = firstDay.getDayOfWeek().getValue();  // 월=1 ~ 일=7

	    List<List<DayDTO>> calendar = new ArrayList<>();
	    List<DayDTO> week = new ArrayList<>(Collections.nCopies(7, null));

	    int dayIndex = firstWeek % 7;
	    for (int day = 1; day <= lastDay; day++) {

	    	DayDTO daydto = new DayDTO(); 
	        daydto.setDay(day);
	        daydto.setItems(service.findItemsByDate(dayIndex, receipts));

	        week.set(dayIndex, daydto);

	        dayIndex++;
	        if (dayIndex == 7) {
	            calendar.add(week);
	            week = new ArrayList<>(Collections.nCopies(7, null));
	            dayIndex = 0;
	        }
	    }

	    calendar.add(week);
	    return calendar;
	}

	@PostMapping("/save")   // 클래스에 @RequestMapping("/receipt") 있으면 실제 경로 /receipt/save
	public String save(@ModelAttribute("saveForm") SaveForm saveForm,
	                   Principal principal) {

	    String loginId = (principal != null) ? principal.getName() : null;

	    // 여러 영수증 반복
	    for (SaveDTO dto : saveForm.getReceipts()) {
	        dto.setUser_id(loginId);   // 로그인 아이디 세팅
	        service.save(dto);         // ServiceIMP.save(SaveDTO) 그대로 사용
	    }

	    return "redirect:/";
	}
	
	@PostMapping("/saveOne")
	public String saveOne(
	        @RequestParam("s_shop") String s_shop,
	        @RequestParam("s_adrr") String s_adrr,
	        @RequestParam("s_date") String s_date,

	        @RequestParam(value="itemNames", required=false) List<String> itemNames,
	        @RequestParam(value="itemPrices", required=false) List<String> itemPrices,
	        @RequestParam(value="itemCounts", required=false) List<String> itemCounts,

	        Principal principal
	) {
	    String userId = principal.getName();

	    if (itemNames == null) itemNames = new ArrayList<>();
	    if (itemPrices == null) itemPrices = new ArrayList<>();
	    if (itemCounts == null) itemCounts = new ArrayList<>();

	    // ✅ 빈 상품명 제거(필요하면)
	    // itemNames = itemNames.stream().filter(s -> s != null && !s.isBlank()).toList();

	    // 문자열 -> 숫자 안전 파싱
	    List<Integer> prices = new ArrayList<>();
	    for (String p : itemPrices) prices.add(parseIntSafe(p, 0));

	    List<Integer> counts = new ArrayList<>();
	    for (String c : itemCounts) counts.add(parseIntSafe(c, 1));

	    SaveDTO dto = new SaveDTO();
	    dto.setUser_id(userId);
	    dto.setS_shop(s_shop);
	    dto.setS_adrr(s_adrr);
	    dto.setS_date(parseDateSafe(s_date)); // 아래 함수 사용

	    dto.setItemNames(itemNames);
	    dto.setItemPrices(prices);
	    dto.setItemCounts(counts);

	    service.save(dto);
	    return "home";
	}

	// 숫자 안전 파싱
	private int parseIntSafe(String s, int def) {
	    if (s == null) return def;
	    String onlyNum = s.replaceAll("[^0-9]", "");
	    if (onlyNum.isEmpty()) return def;
	    try { return Integer.parseInt(onlyNum); }
	    catch (Exception e) { return def; }
	}

	// 날짜 안전 파싱: "yyyy-MM-dd" 또는 "yy. MM. dd." 둘 다 처리
	private LocalDate parseDateSafe(String s) {
	    if (s == null) return LocalDate.now();

	    s = s.trim();

	    // 1) 2025-12-16
	    try { return LocalDate.parse(s); } catch (Exception ignored) {}

	    // 2) 25. 11. 11.
	    try {
	        DateTimeFormatter f = DateTimeFormatter.ofPattern("yy. MM. dd.");
	        return LocalDate.parse(s, f);
	    } catch (Exception ignored) {}

	    return LocalDate.now();
	}




	
	
}
