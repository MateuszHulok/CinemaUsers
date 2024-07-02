package com.clinic.project2.service;


import com.clinic.project2.exception.ClientNotFoundException;
import com.clinic.project2.exception.EmailAlreadyUsedException;
import com.clinic.project2.mapper.ClientMapper;
import com.clinic.project2.model.Client;
import com.clinic.project2.model.command.CreateClientCommand;
import com.clinic.project2.model.command.CreateSubscriptionCommand;
import com.clinic.project2.model.command.RemoveSubscriptionCommand;
import com.clinic.project2.model.dto.ClientDto;
import com.clinic.project2.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.clinic.project2.mapper.ClientMapper.mapToDto;


@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final EmailService emailService;

    public ClientDto addClient(CreateClientCommand command) {
        if (clientRepository.findByMail(command.getMail()) != null) {
            throw new EmailAlreadyUsedException("An account with this email address already exists.");
        }
        Client toSave = ClientMapper.mapFromCommand(command);
        toSave.setVerificationToken(UUID.randomUUID().toString());
        emailService.sendEmailValidationRequest(toSave);
        Client savedClient = clientRepository.save(toSave);
        return mapToDto(savedClient);
    }

    public String confirmRegistration(String token) {
        Client client = clientRepository.findByVerificationToken(token);
        if (client == null) {
            throw new ClientNotFoundException("Invalid token");
        }
        client.setActive(true);
        client.setVerificationToken(null);
        clientRepository.save(client);
        return "Your account has been successfully verified!";
    }

    public ClientDto addFilmSubscription(Long id, CreateSubscriptionCommand command) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(
                        String.format("Client with id %d not found", id)));

        if (command.getDirectors() != null) {
            client.getSubscriptionDirector().addAll(command.getDirectors());
        }
        if (command.getCategories() != null) {
            client.getSubscriptionCategory().addAll(command.getCategories());
        }

        Client updatedClient = clientRepository.save(client);
        return mapToDto(updatedClient);
    }

    public ClientDto removeFilmSubscription(Long id, RemoveSubscriptionCommand command) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(
                        String.format("Client with id %d not found", id)));

        if (command.getDirectors() != null) {
            client.getSubscriptionDirector().removeAll(command.getDirectors());
        }
        if (command.getCategories() != null) {
            client.getSubscriptionCategory().removeAll(command.getCategories());
        }

        Client updatedClient = clientRepository.save(client);
        return mapToDto(updatedClient);
    }
}