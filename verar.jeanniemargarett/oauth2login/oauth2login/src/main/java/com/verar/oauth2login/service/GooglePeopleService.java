package com.verar.oauth2login.service;

import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GooglePeopleService {

    private final OAuth2AuthorizedClientService authorizedClientService;

    // Constructor
    public GooglePeopleService(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    // Get OAuth2 access token
    private String getAccessToken() {
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    oauthToken.getAuthorizedClientRegistrationId(),
                    oauthToken.getName()
            );
            if (client != null) {
                return client.getAccessToken().getTokenValue();
            }
        }
        throw new RuntimeException("OAuth2 authentication failed!");
    }

    // Create a PeopleService instance
    private PeopleService createPeopleService() {
        return new PeopleService.Builder(
                new com.google.api.client.http.javanet.NetHttpTransport(),
                new com.google.api.client.json.gson.GsonFactory(),
                request -> request.getHeaders().setAuthorization("Bearer " + getAccessToken())
        ).setApplicationName("Google Contacts App").build();
    }

    // Get list of contacts
    public List<Person> getContacts() throws IOException {
        try {
            PeopleService peopleService = createPeopleService();
            ListConnectionsResponse response = peopleService.people().connections()
                    .list("people/me")
                    .setPersonFields("names,emailAddresses,phoneNumbers")
                    .execute();
            return response.getConnections() != null ? response.getConnections() : new ArrayList<>();
        } catch (IOException e) {
            throw new IOException("Failed to retrieve contacts from Google People API", e);
        }
    }

    // Add a new contact
    public void addContact(String firstName, String lastName, List<String> emails, List<String> phoneNumbers) throws IOException {
        Person contactToCreate = new Person()
                .setNames(List.of(new Name().setGivenName(firstName).setFamilyName(lastName)))
                .setEmailAddresses(emails.stream().map(email -> new EmailAddress().setValue(email)).collect(Collectors.toList()))
                .setPhoneNumbers(phoneNumbers.stream().map(phone -> new PhoneNumber().setValue(phone)).collect(Collectors.toList()));

        PeopleService peopleService = createPeopleService();
        peopleService.people().createContact(contactToCreate).execute();
    }

    // Get a contact by resource name
    public Person getContact(String resourceName) throws IOException {
        return createPeopleService().people().get(resourceName)
                .setPersonFields("names,emailAddresses,phoneNumbers,metadata")
                .execute();
    }

    // Update an existing contact
    public void updateContact(String resourceName, String firstName, String lastName, List<String> emails, List<String> phoneNumbers) throws IOException {
        Person existingContact = getContact(resourceName);
        Person contactToUpdate = new Person()
                .setEtag(existingContact.getEtag())
                .setNames(List.of(new Name().setGivenName(firstName).setFamilyName(lastName)))
                .setEmailAddresses(emails.stream().map(email -> new EmailAddress().setValue(email)).collect(Collectors.toList()))
                .setPhoneNumbers(phoneNumbers.stream().map(phone -> new PhoneNumber().setValue(phone)).collect(Collectors.toList()));

        createPeopleService().people().updateContact(resourceName, contactToUpdate)
                .setUpdatePersonFields("names,emailAddresses,phoneNumbers")
                .execute();
    }

    // Delete a contact
    public void deleteContact(String resourceName) throws IOException {
        createPeopleService().people().deleteContact(resourceName).execute();
    }
}
