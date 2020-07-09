package ma.snrt.news;

import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import ma.snrt.news.util.Utils;

public class PopupPlayerActivity extends AppCompatActivity {
    PlayerView playerView;
    RelativeLayout playerControls;
    ImageView fullscreenImage, playBtn, pauseBtn, thumbImageView;
    TextView timeTextView;
    String videoUrl;
    SimpleExoPlayer player;
    boolean isFullScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup_player);
        playerView = findViewById(R.id.player_view);
        playerControls = findViewById(R.id.player_controls);
        fullscreenImage = findViewById(R.id.exo_fullscreen);
        playBtn = findViewById(R.id.exo_play);
        pauseBtn = findViewById(R.id.exo_pause);
        timeTextView = findViewById(R.id.exo_position);
        thumbImageView = findViewById(R.id.video_thumb);

        videoUrl = getIntent().getStringExtra("video_uri");
        loadThumbImage();

        player = ExoPlayerFactory.newSimpleInstance(this);
        playerView.setPlayer(player);
        player.setPlayWhenReady(true);

        playVideo();
    }

    private void playVideo() {
        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, getString(R.string.app_name)));
        // This is the MediaSource representing the media to be played.

        MediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .setExtractorsFactory(new DefaultExtractorsFactory())
                .createMediaSource(Uri.parse(videoUrl));
        player.prepare(mediaSource);
        player.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == Player.STATE_READY){
                    thumbImageView.setVisibility(View.GONE);
                }
            }
        });
    }

    private void loadThumbImage() {
        final String image = getIntent().getStringExtra("video_image");

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.placeholder);
        requestOptions.error(R.drawable.placeholder);
        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL);

        Glide.with(this)
                .load(image)
                .apply(requestOptions)
                .into(thumbImageView);
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.root: case R.id.close_btn:
                finish();
                overridePendingTransition(R.anim.slide_out_bottom, R.anim.slide_in_bottom);
                break;
            case R.id.exo_fullscreen:
                toggleFullScreen();
                break;
        }
    }

    private void toggleFullScreen() {
        if(!isFullScreen) {
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            playerView.setLayoutParams(lp);
            FrameLayout.LayoutParams lp2 = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            playerControls.setLayoutParams(lp2);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            findViewById(R.id.close_btn).setVisibility(View.GONE);
            fullscreenImage.setImageResource(R.drawable.exo_controls_fullscreen_exit);

            playBtn.getLayoutParams().width = Utils.dpToPx(getResources(), getResources().getDimensionPixelSize(R.dimen.play_btn));
            pauseBtn.getLayoutParams().width = Utils.dpToPx(getResources(), getResources().getDimensionPixelSize(R.dimen.play_btn));
            fullscreenImage.getLayoutParams().width = Utils.dpToPx(getResources(), getResources().getDimensionPixelSize(R.dimen.small_icon_size));
            timeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

            playerControls.setPadding(0, 0, 0, Utils.dpToPx(getResources(), 10));
        }
        else{
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dpToPx(getResources(), 220));
            lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            playerView.setLayoutParams(lp);
            FrameLayout.LayoutParams lp2 = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dpToPx(getResources(), 220));
            playerControls.setLayoutParams(lp2);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            findViewById(R.id.close_btn).setVisibility(View.VISIBLE);
            fullscreenImage.setImageResource(R.drawable.ic_fullscreen);
            playBtn.getLayoutParams().width = getResources().getDimensionPixelSize(R.dimen.small_play_btn);
            pauseBtn.getLayoutParams().width = getResources().getDimensionPixelSize(R.dimen.small_play_btn);
            fullscreenImage.getLayoutParams().width = getResources().getDimensionPixelSize(R.dimen.standard_icon_size);
            timeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            playerControls.setPadding(0, 0, 0, 0);
        }
        isFullScreen = !isFullScreen;
    }

    @Override
    protected void onDestroy() {
        if(player!=null)
            player.release();
        if(playerView!=null)
            playerView.setVisibility(View.GONE);
        if(thumbImageView!=null)
            thumbImageView.setVisibility(View.VISIBLE);
        super.onDestroy();
    }
}
