package com.SnrtNews.storysnrt;

import java.io.Serializable;

public class StoryModelDetail implements Serializable {
    private String type;
    private String datapub;
    private String imageStory;
    private String link;
    private String titleStory;



    public StoryModelDetail()
    {

    }

    public StoryModelDetail(String type, String datapub, String imageStory, String link, String titleStory)
    {
        this.type = type;
        this.datapub = datapub;
        this.imageStory = imageStory;
        this.link = link;
        this.titleStory = titleStory;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDatapub() {
        return datapub;
    }

    public void setDatapub(String datapub) {
        this.datapub = datapub;
    }

    public String getImageStory() {
        return imageStory;
    }

    public void setImageStory(String imageStory) {
        this.imageStory = imageStory;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getTitleStory() {
        return titleStory;
    }

    public void setTitleStory(String titleStory) {
        this.titleStory = titleStory;
    }
}
