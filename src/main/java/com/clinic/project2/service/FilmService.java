package com.clinic.project2.service;


import com.clinic.project2.mapper.FilmMapper;
import com.clinic.project2.model.Film;
import com.clinic.project2.model.command.CreateFilmCommand;
import com.clinic.project2.model.dto.FilmDto;
import com.clinic.project2.repository.FilmRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.clinic.project2.mapper.FilmMapper.mapToDto;

@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmRepository filmRepository;

    public FilmDto save(CreateFilmCommand command) {
        Film toSave = FilmMapper.mapFromCommand(command);
        Film savedFilm = filmRepository.save(toSave);
        return mapToDto(savedFilm);
    }

    public List<FilmDto> findAll() {
        return filmRepository.findAll().stream()
                .map(FilmMapper::mapToDto)
                .toList();
    }
}
