package com.SnrtNews.storysnrt.callbacks;

import com.SnrtNews.storysnrt.StoryItem
import com.SnrtNews.storysnrt.StoryUser


interface StoryCallback {
    fun onNextCalled(storyItem: StoryItem, index: Int)
    fun done()
}
