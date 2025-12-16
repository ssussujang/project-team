package com.test.ocr.service;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.test.ocr.DTO.ItemDTO;
import com.test.ocr.DTO.ReceiptDTO;

import jakarta.annotation.PostConstruct;

@Service
public class OcrService {

    @Value("${clova.url}")
    private String clovaUrl;

    @Value("${clova.secret}")
    private String clovaSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    public void initImageIO() {
        // Spring Boot fat-jar 환경에서 플러그인 스캔이 늦게 잡히는 경우 대비
        ImageIO.scanForPlugins();

        boolean webpReadable = ImageIO.getImageReadersByFormatName("webp").hasNext();
        System.out.println("[ImageIO] webp reader available = " + webpReadable);
    }

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

            // 2) 바이트 읽기
            byte[] bytes = file.getBytes();

            // 3) 실제 파일 포맷 감지(매직바이트 기준)
            String actual = detectFormatByMagic(bytes);

            System.out.println("[UPLOAD] name=" + file.getOriginalFilename()
                    + ", contentType=" + file.getContentType()
                    + ", actual=" + actual
                    + ", size=" + bytes.length
                    + ", head=" + toHex(bytes, 16));

            // 4) WEBP면 JPG로 변환 (CLOVA는 jpg/png가 가장 안전)
            String formatForClova = actual;
            if ("webp".equals(actual)) {
                System.out.println("[CONVERT] WEBP detected. Converting to JPG... name=" + file.getOriginalFilename());
                bytes = convertWebpToJpg(bytes);
                formatForClova = "jpg";
            }

            // 5) 최종 검증 (CLOVA로 보내는 실제 바이트 기준)
            validateImage(bytes, formatForClova);

            // 6) Base64
            String base64 = Base64.getEncoder().encodeToString(bytes);

            // 7) JSON 바디 구성
            JSONObject body = new JSONObject();
            body.put("version", "V2");
            body.put("requestId", UUID.randomUUID().toString());
            body.put("timestamp", System.currentTimeMillis());

            JSONObject image = new JSONObject();
            image.put("format", formatForClova);
            image.put("name", "receipt");
            image.put("data", base64);

            JSONArray images = new JSONArray();
            images.put(image);
            body.put("images", images);

            // 8) 헤더
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-OCR-SECRET", secret);

            System.out.println("[CLOVA REQ] format=" + formatForClova
                    + ", name=" + file.getOriginalFilename()
                    + ", ct=" + file.getContentType()
                    + ", base64Len=" + base64.length());

            HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);

            // 9) 호출
            ResponseEntity<String> response = restTemplate.postForEntity(uri, request, String.class);

            String respBody = response.getBody();
            System.out.println("[CLOVA JSON] status=" + response.getStatusCode());
            System.out.println("[CLOVA JSON] resp=" + respBody);

            return (respBody == null || respBody.isBlank()) ? null : new JSONObject(respBody);

        } catch (HttpClientErrorException e) {
            System.out.println("[CLOVA JSON] HTTP ERROR status=" + e.getStatusCode());
            System.out.println("[CLOVA JSON] HTTP ERROR body=" + e.getResponseBodyAsString());
            e.printStackTrace();
            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ====== WEBP -> JPG 변환 ======
    private byte[] convertWebpToJpg(byte[] webpBytes) {
        try {
            BufferedImage src = ImageIO.read(new ByteArrayInputStream(webpBytes));
            if (src == null) {
                throw new IllegalArgumentException("WEBP 디코딩 실패: ImageIO가 WEBP를 읽지 못했습니다. (WEBP ImageIO 의존성 필요)");
            }

            // JPG는 알파(투명) 지원이 없으므로 RGB로 변환
            BufferedImage rgb = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = rgb.createGraphics();
            g.drawImage(src, 0, 0, null);
            g.dispose();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            boolean ok = ImageIO.write(rgb, "jpg", out);
            if (!ok) throw new IllegalArgumentException("JPG 인코딩 실패: ImageIO.write가 false 반환");

            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("WEBP → JPG 변환 실패: " + e.getMessage(), e);
        }
    }

    // ====== 실제 포맷(매직바이트) 감지 ======
    private String detectFormatByMagic(byte[] bytes) {
        if (bytes == null || bytes.length < 12) return "unknown";

        // JPEG: FF D8 FF
        boolean isJpg = (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF;
        if (isJpg) return "jpg";

        // PNG: 89 50 4E 47 0D 0A 1A 0A
        boolean isPng = (bytes[0] & 0xFF) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47
                && (bytes[4] & 0xFF) == 0x0D && (bytes[5] & 0xFF) == 0x0A && (bytes[6] & 0xFF) == 0x1A && (bytes[7] & 0xFF) == 0x0A;
        if (isPng) return "png";

        // WEBP: "RIFF" .... "WEBP"
        boolean isRiff = bytes[0] == 0x52 && bytes[1] == 0x49 && bytes[2] == 0x46 && bytes[3] == 0x46; // RIFF
        boolean isWebp = bytes[8] == 0x57 && bytes[9] == 0x45 && bytes[10] == 0x42 && bytes[11] == 0x50; // WEBP
        if (isRiff && isWebp) return "webp";

        return "unknown";
    }

    // ====== 최종 검증 ======
    private void validateImage(byte[] bytes, String format) {
        if (bytes == null || bytes.length < 8) {
            throw new IllegalArgumentException("이미지 파일이 너무 작거나 비어있음");
        }
        if ("jpg".equals(format)) {
            boolean isJpg = (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF;
            if (!isJpg) throw new IllegalArgumentException("최종 전송 포맷이 JPG인데, 바이트 시그니처가 JPG가 아닙니다.");
        } else if ("png".equals(format)) {
            boolean isPng = (bytes[0] & 0xFF) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47
                    && (bytes[4] & 0xFF) == 0x0D && (bytes[5] & 0xFF) == 0x0A && (bytes[6] & 0xFF) == 0x1A && (bytes[7] & 0xFF) == 0x0A;
            if (!isPng) throw new IllegalArgumentException("최종 전송 포맷이 PNG인데, 바이트 시그니처가 PNG가 아닙니다.");
        } else {
            throw new IllegalArgumentException("CLOVA 전송 포맷은 jpg/png만 허용하도록 처리 중입니다. format=" + format);
        }
    }

    private static String toHex(byte[] b, int n) {
        if (b == null) return "null";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(n, b.length); i++) {
            sb.append(String.format("%02X ", b[i]));
        }
        return sb.toString().trim();
    }

    // ====== 이하 parseReceipt는 네 코드 그대로 (변경 없음) ======
    public ReceiptDTO parseReceipt(JSONObject json) {
        if (json == null) return new ReceiptDTO();

        ReceiptDTO dto = new ReceiptDTO();

        try {
            JSONObject result = json
                    .getJSONArray("images")
                    .getJSONObject(0)
                    .getJSONObject("receipt")
                    .getJSONObject("result");

            dto.setR_shop(optText(result, "storeInfo", "name"));

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

            try {
                String dateStr = optText(result, "paymentInfo", "date");
                dto.setR_date(parseLocalDateFlexible(dateStr));
            } catch (Exception ignore) {
                dto.setR_date(null);
            }

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
