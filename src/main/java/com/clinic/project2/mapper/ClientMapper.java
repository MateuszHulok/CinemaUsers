package com.clinic.project2.mapper;

import com.clinic.project2.model.Client;
import com.clinic.project2.model.command.CreateClientCommand;
import com.clinic.project2.model.dto.ClientDto;

public class ClientMapper {

    public static Client mapFromCommand(CreateClientCommand command) {
        return Client.builder()
                .firstName(command.getFirstName())
                .lastName(command.getLastName())
                .mail(command.getMail())
                .password(command.getPassword())
                .build();
    }

    public static ClientDto mapToDto(Client client) {
        return ClientDto.builder()
                .firstName(client.getFirstName())
                .lastName(client.getLastName())
                .mail(client.getMail())
                .build();
    }
}
