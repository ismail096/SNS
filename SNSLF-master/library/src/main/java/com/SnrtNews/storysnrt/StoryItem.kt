package com.SnrtNews.storysnrt;


import android.view.View
import androidx.annotation.LayoutRes

sealed class StoryItem(open val durationInSeconds: Int)
{
    lateinit var view: View
    data class RemoteImage(val imageUrl: String, override val durationInSeconds: Int , val AvatarJournal: String ,val NameJournal: String , val TimePublication: String, val TitleStory: String , val LongApi: String ) : StoryItem(durationInSeconds)
    data class Video(val videoUrl: String , val AvatarJournal: String ,val NameJournal: String , val TimePublication: String ,val TitleStory: String , val LongApi: String) : StoryItem(36000)
    data class CustomView(val customView: View, override val durationInSeconds: Int) : StoryItem(durationInSeconds)
    data class CustomLayout(@LayoutRes internal val layout: Int, override val durationInSeconds: Int) : StoryItem(durationInSeconds)
}
