package com.test.ocr.service;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;              // ✅ Spring MediaType
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.test.ocr.DTO.ItemDTO;
import com.test.ocr.DTO.ReceiptDTO;

@Service
public class OcrService {

    @Value("${clova.url}")
    private String clovaUrl;

    @Value("${clova.secret}")
    private String clovaSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    public JSONObject callClovaOCR(MultipartFile file) {
        try {
            // 0) 파일 검증
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("업로드된 파일이 비어있습니다.");
            }

            // 1) 설정값 검증
            String url = (clovaUrl == null) ? "" : clovaUrl.trim();
            String secret = (clovaSecret == null) ? "" : clovaSecret.trim();
            if (url.isEmpty()) throw new IllegalStateException("clova.url 설정이 비어있습니다.");
            if (secret.isEmpty()) throw new IllegalStateException("clova.secret 설정이 비어있습니다.");

            URI uri = URI.create(url);

            // 2) format 결정 (CLOVA는 보통 jpg/png만)
            String format = detectFormat(file);

            // 3) JSON 바디 구성 (Base64 포함)
            byte[] bytes = file.getBytes();
            String base64 = Base64.getEncoder().encodeToString(bytes);

            JSONObject body = new JSONObject();
            body.put("version", "V2");
            body.put("requestId", UUID.randomUUID().toString());
            body.put("timestamp", System.currentTimeMillis());

            JSONObject image = new JSONObject();
            image.put("format", format);
            image.put("name", "receipt");
            image.put("data", base64); // ✅ data:image/... 같은 prefix 절대 넣지 말기

            JSONArray images = new JSONArray();
            images.put(image);
            body.put("images", images);

            // 4) 헤더
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-OCR-SECRET", secret);

            HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);

            // 5) 호출
            ResponseEntity<String> response = restTemplate.postForEntity(uri, request, String.class);

            String respBody = response.getBody();
            System.out.println("[CLOVA JSON] status=" + response.getStatusCode());
            System.out.println("[CLOVA JSON] resp=" + respBody);

            return (respBody == null || respBody.isBlank()) ? null : new JSONObject(respBody);

        } catch (HttpClientErrorException e) {
            // ✅ 400/401/403/413 등 원인 파악은 이 바디가 핵심
            System.out.println("[CLOVA JSON] HTTP ERROR status=" + e.getStatusCode());
            System.out.println("[CLOVA JSON] HTTP ERROR body=" + e.getResponseBodyAsString());
            e.printStackTrace();
            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ReceiptDTO parseReceipt(JSONObject json) {
        if (json == null) return new ReceiptDTO();

        ReceiptDTO dto = new ReceiptDTO();

        try {
            JSONObject result = json
                    .getJSONArray("images")
                    .getJSONObject(0)
                    .getJSONObject("receipt")
                    .getJSONObject("result");

            // 가게명
            dto.setR_shop(optText(result, "storeInfo", "name"));

            // 주소
            try {
                JSONObject storeInfo = result.optJSONObject("storeInfo");
                JSONArray addrArr = (storeInfo == null) ? null : storeInfo.optJSONArray("addresses");

                if (addrArr != null && addrArr.length() > 0) {
                    JSONObject addrObj = addrArr.optJSONObject(0);
                    String addr = (addrObj == null) ? "" : addrObj.optString("text", "");

                    if (addr.isEmpty() && addrObj != null) {
                        JSONObject formatted = addrObj.optJSONObject("formatted");
                        if (formatted != null) addr = formatted.optString("value", "");
                    }
                    dto.setR_adrr(addr.isEmpty() ? null : addr);
                } else {
                    dto.setR_adrr(null);
                }
            } catch (Exception ignore) {
                dto.setR_adrr(null);
            }

            // 날짜
            try {
                String dateStr = optText(result, "paymentInfo", "date");
                dto.setR_date(parseLocalDateFlexible(dateStr));
            } catch (Exception ignore) {
                dto.setR_date(null);
            }

            // 총액
            try {
                String totalStr = "";
                JSONObject totalPrice = result.optJSONObject("totalPrice");
                if (totalPrice != null) {
                    JSONObject priceObj = totalPrice.optJSONObject("price");
                    if (priceObj != null) totalStr = priceObj.optString("text", "");
                }
                dto.setR_total(toIntMoney(totalStr));
            } catch (Exception ignore) {
                dto.setR_total(0);
            }

            // 아이템
            List<ItemDTO> itemList = new ArrayList<>();
            try {
                JSONArray subResults = result.optJSONArray("subResults");
                if (subResults != null && subResults.length() > 0) {
                    JSONObject firstBlock = subResults.optJSONObject(0);
                    JSONArray items = (firstBlock == null) ? null : firstBlock.optJSONArray("items");

                    if (items != null) {
                        for (int i = 0; i < items.length(); i++) {
                            JSONObject itemObj = items.optJSONObject(i);
                            if (itemObj == null) continue;

                            ItemDTO item = new ItemDTO();

                            String name = optText(itemObj, "name");
                            item.setI_name(name);

                            int price = 0;
                            JSONObject priceWrap = itemObj.optJSONObject("price");
                            if (priceWrap != null) {
                                JSONObject priceInner = priceWrap.optJSONObject("price");
                                if (priceInner != null) price = toIntMoney(priceInner.optString("text", "0"));
                            }
                            item.setI_price(price);

                            int count = 1;
                            JSONObject countObj = itemObj.optJSONObject("count");
                            if (countObj != null) {
                                String digits = countObj.optString("text", "1").replaceAll("[^0-9]", "");
                                if (!digits.isEmpty()) count = Integer.parseInt(digits);
                            }
                            item.setI_count(count);

                            item.setI_category("미분류");

                            if (name != null && !name.trim().isEmpty()) {
                                itemList.add(item);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            dto.setItems(itemList);

        } catch (Exception e) {
            System.out.println("OCR 파싱 오류: " + e.getMessage());
        }

        return dto;
    }

    // ----------------- helpers -----------------

    private String detectFormat(MultipartFile file) {
        String ct = file.getContentType();

        if ("image/png".equalsIgnoreCase(ct)) return "png";
        if ("image/jpeg".equalsIgnoreCase(ct) || "image/jpg".equalsIgnoreCase(ct)) return "jpg";

        String name = file.getOriginalFilename();
        if (name != null) {
            String lower = name.toLowerCase();
            if (lower.endsWith(".png")) return "png";
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "jpg";
        }
        throw new IllegalArgumentException("지원하지 않는 이미지 타입: contentType=" + ct + ", filename=" + name);
    }

    private String optText(JSONObject obj, String... path) {
        JSONObject cur = obj;
        for (String p : path) {
            if (cur == null) return "";
            cur = cur.optJSONObject(p);
        }
        if (cur == null) return "";
        return cur.optString("text", "");
    }

    private int toIntMoney(String s) {
        if (s == null) return 0;
        String digits = s.replaceAll(",", "").replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return 0;
        try { return Integer.parseInt(digits); } catch (Exception e) { return 0; }
    }

    private LocalDate parseLocalDateFlexible(String dateStr) {
        if (dateStr == null) return null;
        String s = dateStr.trim();
        if (s.isEmpty()) return null;

        try { return LocalDate.parse(s); } catch (Exception ignore) {}
        try { return LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyy.MM.dd")); } catch (Exception ignore) {}
        try { return LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyy/MM/dd")); } catch (Exception ignore) {}
        return null;
    }
}
