package com.clinic.project2.service;


import com.clinic.project2.model.Client;
import com.clinic.project2.model.Film;
import com.clinic.project2.repository.ClientRepository;
import com.clinic.project2.repository.FilmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailService emailService;
    private final FilmRepository filmRepository;
    private final ClientRepository clientRepository;



    @Scheduled(cron = "0 0 20 * * *")
    public void sendNotifications() {
        LocalDate twoDaysAgo = LocalDate.now().minusDays(2);
        List<Film> newFilms = filmRepository.findUnprocessedBooksAddedRecently(twoDaysAgo);

        Map<String, List<Film>> emailToFilmMap = new HashMap<>();
        Pageable pageable = PageRequest.of(0, 20);

        while (true) {
            Page<Client> clients = clientRepository.findAllActiveClients(pageable);

            for (Client client : clients.getContent()) {
                for (Film film : newFilms) {
                    if (client.getSubscriptionCategory().contains(film.getCategory()) || client.getSubscriptionDirector().contains(film.getDirector())) {
                        emailToFilmMap.computeIfAbsent(client.getMail(), c -> new ArrayList<>()).add(film);
                    }
                }
            }
            if (!clients.hasNext()) {
                break;
            }
            pageable = clients.nextPageable();
        }
        emailToFilmMap.forEach((email, films) -> {
            if (!films.isEmpty()) {
                emailService.sendNewFilmsNotification(email, films);
            }
        });
    }
}
