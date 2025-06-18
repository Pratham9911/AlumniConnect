package com.pratham.alumniconnect;

import java.util.ArrayList;
import java.util.List;

public class SectionModel {
    private String sectionId;
    private String title;
    private List<EntryModel> entries;

    public SectionModel() {
        // Needed for Firebase
        this.entries = new ArrayList<>();
    }

    public SectionModel(String sectionId, String title) {
        this.sectionId = sectionId;
        this.title = title;
        this.entries = new ArrayList<>();
    }

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<EntryModel> getEntries() {
        return entries;
    }

    public void setEntries(List<EntryModel> entries) {
        this.entries = entries;
    }
}
