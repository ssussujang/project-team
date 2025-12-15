package com.test.ocr.service;

import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.test.ocr.DTO.SaveDTO;
import com.test.ocr.mapper.ReceiptMapper;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@org.springframework.stereotype.Service
public class ReceiptService {

	@Value("${clova.secret}")
    private String secretKey;

    @Value("${clova.url}")
    private String apiUrl;
    
    public JSONObject callClovaOCR(MultipartFile file) throws Exception {

        OkHttpClient client = new OkHttpClient().newBuilder().build();

        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                        "file",
                        file.getOriginalFilename(),
                        RequestBody.create(MediaType.parse("image/jpeg"), file.getBytes())
                )
                .build();

        Request request = new Request.Builder()
                .url(apiUrl)
                .header("X-OCR-SECRET", secretKey)
                .post(body)
                .build();

        Response response = client.newCall(request).execute();
        String result = response.body().string();

        return new JSONObject(result);
    }
    
    
    @Autowired
    private ReceiptMapper mapper;

    @Transactional
    public void saveReceipt(SaveDTO dto) {

        // 1) 리스트 꺼내기
        List<String> names  = dto.getItemNames();
        List<Integer> prices = dto.getItemPrices();
        List<Integer> counts = dto.getItemCounts();

        if (names == null || prices == null || counts == null) {
            return; // 방어코드
        }

        // 2) 리스트 → 콤마 문자열로 변환
        String joinedNames = String.join(",", names);

        String joinedPrices = prices.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        

        String joinedCounts = counts.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        // 3) DTO에 세팅
        dto.setItems_name(joinedNames);
        dto.setItems_price(joinedPrices);
        dto.setItems_count(joinedCounts);

        // 4) save 테이블에 한 줄 insert
        mapper.insertReceipt(dto);
    }
    
    
    
    
}

