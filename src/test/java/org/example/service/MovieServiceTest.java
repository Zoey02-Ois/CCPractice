package org.example.service;

import org.example.model.Movie;
import org.example.repository.MovieRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MovieServiceTest {

    @TempDir
    Path tempDir;

    private MovieService service;

    @BeforeEach
    void setUp() throws IOException {
        String path = tempDir.resolve("movies.json").toString();
        service = new MovieService(new MovieRepository(path));
    }

    @Test
    void addMovie_persistsAndReturnsCorrectMovie() throws IOException {
        Movie m = service.addMovie("Inception", "Sci-Fi", 9.0);
        assertEquals(1, m.getId());
        assertEquals("Inception", m.getTitle());
        assertEquals("Sci-Fi", m.getGenre());
        assertEquals(9.0, m.getRating());
    }

    @Test
    void addMovie_autoIncrementsId() throws IOException {
        service.addMovie("Movie A", "Drama", 7.0);
        Movie second = service.addMovie("Movie B", "Drama", 8.0);
        assertEquals(2, second.getId());
    }

    @Test
    void addMovie_invalidRating_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.addMovie("Bad Movie", "Action", 0.5));
        assertThrows(IllegalArgumentException.class,
                () -> service.addMovie("Bad Movie", "Action", 10.1));
    }

    @Test
    void addMovie_emptyTitle_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.addMovie("  ", "Action", 7.0));
    }

    @Test
    void filterByGenre_returnsMatchingMovies() throws IOException {
        service.addMovie("Inception",    "Sci-Fi", 9.0);
        service.addMovie("Interstellar", "Sci-Fi", 8.5);
        service.addMovie("The Godfather","Drama",  9.2);

        List<Movie> scifi = service.filterByGenre("Sci-Fi");
        assertEquals(2, scifi.size());
        assertTrue(scifi.stream().allMatch(m -> m.getGenre().equalsIgnoreCase("Sci-Fi")));
    }

    @Test
    void filterByGenre_caseInsensitive() throws IOException {
        service.addMovie("Inception", "Sci-Fi", 9.0);
        assertEquals(1, service.filterByGenre("sci-fi").size());
        assertEquals(1, service.filterByGenre("SCI-FI").size());
    }

    @Test
    void filterByGenre_noMatch_returnsEmpty() throws IOException {
        service.addMovie("Inception", "Sci-Fi", 9.0);
        assertTrue(service.filterByGenre("Horror").isEmpty());
    }

    @Test
    void getTopN_returnsCorrectOrder() throws IOException {
        service.addMovie("Movie A", "Action", 6.0);
        service.addMovie("Movie B", "Action", 9.0);
        service.addMovie("Movie C", "Action", 7.5);

        List<Movie> top2 = service.getTopN(2);
        assertEquals(2, top2.size());
        assertEquals("Movie B", top2.get(0).getTitle());
        assertEquals("Movie C", top2.get(1).getTitle());
    }

    @Test
    void getTopN_nLargerThanSize_returnsAll() throws IOException {
        service.addMovie("Movie A", "Action", 8.0);
        service.addMovie("Movie B", "Action", 7.0);
        assertEquals(2, service.getTopN(10).size());
    }

    @Test
    void getTopN_invalidN_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> service.getTopN(0));
        assertThrows(IllegalArgumentException.class, () -> service.getTopN(-1));
    }

    @Test
    void getAllMovies_returnsAllAdded() throws IOException {
        service.addMovie("Movie A", "Action", 8.0);
        service.addMovie("Movie B", "Drama",  7.0);
        assertEquals(2, service.getAllMovies().size());
    }
}