package com.example.momento.models;

// Model representing an event category
public class Category {
    private int id; // Unique category identifier
    private String name; // Name of the category

    // Empty constructor
    public Category() {}

    // Constructor with id and name
    public Category(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // Returns the category ID
    public int getId() {
        return id;
    }

    // Updates the category ID
    public void setId(int id) {
        this.id = id;
    }

    // Returns the category name
    public String getName() {
        return name;
    }

    // Updates the category name
    public void setName(String name) {
        this.name = name;
    }
}
