package com.verar.oauth2login.controller;

import com.verar.oauth2login.service.GoogleContactsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UiController {

    private final GoogleContactsService googleContactsService;

    public UiController(GoogleContactsService googleContactsService) {
        this.googleContactsService = googleContactsService;
    }

    @GetMapping("/contacts")
    public String getContacts(Model model, OAuth2AuthenticationToken authentication) {
        model.addAttribute("userName", authentication.getPrincipal().getAttribute("name"));
        model.addAttribute("userEmail", authentication.getPrincipal().getAttribute("email"));
        model.addAttribute("contacts", googleContactsService.getContacts(authentication));
        return "contacts";
    }
}