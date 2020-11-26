# Stories

StoryNewsSnrt is a library that shows a stories.

## Install

Add jitpack to your repositories in your root `build.gradle`:

```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

```

Add the dependency in your app `build.gradle`:

```
dependencies 
{
	implementation 'com.github.ismail096:STNewsLast:V1.0'
}


```

## How to Use

To see how a StoryNewsSnrt can be added to your xml layouts, check the app project.

```xml
   <androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/container"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="horizontal"
    tools:context=".MainActivity">

</androidx.constraintlayout.widget.ConstraintLayout>
```
```java

storyItem .add(new StoryItem.RemoteImage("LinkImage",DurationInSecond,"AvatarJournal","NameJournal","TimePublication","TitleStory", "Langue Story on a 2 valeur soit fr ou ar"));

storyItem .add(new StoryItem.Video("LinkVideo","AvatarJournal","NameJournal","TimePublication","TitleStory", "Langue Story on a 2 valeur soit fr ou ar"));

       // ici On a 2 Version Soit l'arabe ou francais 
       
      // si tu veut utilise le francais donc il faut utilise la classe SnrtNewsStoryFr: 
       
                 story = new SnrtNewsStoryFr(this, container, storyuser, new StoryCallback() {
                    @Override
                    public void onNextCalled(@NotNull StoryItem storyItem, int index)
                    {

                    }


                    @Override
                    public void done()
                    {


                    }
                },mProgressDrawable);


                story.start(Position of user click);

     //si tu veut l 'arabe  donc il faut utilise la classe SnrtNewsStoryAr: 
     
     story = new SnrtNewsStoryAr(this, container, storyuser, new StoryCallback() {
                    @Override
                    public void onNextCalled(@NotNull StoryItem storyItem, int index)
                    {

                    }


                    @Override
                    public void done()
                    {


                    }
                },mProgressDrawable);


                story.start(Position of user click);
     
        
```   
Overview

```java
package com.SnrtNews.storysnrt;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.SnrtNews.storysnrt.callbacks.StoryCallback;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;


public class StoryActivity extends AppCompatActivity
{

    // choix entre story arabe et francais
    private SnrtNewsStoryAr story;
    private int mProgressDrawable = R.drawable.white_lightgrey_drawable;

    // 1 story
    private List<StoryItem> storyItem;
    // story 1 user
    private List<List<StoryItem>> storyuser;
    // story All user
    private ArrayList<ArrayList<StoryModel>> storyGlobal= new ArrayList<ArrayList<StoryModel>>();



    private ConstraintLayout container;
    private ArrayList<StoryModel> storyModels = new ArrayList<>();
    private  StoryModel storyModel ;
    private  StoryModelDetail storyModelDetail ;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        container = findViewById(R.id.container);

        getStoryData();


    }


    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onPause() {
        story.pause(false);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        story.release();
        super.onDestroy();
    }

    private void getStoryData() {
        AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);
        client.get("https://snrtnews.aramobile.com/api/stories/ar", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);

                try {



                    JSONArray jsonarray = new JSONArray(result);
                    for (int i = 0; i < jsonarray.length(); i++)
                    {
                        JSONObject jsonobject = jsonarray.getJSONObject(i);
                        storyModel = new StoryModel();
                        storyModels = new ArrayList<>();
                        storyModel.setNameJournaliste(jsonobject.getString("name"));
                        storyModel.setAvatarJournaliste(jsonobject.getString("image"));
                        JSONArray jsonarray2 = new JSONArray(jsonobject.getString("stories"));
                        for (int k = 0; k < jsonarray2.length(); k++)
                        {
                            storyModelDetail = new StoryModelDetail();
                            JSONObject jsonobject2 = jsonarray2.getJSONObject(k);
                            storyModelDetail.setType(jsonobject2.getString("type"));
                            storyModelDetail.setDatapub(jsonobject2.getString("date_publication"));
                            storyModelDetail.setImageStory(jsonobject2.getString("image"));
                            storyModelDetail.setLink(jsonobject2.getString("link"));
                            storyModelDetail.setTitleStory(jsonobject2.getString("title"));
                            storyModel.setStoryModelDetail(storyModelDetail);
                            storyModels.add(storyModel);

                        }
                        storyGlobal.add(storyModels);


                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }


            @Override
            public void onFinish()
            {

                storyuser = new ArrayList<>();

                for(int k =0; k<storyGlobal.size();k++)
                {
                    storyItem = new ArrayList<>();
                    for(int i =0; i< storyGlobal.get(k).size();i++)
                    {
                        if(storyGlobal.get(k).get(i).getStoryModelDetail().getType().equals("image"))
                        {
                            storyItem.add(new StoryItem.RemoteImage("https://snrtnews.aramobile.com"+storyGlobal.get(k).get(i).getStoryModelDetail().getImageStory(), 5,"https://snrtnews.aramobile.com"+storyGlobal.get(k).get(i).getAvatarJournaliste(),storyGlobal.get(k).get(i).getNameJournaliste(), storyGlobal.get(k).get(i).getStoryModelDetail().getDatapub() , storyGlobal.get(k).get(i).getStoryModelDetail().getTitleStory(),"ar"));


                        }
                        else if(storyGlobal.get(k).get(i).getStoryModelDetail().getType().equals("video"))
                        {

                            storyItem.add(new StoryItem.Video(storyGlobal.get(k).get(i).getStoryModelDetail().getLink(),"https://snrtnews.aramobile.com"+storyGlobal.get(k).get(i).getAvatarJournaliste(),storyGlobal.get(k).get(i).getNameJournaliste(), storyGlobal.get(k).get(i).getStoryModelDetail().getDatapub(),storyGlobal.get(k).get(i).getStoryModelDetail().getTitleStory(),"ar"));

                        }
                    }

                    storyuser.add(storyItem);
                }



                story = new SnrtNewsStoryAr(StoryActivity.this, container, storyuser, new StoryCallback() {
                    @Override
                    public void onNextCalled(@NotNull StoryItem storyItem, int index)
                    {

                    }


                    @Override
                    public void done()
                    {


                    }
                },mProgressDrawable);


                story.start(0);



            }





            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
            }
        });
    }
}



```
