package com.clinic.project2.controller;


import com.clinic.project2.model.command.CreateClientCommand;
import com.clinic.project2.model.command.CreateSubscriptionCommand;
import com.clinic.project2.model.command.RemoveSubscriptionCommand;
import com.clinic.project2.model.dto.ClientDto;
import com.clinic.project2.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1/clients")
@RestController
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClientDto addClient(@RequestBody @Valid CreateClientCommand command) {
        return clientService.addClient(command);
    }

    @GetMapping("/confirm")
    public String confirmRegistration(@RequestParam @Valid String token) {
        return clientService.confirmRegistration(token);
    }


    @PutMapping("/{id}/add-subscription")
    public ClientDto addSubscription(@PathVariable @Valid Long id, @RequestBody CreateSubscriptionCommand command) {
        return clientService.addFilmSubscription(id, command);
    }


    @PutMapping("/{id}/remove-subscription")
    public ClientDto removeSubscription(@PathVariable @Valid Long id, @RequestBody RemoveSubscriptionCommand command) {
        return clientService.removeFilmSubscription(id, command);
    }
}
