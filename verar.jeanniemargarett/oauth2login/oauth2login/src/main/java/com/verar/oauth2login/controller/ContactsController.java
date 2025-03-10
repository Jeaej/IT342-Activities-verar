package com.verar.oauth2login.controller;

import java.io.IOException;
import java.util.List;

import com.verar.oauth2login.service.GooglePeopleService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.api.services.people.v1.model.Person;

@Controller
@RequestMapping("/api/contacts")
public class ContactsController {

    private final GooglePeopleService googlePeopleService;

    public ContactsController(GooglePeopleService googlePeopleService) {
        this.googlePeopleService = googlePeopleService;
    }

    // Fetch the list of contacts
    @GetMapping
    @ResponseBody
    public List<Person> getContacts() throws IOException {
        return googlePeopleService.getContacts();
    }

    // Add a new contact
    @PostMapping("/add")
    public String addContact(@RequestParam String firstName,
                             @RequestParam String lastName,
                             @RequestParam List<String> emails,
                             @RequestParam List<String> phoneNumbers,
                             RedirectAttributes redirectAttributes) {
        try {
            googlePeopleService.addContact(firstName, lastName, emails, phoneNumbers);
            redirectAttributes.addFlashAttribute("message", "Contact added successfully!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to add contact.");
        }
        return "redirect:/contacts";
    }

    // Update an existing contact
    @PostMapping("/update")
    public String updateContact(@RequestParam String resourceName,
                                @RequestParam String firstName,
                                @RequestParam String lastName,
                                @RequestParam List<String> emails,
                                @RequestParam List<String> phoneNumbers,
                                RedirectAttributes redirectAttributes) {
        try {
            googlePeopleService.updateContact(resourceName, firstName, lastName, emails, phoneNumbers);
            redirectAttributes.addFlashAttribute("message", "Contact updated successfully!");
        } catch (IOException e) {
            if (e.getMessage().contains("etag")) {
                redirectAttributes.addFlashAttribute("error", "Contact was modified elsewhere. Reload and try again.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to update contact.");
            }
        }
        return "redirect:/contacts";
    }

    // Delete a contact
    @PostMapping("/delete")
    public String deleteContact(@RequestParam String resourceName, RedirectAttributes redirectAttributes) {
        try {
            googlePeopleService.deleteContact(resourceName);
            redirectAttributes.addFlashAttribute("message", "Contact deleted successfully!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete contact.");
        }
        return "redirect:/contacts";
    }
}
