package com.verar.oauth2login.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller  // Change to @Controller instead of @RestController
public class UserController {

    @GetMapping("/user-info")
    public String getUserInfo(@AuthenticationPrincipal OAuth2User principal, Model model) {
        if (principal != null) {
            model.addAttribute("userName", principal.getAttribute("name"));
            model.addAttribute("userEmail", principal.getAttribute("email"));
        }
        return "user-info"; // Redirects to user-info.html in templates
    }
}
