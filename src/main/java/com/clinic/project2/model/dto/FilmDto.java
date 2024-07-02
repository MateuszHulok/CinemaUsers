package com.clinic.project2.model.dto;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FilmDto {

    private String director;
    private String title;
    private String category;
}
