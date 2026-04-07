package org.example.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class User {

    private final String username;
    private final String password;   // SHA-256 hex digest，由 UserService 负责哈希后再传入
    private final String createdAt;  // ISO-8601 字符串，例如 "2026-04-07T10:00:00"

    @JsonCreator
    public User(
            @JsonProperty("username")  String username,
            @JsonProperty("password")  String password,
            @JsonProperty("createdAt") String createdAt) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username must not be empty");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password must not be empty");
        }
        if (createdAt == null || createdAt.isEmpty()) {
            throw new IllegalArgumentException("CreatedAt must not be empty");
        }
        this.username  = username.trim();
        this.password  = password;
        this.createdAt = createdAt;
    }

    public String getUsername()  { return username; }
    public String getPassword()  { return password; }
    public String getCreatedAt() { return createdAt; }
}
