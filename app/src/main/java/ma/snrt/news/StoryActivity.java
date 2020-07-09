package ma.snrt.news;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.SnrtNews.storysnrt.SnrtNewsStoryAr;
import com.SnrtNews.storysnrt.SnrtNewsStoryFr;
import com.SnrtNews.storysnrt.StoryItem;
import com.SnrtNews.storysnrt.callbacks.StoryCallback;
import com.thefuntasty.hauler.DragDirection;
import com.thefuntasty.hauler.HaulerView;
import com.thefuntasty.hauler.OnDragDismissedListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import ma.snrt.news.model.Story;
import ma.snrt.news.model.User;
import ma.snrt.news.ui.TextViewRegular;
import ma.snrt.news.util.Utils;


public class StoryActivity extends AppCompatActivity
{

    SnrtNewsStoryFr storyFr;
    SnrtNewsStoryAr storyAr;
    private int mProgressDrawable = R.drawable.white_lightgrey_drawable;

    private ConstraintLayout container;
    TextViewRegular positionTextView, likeTextView;
    //int storyIndex = 0;
    int userIndex = 0;
    private List<StoryItem> storyItem;
    private List<List<StoryItem>> storyuser;
    ArrayList<User> users;
    private HaulerView advancedHaulerView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.zoom_in, R.anim.zoom_in);
        setContentView(R.layout.activity_story);

        container = findViewById(R.id.container);
        advancedHaulerView = findViewById(R.id.advancedHaulerView);
        positionTextView = findViewById(R.id.story_pin_text);
        likeTextView = findViewById(R.id.story_like_text);

        users = (ArrayList<User>) getIntent().getSerializableExtra("users");
        userIndex = getIntent().getIntExtra("user_index", 0);
        advancedHaulerView.setOnDragDismissedListener(new OnDragDismissedListener() {
            @Override
            public void onDismissed(@NotNull DragDirection dragDirection) {

                finish();
                overridePendingTransition(R.anim.slide_out_bottom, R.anim.slide_in_bottom);
            }
        });
        getStoryData();
    }


    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onPause() {
        if(storyFr!=null)
            storyFr.pause(false);
        if(storyAr!=null)
            storyAr.pause(false);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if(storyFr!=null)
            storyFr.release();
        if(storyAr!=null)
            storyAr.release();
        super.onDestroy();
    }

    private void getStoryData() {

        storyuser = new ArrayList<>();

        for(int k =0; k<users.size();k++)
        {
            storyItem = new ArrayList<>();
            User user = users.get(k);
            for(int i =0; i< user.getStories().size();i++)
            {
                Story story = users.get(k).getStories().get(i);
                if(story.getType().equals("image"))
                    storyItem.add(new StoryItem.RemoteImage(story.getImage(), 5,user.getImage() , user.getName() , Utils.getPostRelativeDate(this, story.getDatePublication()) , story.getTitle(), Utils.getAppCurrentLang()));

                else if(story.getType().equals("video"))
                    storyItem.add(new StoryItem.Video(story.getLink(), user.getImage() , user.getName() , Utils.getPostRelativeDate(this, story.getDatePublication()) , story.getTitle(),Utils.getAppCurrentLang()));
            }

            storyuser.add(storyItem);
        }
        if(Utils.getAppCurrentLang().equals("fr")) {
            storyFr = new SnrtNewsStoryFr(this, container, storyuser, new StoryCallback() {
                @Override
                public void onNextCalled(@NotNull StoryItem storyItem, int i) {

                }

                @Override
                public void done() {

                }
            }, mProgressDrawable);
            storyFr.start(userIndex);
        }
        else{
            storyAr = new SnrtNewsStoryAr(this, container, storyuser, new StoryCallback() {
                @Override
                public void onNextCalled(@NotNull StoryItem storyItem, int i) {

                }

                @Override
                public void done() {

                }
            }, mProgressDrawable);
            storyAr.start(userIndex);
        }
    }
}
