package com.clinic.project2.controller;

import com.clinic.project2.model.Film;
import com.clinic.project2.model.command.CreateFilmCommand;
import com.clinic.project2.repository.FilmRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.core.type.TypeReference;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FilmRepository filmRepository;

    @BeforeEach
    public void clearDatabase() {
        filmRepository.deleteAll();
    }

    @Test
    void testSaveFilm_ValidData_ShouldReturnFilm() throws Exception {
        CreateFilmCommand command = CreateFilmCommand.builder()
                .title("Jurassic Park")
                .director("Steven Spielberg")
                .category("Fantasy")
                .build();
        String requestBody = objectMapper.writeValueAsString(command);
        mockMvc.perform(post("/api/v1/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is(command.getTitle())))
                .andExpect(jsonPath("$.director", is(command.getDirector())))
                .andExpect(jsonPath("$.category", is(command.getCategory())));


        List<Film> films = filmRepository.findAll();
        assertEquals(1, films.size());
        Film savedFilm = films.get(0);
        assertEquals("Jurassic Park", savedFilm.getTitle());
        assertEquals("Steven Spielberg", savedFilm.getDirector());
        assertEquals("Fantasy", savedFilm.getCategory());
    }

    @Test
    void testSaveFilm_InvalidData_ShouldReturnValidationError() throws Exception {
        int initialFilmCount = filmRepository.findAll().size();

        CreateFilmCommand command = CreateFilmCommand.builder()
                .title("")
                .director("")
                .category("")
                .build();
        String requestBody = objectMapper.writeValueAsString(command);
        MockHttpServletResponse response = mockMvc.perform(post("/api/v1/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse();
        String actualContent = response.getContentAsString();
        assertNotNull(actualContent, "Response content is null");
        assertNotEquals("", actualContent.trim(), "Response content is empty");

        Map<String, String> errors = objectMapper.readValue(actualContent, new TypeReference<>() {});
        assertNotNull(errors.get("title"), "Title validation error is missing");
        assertNotNull(errors.get("director"), "Director validation error is missing");
        assertNotNull(errors.get("category"), "Category validation error is missing");

        List<Film> films = filmRepository.findAll();
        assertEquals(initialFilmCount, films.size(), "No film should be saved due to validation errors");
    }

    @Test
    void testSaveFilm_DuplicateFilm_ShouldBeAccepted() throws Exception {
        CreateFilmCommand firstCommand = CreateFilmCommand.builder()
                .title("Star Wars")
                .director("George Lucas")
                .category("Sci-Fi")
                .build();

        String firstRequest = objectMapper.writeValueAsString(firstCommand);
        MvcResult firstResult = mockMvc.perform(post("/api/v1/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstRequest))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode firstResponse = objectMapper.readTree(firstResult.getResponse().getContentAsString());
        assertEquals("Star Wars", firstResponse.get("title").asText());
        assertEquals("George Lucas", firstResponse.get("director").asText());
        assertEquals("Sci-Fi", firstResponse.get("category").asText());


        CreateFilmCommand secoundCommand = CreateFilmCommand.builder()
                .title("Star Wars")
                .director("George Lucas")
                .category("Sci-Fi")
                .build();

        String secondRequest = objectMapper.writeValueAsString(secoundCommand);
        MvcResult secondResult = mockMvc.perform(post("/api/v1/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(secondRequest))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode secoundResponse = objectMapper.readTree(secondResult.getResponse().getContentAsString());
        assertEquals("Star Wars", secoundResponse.get("title").asText());
        assertEquals("George Lucas", secoundResponse.get("director").asText());
        assertEquals("Sci-Fi", secoundResponse.get("category").asText());
    }

    @Test
    void testGetNonExistingEndPoint_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/nonexisting"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testFindUnprocessedBooksAddedRecently_ShouldReturnUnprocessedBooks() throws Exception {
        Film film1 = Film.builder()
                .title("Film 1")
                .processedDate(null)
                .createdDate(LocalDate.now().minusDays(1))
                .build();
        filmRepository.save(film1);

        Film film2 = Film.builder()
                .title("Film 2")
                .processedDate(null)
                .createdDate(LocalDate.now().minusDays(2))
                .build();
        filmRepository.save(film2);

        Film film3 = Film.builder()
                .title("Film 3")
                .processedDate(LocalDate.now())
                .createdDate(LocalDate.now().minusDays(3))
                .build();
        filmRepository.save(film3);

        LocalDate cutoffDate = LocalDate.now().minusDays(2);
        List<Film> unprocessedFilms = filmRepository.findUnprocessedBooksAddedRecently(cutoffDate);

        assertEquals(2, unprocessedFilms.size());
        assertEquals("Film 1", unprocessedFilms.get(0).getTitle());
        assertEquals("Film 2", unprocessedFilms.get(1).getTitle());
    }
}