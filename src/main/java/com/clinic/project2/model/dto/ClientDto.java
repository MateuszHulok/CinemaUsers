package com.clinic.project2.model.dto;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClientDto {

    private String firstName;
    private String lastName;
    private String mail;
}
