package com.clinic.project2.controller;


import com.clinic.project2.model.Film;
import com.clinic.project2.model.command.CreateFilmCommand;
import com.clinic.project2.model.dto.FilmDto;
import com.clinic.project2.service.FilmService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/films")
public class FilmController {

    private final FilmService filmService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FilmDto save(@RequestBody @Valid CreateFilmCommand command) {
        return filmService.save(command);
    }

    @GetMapping
    public List<FilmDto> findAll() {
        return filmService.findAll();
    }
}
