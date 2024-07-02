package com.clinic.project2.repository;

import com.clinic.project2.model.Film;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface FilmRepository extends JpaRepository<Film, Long> {


    @Query("SELECT b FROM Film b where b.processedDate IS NULL AND b.createdDate >= :cutoffDate")
    List<Film> findUnprocessedBooksAddedRecently(@Param("cutoffDate")LocalDate cutoffDate);
}
