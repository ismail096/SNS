package ma.snrt.news;

import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.gson.JsonObject;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import ma.snrt.news.model.Story;
import ma.snrt.news.model.User;
import ma.snrt.news.network.ApiCall;
import ma.snrt.news.ui.TextViewBold;
import ma.snrt.news.ui.TextViewRegular;
import ma.snrt.news.ui.TextViewExtraBold;
import ma.snrt.news.ui.StoryStatusView;
import ma.snrt.news.util.Cache;
import ma.snrt.news.util.DateTimeUtils;
import ma.snrt.news.util.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatusStoriesActivity extends AppCompatActivity implements StoryStatusView.UserInteractionListener {

    public static final String STATUS_RESOURCES_KEY = "statusStoriesResources";

    private static StoryStatusView storyStatusView;
    private ImageView image, likeBtn, pauseBtn;
    CircleImageView userImageView;
    TextViewExtraBold usernameTextView, dateTextView;
    TextViewBold descTextView;
    TextViewRegular positionTextView, likeTextView;
    PlayerView playerView;
    SimpleExoPlayer player;
    ArrayList<Story> stories;
    User user;
    private long statusDuration = 10000L;
    private boolean isImmersive = true;
    boolean isStoryPaused;
    private int counter = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_stories);
        Cache.initLikedPosts();
        user =(User) getIntent().getSerializableExtra(STATUS_RESOURCES_KEY);
        stories = user.getStories();
        image = findViewById(R.id.image);
        storyStatusView = findViewById(R.id.storiesStatus);
        userImageView = findViewById(R.id.user_image);
        usernameTextView = findViewById(R.id.user_name);
        dateTextView = findViewById(R.id.story_date);
        positionTextView = findViewById(R.id.story_pin_text);
        likeTextView = findViewById(R.id.story_like_text);
        likeBtn = findViewById(R.id.story_like);
        descTextView = findViewById(R.id.story_text);
        playerView = findViewById(R.id.player_view);
        pauseBtn = findViewById(R.id.story_pause);

        storyStatusView.setStoriesCount(stories.size());
        storyStatusView.setStoryDuration(statusDuration);
        // or
        storyStatusView.setUserInteractionListener(this);
        storyStatusView.playStories();

        player = ExoPlayerFactory.newSimpleInstance(this);
        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
        playerView.setPlayer(player);
        player.setPlayWhenReady(true);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storyStatusView.skip();
            }
        });

        setImage(userImageView, user.getImage(), false);
        usernameTextView.setText(user.getName());
        loadStory();

        findViewById(R.id.actions).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    storyStatusView.pause();
                    pauseBtn.setImageResource(R.drawable.play_story);
                    if(stories.get(counter).getType().equals("video"))
                        pausePlayer();
                    isStoryPaused = true;
                    return true;
                }
                if(motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
                    storyStatusView.resume();
                    pauseBtn.setImageResource(R.drawable.story_pause);
                    if(stories.get(counter).getType().equals("video"))
                        startPlayer();
                    isStoryPaused = false;
                    return true;
                }
                return false;
            }
        });
    }

    private void loadStory() {
        Story story = stories.get(counter);
        isStoryPaused = true;
        storyStatusView.pause();
        if(Utils.getAppCurrentLang().equals("fr")) {
            String result = DateTimeUtils.getTimeAgoFr(DateTimeUtils.getTimeStamp(story.getDatePublication()));
            dateTextView.setText(result);
        }
        else
            dateTextView.setText(DateTimeUtils.getTimeAgoAr(DateTimeUtils.getTimeStamp(story.getDatePublication())));
        if(stories.get(counter).getPosition()!=null)
            positionTextView.setText(story.getPosition());
        else
            positionTextView.setText(story.getPosition());
        likeTextView.setText(story.getLikes_numbers()+"");
        descTextView.setText(story.getTitle());
        if(Cache.existsInLikes(story.getId()+""))
            likeBtn.setImageResource(R.drawable.like_full);
        else
            likeBtn.setImageResource(R.drawable.story_like);
        if(story.getType().equals("image")) {
            player.stop();
            playerView.setVisibility(View.GONE);
            image.setVisibility(View.VISIBLE);
            setImage(image, story.getImage(), true);
        }
        else
        {
            player.stop();
            playerView.setVisibility(View.VISIBLE);
            image.setVisibility(View.GONE);
            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                    Util.getUserAgent(this, getString(R.string.app_name)));
            MediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                    .setExtractorsFactory(new DefaultExtractorsFactory())
                    .createMediaSource(Uri.parse(story.getLink()));
            player.prepare(mediaSource);
            storyStatusView.resume();
            startPlayer();
            pauseBtn.setImageResource(R.drawable.story_pause);
            isStoryPaused = false;
        }
    }

    public void setImage(final ImageView imageView, final String url, final boolean resumeStory){
        try {
            Picasso.with(StatusStoriesActivity.this)
                    .load(url)
                    .noFade()
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .error(R.drawable.placeholder)
                    .into(imageView, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            if(storyStatusView!=null && resumeStory) {
                                isStoryPaused= false;
                                storyStatusView.resume();
                                imageView.setAlpha(0f);
                                imageView.animate().setDuration(200).alpha(1f).start();
                            }
                        }

                        @Override
                        public void onError() {
                            Picasso.with(StatusStoriesActivity.this)
                                    .load(url)
                                    .noFade()
                                    .error(R.drawable.placeholder)
                                    .into(imageView, new com.squareup.picasso.Callback() {
                                        @Override
                                        public void onSuccess() {
                                            if(storyStatusView!=null && resumeStory) {
                                                isStoryPaused= false;
                                                storyStatusView.resume();
                                                imageView.setAlpha(0f);
                                                imageView.animate().setDuration(200).alpha(1f).start();
                                            }
                                        }
                                        @Override
                                        public void onError() {
                                            if(storyStatusView!=null && resumeStory) {
                                                isStoryPaused= false;
                                                storyStatusView.resume();
                                                imageView.setAlpha(0f);
                                                imageView.animate().setDuration(200).alpha(1f).start();
                                            }
                                        }
                                    });
                        }
                    });
        } catch (Exception ex) {
            imageView.setImageResource(R.drawable.placeholder);
            if(storyStatusView!=null && resumeStory) {
                isStoryPaused= false;
                storyStatusView.resume();
                imageView.setAlpha(0f);
                imageView.animate().setDuration(200).alpha(1f).start();
            }
        }
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
                Toast.makeText(StatusStoriesActivity.this, getString(R.string.api_error), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(StatusStoriesActivity.this, getString(R.string.api_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onNext() {
        storyStatusView.pause();
        ++counter;
        loadStory();
    }

    @Override
    public void onPrev() {
        if (counter - 1 < 0) return;
        storyStatusView.pause();
        --counter;
        loadStory();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (isImmersive && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            if (hasFocus) {
                getWindow().getDecorView()
                        .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
        }
    }

    @Override
    public void onComplete() {
        finish();
    }

    private void pausePlayer(){
        player.setPlayWhenReady(false);
        player.getPlaybackState();


    }
    private void startPlayer(){
        player.setPlayWhenReady(true);
        player.getPlaybackState();
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.story_close:
                finish();
                break;
            case R.id.story_pin:

                break;
            case R.id.story_pause:
                if(isStoryPaused) {
                    storyStatusView.resume();
                    pauseBtn.setImageResource(R.drawable.story_pause);
                    if(stories.get(counter).getType().equals("video"))
                        startPlayer();
                }
                else {
                    storyStatusView.pause();
                    pauseBtn.setImageResource(R.drawable.play_story);
                    if(stories.get(counter).getType().equals("video"))
                        pausePlayer();
                }
                isStoryPaused = !isStoryPaused;
                break;
            case R.id.story_like:
                postLike(!Cache.existsInLikes(stories.get(counter).getId()+""), stories.get(counter).getId());
                break;
            case R.id.reverse:
                storyStatusView.reverse();
                break;
            case R.id.skip:
                storyStatusView.skip();
                break;
        }
    }
    @Override
    protected void onDestroy() {
        // Very important !
        storyStatusView.destroy();
        if(player!=null)
            player.release();
        super.onDestroy();
    }
}
