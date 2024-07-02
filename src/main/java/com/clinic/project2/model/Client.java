package com.clinic.project2.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    private String mail;

    private String password;

    private boolean active;

    private String verificationToken;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "subscription_director", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "director")
    private Set<String> subscriptionDirector;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "subscription_category", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "category")
    private Set<String> subscriptionCategory;
}
