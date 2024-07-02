package com.clinic.project2.service;

import com.clinic.project2.model.Film;
import com.clinic.project2.model.command.CreateFilmCommand;
import com.clinic.project2.model.dto.FilmDto;
import com.clinic.project2.repository.FilmRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class FilmServiceTest {
    @Mock
    private FilmRepository filmRepository;
    @InjectMocks
    private FilmService filmService;
    @Captor
    private ArgumentCaptor<Film> filmArgumentCaptor;


    @Test
    void testSaveBook_ValidData_ShouldSaveBook() {
        CreateFilmCommand command = CreateFilmCommand.builder()
                .title("Hobbit")
                .director("Tolkien")
                .category("Adventure")
                .build();
        when(filmRepository.save(any(Film.class))).thenAnswer(invocation -> {
            Film film= invocation.getArgument(0);
            film.setId(1L);
            return film;
        });
        FilmDto result = filmService.save(command);
        assertEquals(command.getTitle(), result.getTitle());
        assertEquals(command.getDirector(), result.getDirector());
        assertEquals(command.getCategory(), result.getCategory());
        verify(filmRepository, times(1)).save(filmArgumentCaptor.capture());
        Film savedFilm = filmArgumentCaptor.getValue();
        assertEquals(command.getTitle(), savedFilm.getTitle());
        assertEquals(command.getDirector(), savedFilm.getDirector());
        assertEquals(command.getCategory(), savedFilm.getCategory());
        verifyNoMoreInteractions(filmRepository);
    }
}