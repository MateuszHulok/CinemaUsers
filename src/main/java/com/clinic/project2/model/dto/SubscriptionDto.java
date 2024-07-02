package com.clinic.project2.model.dto;


import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
public class SubscriptionDto {

    private Long clientId;
    private Set<String> directors;
    private Set<String> category;
}
