package com.clinic.project2.controller;

import com.clinic.project2.exception.EmailAlreadyUsedException;
import com.clinic.project2.model.Client;
import com.clinic.project2.model.command.CreateClientCommand;
import com.clinic.project2.model.command.CreateSubscriptionCommand;
import com.clinic.project2.model.command.RemoveSubscriptionCommand;
import com.clinic.project2.model.dto.ClientDto;
import com.clinic.project2.repository.ClientRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.util.AssertionErrors.assertNotEquals;
import static org.springframework.test.util.AssertionErrors.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClientRepository clientRepository;

    @BeforeEach
    public void clearDatabase() {
        clientRepository.deleteAll();
    }

    @Test
    void givenValidClient_whenAddClient_thenClientIsAdded() throws Exception {
        CreateClientCommand command = CreateClientCommand.builder()
                .firstName("John")
                .lastName("Mark")
                .mail("john.mark@gmil.com")
                .password("password")
                .build();
        String requestBody = objectMapper.writeValueAsString(command);
        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName", is(command.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(command.getLastName())))
                .andExpect(jsonPath("$.mail", is(command.getMail())));
        List<Client> clients = clientRepository.findAll();
        assertEquals(1, clients.size());
        Client savedClient = clients.get(0);
        assertEquals("John", savedClient.getFirstName());
        assertEquals("Mark", savedClient.getLastName());
        assertEquals("john.mark@gmil.com", savedClient.getMail());
    }

    @Test
    void testAddClient_InvalidData_ShouldReturnValidationError() throws Exception {
        int initialClientCount = clientRepository.findAll().size();

        CreateClientCommand command = CreateClientCommand.builder()
                .firstName("")
                .lastName("")
                .mail("")
                .password("")
                .build();
        String requestBody = objectMapper.writeValueAsString(command);
        MockHttpServletResponse response = mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse();

        String actualContent = response.getContentAsString();
        assertNotNull(actualContent, "Response content is null");
        assertNotEquals("", actualContent.trim(), "Response content is empty");

        List<Client> clients = clientRepository.findAll();
        assertEquals(initialClientCount, clients.size(), "No client should be saved due to validation errors");
    }

    @Test
    void testAddClient_DuplicateClient_ShouldNotBeAccepted() throws Exception {
        CreateClientCommand firstCommand = CreateClientCommand.builder()
                .firstName("John")
                .lastName("Mark")
                .mail("john.mark@gmil.com")
                .password("password")
                .build();
        String firstRequest = objectMapper.writeValueAsString(firstCommand);
        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstRequest))
                .andExpect(status().isCreated());
        CreateClientCommand secondCommand = CreateClientCommand.builder()
                .firstName("John")
                .lastName("Mark")
                .mail("john.mark@gmil.com")
                .password("password")
                .build();
        String secondRequest = objectMapper.writeValueAsString(secondCommand);

        Exception exception = assertThrows(Exception.class, () -> mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(secondRequest))
                .andReturn());

        assertEquals(EmailAlreadyUsedException.class, exception.getCause().getClass());
    }

    @Test
    void givenValidClientAndSubscription_whenAddSubscription_thenSubscriptionIsAdded() throws Exception {
        CreateClientCommand clientCommand = CreateClientCommand.builder()
                .firstName("John")
                .lastName("Mark")
                .mail("john.mark@gmil.com")
                .password("password")
                .build();

        String clientRequestBody = objectMapper.writeValueAsString(clientCommand);

        MvcResult clientResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientRequestBody))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn();

        String clientResponse = clientResult.getResponse().getContentAsString();
        ClientDto clientDto = objectMapper.readValue(clientResponse, ClientDto.class);

        Client savedClientBeforeSubscription = clientRepository.findByMail(clientDto.getMail());
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/clients/confirm")
                        .param("token", savedClientBeforeSubscription.getVerificationToken()))
                .andExpect(MockMvcResultMatchers.status().isOk());

        CreateSubscriptionCommand subscriptionCommand = CreateSubscriptionCommand.builder()
                .directors(Set.of("George Lucas"))
                .categories(Set.of("Fantasy"))
                .build();

        String subscriptionRequestBody = objectMapper.writeValueAsString(subscriptionCommand);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/clients/{id}/add-subscription", savedClientBeforeSubscription.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(subscriptionRequestBody))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Client savedClientAfterSubscription = clientRepository.findById(savedClientBeforeSubscription.getId()).orElseThrow(() -> new AssertionError("Client not found"));
        assertEquals(1, savedClientAfterSubscription.getSubscriptionDirector().size());
        assertEquals(1, savedClientAfterSubscription.getSubscriptionCategory().size());
        assertTrue(savedClientAfterSubscription.getSubscriptionDirector().contains("George Lucas"));
        assertTrue(savedClientAfterSubscription.getSubscriptionCategory().contains("Fantasy"));
    }

    @Test
    void givenClientWithSubscription_whenRemoveSubscription_thenSubscriptionIsRemoved() throws Exception {
        CreateClientCommand clientCommand = CreateClientCommand.builder()
                .firstName("John")
                .lastName("Mark")
                .mail("john.mark@gmil.com")
                .password("password")
                .build();

        String clientRequestBody = objectMapper.writeValueAsString(clientCommand);

        MvcResult clientResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientRequestBody))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn();

        String clientResponse = clientResult.getResponse().getContentAsString();
        ClientDto clientDto = objectMapper.readValue(clientResponse, ClientDto.class);

        Client savedClientBeforeSubscription = clientRepository.findByMail(clientDto.getMail());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/clients/confirm")
                        .param("token", savedClientBeforeSubscription.getVerificationToken()))
                .andExpect(MockMvcResultMatchers.status().isOk());

        RemoveSubscriptionCommand removeSubscriptionCommand = RemoveSubscriptionCommand.builder()
                .directors(Set.of("Tolkien"))
                .categories(Set.of("Fantasy"))
                .build();
        String removeSubscriptionRequestBody = objectMapper.writeValueAsString(removeSubscriptionCommand);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/clients/{id}/remove-subscription", savedClientBeforeSubscription.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(removeSubscriptionRequestBody))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Client savedClientAfterRemovingSubscription = clientRepository.findById(savedClientBeforeSubscription.getId()).orElseThrow(() -> new AssertionError("Client not found"));
        assertTrue(savedClientAfterRemovingSubscription.getSubscriptionDirector().isEmpty());
        assertTrue(savedClientAfterRemovingSubscription.getSubscriptionCategory().isEmpty());
    }

    @Test
    void testGetNonExistingEndpoint_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/non-existing-endpoint"))
                .andExpect(status().isNotFound());
    }
}