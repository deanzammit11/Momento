package com.example.momento.models;

// Model representing an event
public class Event {
    private int id; // Unique Event identifier
    private String title; // Event title
    private String date; // Event date
    private String location; // Event location
    private String category; // Event category
    private String imageUri; // URI for event image

    // Constructor
    public Event(int id, String title, String date, String location, String category, String imageUri) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.location = location;
        this.category = category;
        this.imageUri = imageUri;
    }

    public int getId() { return id; } // Returns event id
    public void setId(int id) { this.id = id; } // Updates event ID

    public String getTitle() { return title; } // Returns event title
    public void setTitle(String title) { this.title = title; } // Updates event title

    public String getDate() { return date; } // Returns event date
    public void setDate(String date) { this.date = date; } // Updates event date

    public String getLocation() { return location; } // Returns event location
    public void setLocation(String location) { this.location = location; } // Updates event location

    public String getCategory() { return category; } // Returns event category
    public void setCategory(String category) { this.category = category; } // Updates event category

    public String getImageUri() { return imageUri; } // Returns image URI
    public void setImageUri(String imageUri) { this.imageUri = imageUri; } // Updates image URI
}
