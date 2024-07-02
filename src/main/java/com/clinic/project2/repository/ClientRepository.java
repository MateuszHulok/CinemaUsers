package com.clinic.project2.repository;

import com.clinic.project2.model.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ClientRepository extends JpaRepository<Client, Long> {

    Client findByMail(String mail);

    Client findByVerificationToken(String verificationToken);

    @Query("SELECT c FROM Client c WHERE c.active = true")
    Page<Client> findAllActiveClients(Pageable pageable);
}
