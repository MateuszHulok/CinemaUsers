package com.clinic.project2.mapper;

import com.clinic.project2.model.Film;
import com.clinic.project2.model.command.CreateFilmCommand;
import com.clinic.project2.model.dto.FilmDto;

public class FilmMapper {


    public static Film mapFromCommand(CreateFilmCommand command) {
        return Film.builder()
                .director(command.getDirector())
                .title(command.getTitle())
                .category(command.getCategory())
                .build();
    }

    public static FilmDto mapToDto(Film film) {
        return FilmDto.builder()
                .director(film.getDirector())
                .title(film.getTitle())
                .category(film.getCategory())
                .build();
    }
}
