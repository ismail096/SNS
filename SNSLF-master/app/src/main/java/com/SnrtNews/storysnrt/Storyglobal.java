package com.SnrtNews.storysnrt;

import java.io.Serializable;
import java.util.List;

public class Storyglobal  implements Serializable
{
    private List<StoryModel> storyModel;

    public Storyglobal()
    {

    }

    public Storyglobal(List<StoryModel> storyModel) {
        this.storyModel = storyModel;
    }

    public List<StoryModel> getStoryModel() {
        return storyModel;
    }

    public void setStoryModel(List<StoryModel> storyModel) {
        this.storyModel = storyModel;
    }
}
