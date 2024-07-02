package com.clinic.project2.model.command;


import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class RemoveSubscriptionCommand {

    private Set<String> directors;
    private Set<String> categories;
}
