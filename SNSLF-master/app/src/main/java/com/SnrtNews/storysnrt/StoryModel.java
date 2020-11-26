package com.SnrtNews.storysnrt;


import java.io.Serializable;

public class StoryModel implements Serializable {
    private String nameJournaliste;
    private String AvatarJournaliste;

    private StoryModelDetail storyModelDetail;


    public StoryModel() {
    }

    public StoryModel(String nameJournaliste, String avatarJournaliste, StoryModelDetail storyModelDetail) {
        this.nameJournaliste = nameJournaliste;
        AvatarJournaliste = avatarJournaliste;
        this.storyModelDetail = storyModelDetail;
    }

    public String getNameJournaliste() {
        return nameJournaliste;
    }

    public void setNameJournaliste(String nameJournaliste) {
        this.nameJournaliste = nameJournaliste;
    }

    public String getAvatarJournaliste() {
        return AvatarJournaliste;
    }

    public void setAvatarJournaliste(String avatarJournaliste) {
        AvatarJournaliste = avatarJournaliste;
    }

    public StoryModelDetail getStoryModelDetail() {
        return storyModelDetail;
    }

    public void setStoryModelDetail(StoryModelDetail storyModelDetail) {
        this.storyModelDetail = storyModelDetail;
    }
}
