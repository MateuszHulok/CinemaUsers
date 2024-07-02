package com.clinic.project2.model.command;


import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class CreateSubscriptionCommand {

    private Set<String> directors;
    private Set<String> categories;
}
