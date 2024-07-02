package com.clinic.project2.service;

import com.clinic.project2.exception.ClientNotFoundException;
import com.clinic.project2.exception.EmailAlreadyUsedException;
import com.clinic.project2.model.Client;
import com.clinic.project2.model.command.CreateClientCommand;
import com.clinic.project2.model.command.CreateSubscriptionCommand;
import com.clinic.project2.model.command.RemoveSubscriptionCommand;
import com.clinic.project2.model.dto.ClientDto;
import com.clinic.project2.repository.ClientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ClientServiceTest {

    @InjectMocks
    private ClientService clientService;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private EmailService emailService;

    @Captor
    private ArgumentCaptor<Client> clientArgumentCaptor;


    @Test
    public void testAddClient_NewClientIsAdded_ResultsInSuccess() {
        CreateClientCommand command = CreateClientCommand.builder()
                .firstName("John")
                .lastName("Mark")
                .mail("john.mark@gmil.com")
                .password("password")
                .build();
        Client expectedClient = Client.builder()
                .firstName("John")
                .lastName("Mark")
                .mail("john.mark@gmil.com")
                .password("password")
                .build();
        when(clientRepository.findByMail(command.getMail())).thenReturn(null);
        when(clientRepository.save(any(Client.class))).thenReturn(expectedClient);
        ClientDto savedClient = clientService.addClient(command);
        verify(clientRepository).save(clientArgumentCaptor.capture());
        Client savedClientEntity = clientArgumentCaptor.getValue();
        assertEquals(command.getFirstName(), savedClientEntity.getFirstName());
        assertEquals(command.getLastName(), savedClientEntity.getLastName());
        assertEquals(command.getMail(), savedClientEntity.getMail());
        assertNotNull(savedClientEntity.getVerificationToken());
        verify(emailService).sendEmailValidationRequest(savedClientEntity);
    }

    @Test
    public void testConfirmRegistration_ValidToken_ResultsInAccountBeingVerified() {
        String token = UUID.randomUUID().toString();
        Client client = Client.builder()
                .firstName("John")
                .lastName("Mark")
                .mail("john.mark@gmil.com")
                .password("password")
                .build();
        when(clientRepository.findByVerificationToken(token)).thenReturn(client);
        String confirmationResult = clientService.confirmRegistration(token);
        assertEquals("Your account has been successfully verified!", confirmationResult);
        assertTrue(client.isActive());
        assertNull(client.getVerificationToken());
        verify(clientRepository).save(clientArgumentCaptor.capture());
        Client updatedClient = clientArgumentCaptor.getValue();
        assertNull(updatedClient.getVerificationToken(), "Verification token should be null after confirmation");
        assertTrue(updatedClient.isActive(), "Client should be active after confirmation");
    }

    @Test
    public void testAddClient_EmailAlreadyExists_ThrowsException() {
        CreateClientCommand command = CreateClientCommand.builder()
                .firstName("John")
                .lastName("Mark")
                .mail("john.mark@gmil.com")
                .password("password")
                .build();
        when(clientRepository.findByMail(command.getMail())).thenReturn(new Client());
        EmailAlreadyUsedException exception = assertThrows(
                EmailAlreadyUsedException.class,
                () -> clientService.addClient(command)
        );
        assertEquals("An account with this email address already exists.", exception.getMessage());
    }

    @Test
    public void testAddSubscription_ClientNotFound_ThrowsException() {
        Long clientId = 1L;
        CreateSubscriptionCommand command = CreateSubscriptionCommand.builder()
                .directors(Set.of("George Lucas"))
                .categories(Set.of("Fantasy"))
                .build();
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());
        ClientNotFoundException exception = assertThrows(
                ClientNotFoundException.class,
                () -> clientService.addFilmSubscription(clientId, command)
        );
        assertEquals("Client with id 1 not found", exception.getMessage());
    }

    @Test
    public void testRemoveSubscription_ClientNotFound_ThrowsException() {
        Long clientId = 1L;
        RemoveSubscriptionCommand command = RemoveSubscriptionCommand.builder()
                .directors(Set.of("George Lucas"))
                .categories(Set.of("Fantasy"))
                .build();
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());
        ClientNotFoundException exception = assertThrows(
                ClientNotFoundException.class,
                () -> clientService.removeFilmSubscription(clientId, command)
        );
        assertEquals("Client with id 1 not found", exception.getMessage());
    }

    @Test
    public void testConfirmRegistration_TokenNotFound_ThrowsException() {
        String token = "invalidToken";
        when(clientRepository.findByVerificationToken(token)).thenReturn(null);

        ClientNotFoundException exception = assertThrows(
                ClientNotFoundException.class,
                () -> clientService.confirmRegistration(token)
        );
        assertEquals("Invalid token", exception.getMessage());
    }
}