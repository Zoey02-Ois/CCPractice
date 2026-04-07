package org.example.api;

public class MovieRequest {
    private String title;
    private String genre;
    private double rating;

    public MovieRequest() {}

    public String getTitle()  { return title; }
    public String getGenre()  { return genre; }
    public double getRating() { return rating; }

    public void setTitle(String title)   { this.title  = title; }
    public void setGenre(String genre)   { this.genre  = genre; }
    public void setRating(double rating) { this.rating = rating; }
}
