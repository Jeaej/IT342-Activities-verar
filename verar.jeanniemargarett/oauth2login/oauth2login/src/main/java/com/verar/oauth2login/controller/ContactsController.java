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

    // Endpoint to get the list of contacts
    @GetMapping
    @ResponseBody
    public List<Person> getContacts() throws IOException {
        List<Person> contacts = googlePeopleService.getContacts();
        System.out.println("Fetched Contacts: " + contacts);
        return contacts;
    }

    // Endpoint to add a new contact
    @PostMapping("/add")
    public String addContact(@RequestParam String firstName, @RequestParam String lastName,
                             @RequestParam List<String> emails, @RequestParam List<String> phoneNumbers,
                             RedirectAttributes redirectAttributes) {
        try {
            // Call the service to add the contact
            googlePeopleService.addContact(firstName, lastName, emails, phoneNumbers);
            // Add a success message to the redirect attributes
            redirectAttributes.addFlashAttribute("message", "Contact added successfully!");
            return "redirect:/contacts";
        } catch (IOException e) {
            e.printStackTrace();
            // Add an error message to the redirect attributes
            redirectAttributes.addFlashAttribute("error", "Failed to add contact.");
            return "redirect:/contacts";
        }
    }

    // Endpoint to update an existing contact
    @PostMapping("/update")
    public String updateContact(@RequestParam String resourceName,
                                @RequestParam String firstName,
                                @RequestParam String lastName,
                                @RequestParam List<String> emails,
                                @RequestParam List<String> phoneNumbers,
                                RedirectAttributes redirectAttributes) {
        try {
            // Call the service to update the contact
            googlePeopleService.updateContact(resourceName, firstName, lastName, emails, phoneNumbers);
            // Add a success message to the redirect attributes
            redirectAttributes.addFlashAttribute("message", "Contact updated successfully!");
        } catch (IOException e) {
            e.printStackTrace();
            // Check if the error message contains "etag"
            if (e.getMessage().contains("etag")) {
                // Add a specific error message for etag conflict
                redirectAttributes.addFlashAttribute("error", "Contact was modified by someone else. Please reload and try again.");
            } else {
                // Add a general error message
                redirectAttributes.addFlashAttribute("error", "Failed to update contact: " + e.getMessage());
            }
        }
        return "redirect:/contacts";
    }

    // Endpoint to delete a contact
    @PostMapping("/delete")
    public String deleteContact(@RequestParam String resourceName, RedirectAttributes redirectAttributes) {
        try {
            // Call the service to delete the contact
            googlePeopleService.deleteContact(resourceName);
            // Add a success message to the redirect attributes
            redirectAttributes.addFlashAttribute("message", "Contact deleted successfully!");
        } catch (IOException e) {
            e.printStackTrace();
            // Add an error message to the redirect attributes
            redirectAttributes.addFlashAttribute("error", "Failed to delete contact.");
        }
        return "redirect:/contacts";
    }

}