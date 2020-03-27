package ma.snrt.news;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.SnrtNews.storysnrt.SnrtNewsStory;
import com.SnrtNews.storysnrt.StoryItem;
import com.SnrtNews.storysnrt.callbacks.StoryCallback;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import ma.snrt.news.model.Story;
import ma.snrt.news.model.User;
import ma.snrt.news.network.ApiCall;
import ma.snrt.news.ui.TextViewBold;
import ma.snrt.news.ui.TextViewExtraBold;
import ma.snrt.news.ui.TextViewRegular;
import ma.snrt.news.util.Cache;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class StoryActivity extends AppCompatActivity
{

    private SnrtNewsStory story;
    private int mProgressDrawable = R.drawable.white_lightgrey_drawable;

    private List<StoryItem> storyItem;
    private ConstraintLayout container;
    private ImageView  likeBtn, pauseBtn;
    TextViewRegular positionTextView, likeTextView;
    boolean isStoryPaused;
    private User user;
    int storyIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        container = findViewById(R.id.container);
        pauseBtn = findViewById(R.id.story_pause);
        positionTextView = findViewById(R.id.story_pin_text);
        likeTextView = findViewById(R.id.story_like_text);
        likeBtn = findViewById(R.id.story_like);

        user = (User) getIntent().getSerializableExtra("user");
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
        storyItem = new ArrayList<>();

        for(int i =0; i<user.getStories().size();i++)
        {
            if(user.getStories().get(i).getType().equals("image"))
                storyItem.add(new StoryItem.RemoteImage(user.getStories().get(i).getImage(), 5, user.getImage() , user.getName(), user.getStories().get(i).getDatePublication() , user.getStories().get(i).getTitle()));
            else if(user.getStories().get(i).getType().equals("video"))
                storyItem.add(new StoryItem.Video(user.getStories().get(i).getLink(),user.getImage() , user.getName(), user.getStories().get(i).getDatePublication() , user.getStories().get(i).getTitle()));

        }
        story = new SnrtNewsStory(StoryActivity.this, container, storyItem, new StoryCallback() {
            @Override
            public void onNextCalled(@NotNull StoryItem storyItem, int index)
            {
                storyIndex = index;
                Story currentStory = user.getStories().get(index);
                if(Cache.existsInLikes(currentStory.getId()+""))
                    likeBtn.setImageResource(R.drawable.like_full);
                else
                    likeBtn.setImageResource(R.drawable.story_like);
                positionTextView.setText(currentStory.getPosition());
                likeTextView.setText(currentStory.getLikes_numbers()+"");
            }

            @Override
            public void done()
            {

            }
        },mProgressDrawable);
        story.start();
    }

    private void postLike(final boolean like,final int postId){
        ApiCall.likePost(like, postId, "story", new Callback<JsonObject>(){
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if(response.isSuccessful() && response.body()!=null){
                    if(response.body().has("status") && response.body().get("status").getAsBoolean()){
                        if(like){
                            Cache.likePost(postId+"");
                            likeBtn.setImageResource(R.drawable.like_full);
                        }
                        else{
                            Cache.unLikePost(postId+"");
                            likeBtn.setImageResource(R.drawable.story_like);
                        }
                        return;
                    }
                }
                Toast.makeText(StoryActivity.this, getString(R.string.api_error), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(StoryActivity.this, getString(R.string.api_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.story_close:
                finish();
                break;
            case R.id.story_pause:
                if(isStoryPaused) {
                    story.resume();
                    pauseBtn.setImageResource(R.drawable.story_pause);
                }
                else {
                    story.pause(false);
                    pauseBtn.setImageResource(R.drawable.play_story);
                }
                isStoryPaused = !isStoryPaused;
                break;
            case R.id.story_like:
                postLike(!Cache.existsInLikes(user.getStories().get(storyIndex).getId()+""), user.getStories().get(storyIndex).getId());
                break;
            case R.id.reverse:
                story.prev();
                break;
            case R.id.skip:
                story.next();
                break;
        }
    }
}
