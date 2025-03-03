package com.verar.oauth2login.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.verar.oauth2login.entity.Contact;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class GoogleContactsService {
    private final OAuth2AuthorizedClientService authorizedClientService;

    public GoogleContactsService(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    public List<Contact> getContacts(OAuth2AuthenticationToken authentication) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );

        String accessToken = client.getAccessToken().getTokenValue();
        String contactsUrl = "https://people.googleapis.com/v1/people/me/connections?personFields=names,emailAddresses,phoneNumbers";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(contactsUrl, HttpMethod.GET, entity, String.class);

        return parseContacts(response.getBody());
    }

    private List<Contact> parseContacts(String responseBody) {
        List<Contact> contacts = new ArrayList<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode connections = root.path("connections");

            for (JsonNode connection : connections) {
                String name = connection.path("names").get(0).path("displayName").asText();
                String email = connection.has("emailAddresses") ? connection.path("emailAddresses").get(0).path("value").asText() : "N/A";
                String phoneNumber = connection.has("phoneNumbers") ? connection.path("phoneNumbers").get(0).path("value").asText() : "N/A";

                contacts.add(new Contact(name, email, phoneNumber));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contacts;
    }
}