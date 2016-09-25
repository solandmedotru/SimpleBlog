package ru.solandme.simpleblog;

public class Blog {

    private String title, description, imageURL;

    public Blog() {
    }

    public Blog(String title, String description, String imageURL) {
        this.title = title;
        this.description = description;
        this.imageURL = imageURL;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}