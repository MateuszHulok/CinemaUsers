package com.clinic.project2.model.command;


import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateFilmCommand {

    @NotBlank(message = "Director cannot be blank")
    private String director;

    @NotBlank(message = "Title cannot be blank")
    private String title;

    @NotBlank(message = "Category cannot be blank")
    private String category;
}
