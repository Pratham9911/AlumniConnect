package com.pratham.alumniconnect;

public class EntryModel {
    private String title;
    private String subtitle;
    private String imageUrl;

    public EntryModel() {}  // For Firebase

    public EntryModel(String title, String subtitle, String imageUrl) {
        this.title = title;
        this.subtitle = subtitle;
        this.imageUrl = imageUrl;
    }

    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getImageUrl() { return imageUrl; }

    public void setTitle(String title) { this.title = title; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
