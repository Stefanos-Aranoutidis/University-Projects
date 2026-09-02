package com.unipi.unipifirechat;

public class User {
    private String id;
    private String username;
    private String email;

    // 1. Κενός Κατασκευαστής
    public User() {
    }

    // 2. Κατασκευαστής με στοιχεία
    public User(String id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    // 3. Getters και Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}