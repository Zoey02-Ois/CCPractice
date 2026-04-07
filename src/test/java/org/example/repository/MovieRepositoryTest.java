package org.example.repository;

import org.example.model.Movie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MovieRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void loadAll_nonExistentFile_returnsEmptyList() throws IOException {
        MovieRepository repo = new MovieRepository(tempDir.resolve("new.json").toString());
        assertTrue(repo.loadAll().isEmpty());
    }

    @Test
    void saveAndLoad_roundTrip() throws IOException {
        String path = tempDir.resolve("movies.json").toString();
        MovieRepository repo = new MovieRepository(path);

        List<Movie> movies = Arrays.asList(
                new Movie(1, "Inception",    "Sci-Fi", 9.0),
                new Movie(2, "The Godfather","Drama",  9.2)
        );
        repo.saveAll(movies);

        List<Movie> loaded = repo.loadAll();
        assertEquals(2, loaded.size());
        assertEquals("Inception",     loaded.get(0).getTitle());
        assertEquals("The Godfather", loaded.get(1).getTitle());
        assertEquals(9.2, loaded.get(1).getRating());
    }

    @Test
    void saveAll_overwritesPreviousData() throws IOException {
        String path = tempDir.resolve("movies.json").toString();
        MovieRepository repo = new MovieRepository(path);

        repo.saveAll(Arrays.asList(new Movie(1, "Old Movie", "Drama", 7.0)));
        repo.saveAll(Arrays.asList(new Movie(1, "New Movie", "Action", 8.0)));

        List<Movie> loaded = repo.loadAll();
        assertEquals(1, loaded.size());
        assertEquals("New Movie", loaded.get(0).getTitle());
    }
}