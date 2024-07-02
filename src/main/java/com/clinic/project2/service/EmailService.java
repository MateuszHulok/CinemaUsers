package com.clinic.project2.service;


import com.clinic.project2.exception.EmailNotSendException;
import com.clinic.project2.model.Client;
import com.clinic.project2.model.Film;
import com.clinic.project2.repository.ClientRepository;
import com.clinic.project2.repository.FilmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender sender;
    private final FilmRepository filmRepository;
    private final ClientRepository clientRepository;

    @Value("http://localhost:8080")
    private String appHost;

    @Async
    public void sendEmailValidationRequest(Client client) {
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
            helper.setTo(client.getMail());
            helper.setSubject("Account Confirmation");
            String confirmationUrl = appHost + "/api/v1/clients/confirm?token=" + client.getVerificationToken();
            String text = "<p>Dear Client :),</p>"
                    + "<p>To confirm your account, click <a href=\"" + confirmationUrl + "\">Confirm your account</a>.</p>";
            helper.setText(text, true);
            sender.send(message);
        } catch (MessagingException e) {
            throw new EmailNotSendException("Failed to send email", e);
        }
    }

    @Async
    public void sendNewFilmsNotification(String email, List<Film> newFilms) {
        Client client = clientRepository.findByMail(email);
        List<Film> filmsToNotify = newFilms.stream()
                .filter(book -> client.getSubscriptionDirector().contains(book.getDirector()) || client.getSubscriptionCategory().contains(book.getCategory()))
                .collect(Collectors.toList());

        if (!filmsToNotify.isEmpty()) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("\uD83C\uDFA5 New Films Available");
            String emailContent = buildEmailContentForNewBooks(filmsToNotify);
            message.setText(emailContent);
            sender.send(message);

            filmsToNotify.forEach(book -> {
                book.setProcessedDate(LocalDate.now());
                filmRepository.save(book);
            });
        }
    }
    private String buildEmailContentForNewBooks(List<Film> newFilms) {
        StringBuilder text = new StringBuilder();
        text.append("Dear Subscriber,\n\n");
        text.append("We're excited to inform you that new films matching your subscription preferences are available at Our Cinema.\n\n");
        for (Film film : newFilms) {
            text.append("Title: ").append(film.getTitle()).append("\n");
            text.append("Director: ").append(film.getDirector()).append("\n");
            text.append("Category: ").append(film.getCategory()).append("\n\n");
        }
        text.append("Stay tuned for more updates and happy reading!");
        return text.toString();
    }
}