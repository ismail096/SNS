package com.SnrtNews.storysnrt;

import java.util.ArrayList;
import java.util.List;

public class StoryUser
{

    private List<StoryItem> storyGlobal= new ArrayList<>();

    public StoryUser(List<StoryItem> storyGlobal) {
        this.storyGlobal = storyGlobal;
    }

    public StoryUser() {

    }

    public List<StoryItem> getStoryGlobal() {
        return storyGlobal;
    }

    public void setStoryGlobal(List<StoryItem> storyGlobal) {
        this.storyGlobal = storyGlobal;
    }
}
