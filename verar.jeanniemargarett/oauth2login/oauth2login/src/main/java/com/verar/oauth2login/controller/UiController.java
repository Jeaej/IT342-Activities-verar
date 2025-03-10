package com.verar.oauth2login.controller;

import com.verar.oauth2login.service.GooglePeopleService;
import com.google.api.services.people.v1.model.Person;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;

@Controller
public class UiController {

    private final GooglePeopleService googlePeopleService;

    // Constructor to initialize the GooglePeopleService
    public UiController(GooglePeopleService googlePeopleService) {
        this.googlePeopleService = googlePeopleService;
    }

    // Display the list of contacts
    @GetMapping("/contacts")
    public String showContacts(Model model) {
        try {
            List<Person> contacts = googlePeopleService.getContacts();
            model.addAttribute("contacts", contacts);
            return "contacts";
        } catch (IOException e) {
            model.addAttribute("error", "Failed to fetch contacts.");
            return "error";
        }
    }

    // Show the form to add a contact
    @GetMapping("/addcontact")
    public String showAddContactForm() {
        return "addcontact";
    }

    // Show the form to edit a contact
    @GetMapping("/editcontact")
    public String showEditContactForm(@RequestParam String resourceName, Model model) {
        try {
            Person contact = googlePeopleService.getContact(resourceName);
            if (contact == null) {
                model.addAttribute("error", "Contact not found.");
                return "error";
            }
            String etag = contact.getMetadata().getSources().get(0).getEtag();
            model.addAttribute("contact", contact);
            model.addAttribute("etag", etag);
            return "editcontact";
        } catch (IOException e) {
            model.addAttribute("error", "Failed to fetch contact details.");
            return "error";
        }
    }

    // Display user information
    @GetMapping("/user-info")
    public String userInfo(Authentication authentication, Model model) {
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
            model.addAttribute("userName", oauthUser.getAttribute("name"));
            model.addAttribute("userEmail", oauthUser.getAttribute("email"));
            model.addAttribute("userPicture", oauthUser.getAttribute("picture"));
        }
        return "user-info";
    }
}
