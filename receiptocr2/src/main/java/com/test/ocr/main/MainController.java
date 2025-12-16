package com.test.ocr.main;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.test.ocr.DTO.ReceiptSaveDTO;

@Controller
public class MainController {

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

    @GetMapping("/analysisPage")
    public String analysisPage() {
        return "user/analysisPage"; // templates/user/analysisPage.html
    }
}
