package com.test.ocr.main;

import java.security.Principal;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.test.ocr.DTO.ReceiptSaveDTO;
import com.test.ocr.mapper.ReceiptAnalyzeMapper;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MainController {

	 // 영수증 분석 Mapper 의존성 주입
    private final ReceiptAnalyzeMapper receiptAnalyzeMapper;

    @GetMapping("/")
    public String ocr(Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (!model.containsAttribute("receiptForm")) {
            model.addAttribute("receiptForm", new ReceiptSaveDTO());
        }

        boolean isLogin = auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal());

        model.addAttribute("isLogin", isLogin);

        return "home";
    }

    // "/analysisPage" 경로로 접근 시 실행되는 메서드
    @GetMapping("/analysisPage")
    public String analysisPage(Model model, Principal principal) {


        // 인증되지 않은 사용자면 로그인 페이지로 리다이렉트
        if (principal == null) return "redirect:/user/login";

        // 현재 로그인한 사용자의 아이디 (username) 가져오기
        String uId = principal.getName();
        
        // 사용자의 일일/주간/월간 소비 데이터 및 전체 성별별 소비 데이터 모델에 추가
        model.addAttribute("dailyData",   receiptAnalyzeMapper.selectDailyTotalByUser(uId));
        model.addAttribute("weeklyData",  receiptAnalyzeMapper.selectWeeklyTotalByUser(uId));
        model.addAttribute("monthlyData", receiptAnalyzeMapper.selectMonthlyTotalByUser(uId));
        model.addAttribute("genderData",  receiptAnalyzeMapper.selectTotalByGender());

        // 사용자 개인 평균 소비, 전체 평균 소비 추가
        model.addAttribute("myAvg",       receiptAnalyzeMapper.selectMyAverage(uId));
        model.addAttribute("allAvg",      receiptAnalyzeMapper.selectAllAverage());

        // 분석 결과를 보여주는 analysisPage.html 뷰 반환
        return "user/analysisPage";
    }
}
