package org.example.service;

import org.example.model.Movie;
import org.example.repository.MovieRepository;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MovieService {

    private final MovieRepository repository;
    private List<Movie> cache;

    public MovieService(MovieRepository repository) throws IOException {
        this.repository = repository;
        this.cache = repository.loadAll();
    }

    public Movie addMovie(String title, String genre, double rating) throws IOException {
        int nextId = cache.stream()
                .mapToInt(Movie::getId)
                .max()
                .orElse(0) + 1;
        Movie movie = new Movie(nextId, title, genre, rating);
        cache.add(movie);
        repository.saveAll(cache);
        return movie;
    }

    public List<Movie> filterByGenre(String genre) {
        return cache.stream()
                .filter(m -> m.getGenre().equalsIgnoreCase(genre))
                .collect(Collectors.toList());
    }

    public List<Movie> getTopN(int n) {
        if (n <= 0) throw new IllegalArgumentException("n must be positive");
        return cache.stream()
                .sorted(Comparator.comparingDouble(Movie::getRating).reversed())
                .limit(n)
                .collect(Collectors.toList());
    }

    public List<Movie> getAllMovies() {
        return cache;
    }
}
