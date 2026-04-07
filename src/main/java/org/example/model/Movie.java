package org.example.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Movie {

    private final int id;
    private final String title;
    private final String genre;
    private final double rating;

    @JsonCreator
    public Movie(
            @JsonProperty("id")     int id,
            @JsonProperty("title")  String title,
            @JsonProperty("genre")  String genre,
            @JsonProperty("rating") double rating) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title must not be empty");
        }
        if (genre == null || genre.trim().isEmpty()) {
            throw new IllegalArgumentException("Genre must not be empty");
        }
        if (rating < 1.0 || rating > 10.0) {
            throw new IllegalArgumentException("Rating must be between 1 and 10");
        }
        this.id     = id;
        this.title  = title.trim();
        this.genre  = genre.trim();
        this.rating = rating;
    }

    public int    getId()     { return id; }
    public String getTitle()  { return title; }
    public String getGenre()  { return genre; }
    public double getRating() { return rating; }

    @Override
    public String toString() {
        return String.format("[%d] %-30s | %-15s | %.1f/10", id, title, genre, rating);
    }
}
