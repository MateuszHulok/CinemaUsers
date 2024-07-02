package com.clinic.project2.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.clinic.project2.model.Client;
import com.clinic.project2.model.Film;
import com.clinic.project2.repository.ClientRepository;
import com.clinic.project2.repository.FilmRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @InjectMocks
    private EmailService emailService;

    @Mock
    private JavaMailSender sender;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private FilmRepository filmRepository;

    @Captor
    private ArgumentCaptor<SimpleMailMessage> mailMessageCaptor;

    @Test
    public void testSendNewFilmsNotification_ValidEmailAndBook_EmailSent() {
        String email = "john.mark@gmail.com";
        Client client = Client.builder()
                .id(1L)
                .mail(email)
                .subscriptionDirector(Set.of("Director"))
                .subscriptionCategory(Set.of("Category"))
                .build();
        Film film = Film.builder()
                .title("Title")
                .director("Director")
                .category("Category")
                .build();
        when(clientRepository.findByMail(email)).thenReturn(client);

        emailService.sendNewFilmsNotification(email, List.of(film));

        verify(sender).send(mailMessageCaptor.capture());
        SimpleMailMessage sentMessage = mailMessageCaptor.getValue();
        assertEquals(email, sentMessage.getTo()[0]);
        assertTrue(sentMessage.getSubject().contains("\uD83C\uDFA5 New Films Available"));
        assertTrue(sentMessage.getText().contains("Title: " + film.getTitle()));
        assertTrue(sentMessage.getText().contains("Director: " + film.getDirector()));
        assertTrue(sentMessage.getText().contains("Category: " + film.getCategory()));
    }

    @Test
    public void testSendNewBookNotification_ValidEmailAndBook_EmailSent() {
        String email = "john.mark@gmail.com";
        Client client = Client.builder()
                .id(1L)
                .mail(email)
                .subscriptionDirector(Set.of("Director"))
                .subscriptionCategory(Set.of("Category"))
                .build();

        Film film = Film.builder()
                .title("Title")
                .director("Director")
                .category("Category")
                .build();

        when(clientRepository.findByMail(email)).thenReturn(client);

        emailService.sendNewFilmsNotification(email, List.of(film));
        verify(sender).send(mailMessageCaptor.capture());
        SimpleMailMessage sentMessage = mailMessageCaptor.getValue();
        assertEquals(email, sentMessage.getTo()[0]);
        assertTrue(sentMessage.getSubject().contains("\uD83C\uDFA5 New Films Available"));
        assertTrue(sentMessage.getText().contains("Title: " + film.getTitle()));
        assertTrue(sentMessage.getText().contains("Director: " + film.getDirector()));
        assertTrue(sentMessage.getText().contains("Category: " + film.getCategory()));
    }

    @Test
    public void testSendNewBookNotification_EmptyEmail_NoExceptionThrown() {
        String email = "";
        Client client = Client.builder()
                .id(1L)
                .mail(email)
                .subscriptionCategory(new HashSet<>())
                .subscriptionDirector(new HashSet<>())
                .build();

        Film film = Film.builder()
                .title("Title")
                .director("Director")
                .category("Category")
                .build();

        when(clientRepository.findByMail(email)).thenReturn(client);

        assertDoesNotThrow(() -> emailService.sendNewFilmsNotification(email, List.of(film)));
        verify(sender, never()).send(any(SimpleMailMessage.class));
    }
}
