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
        /*HlsMediaSource videoSource =
                new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse("https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8"));        // Prepare the player with the source.
        player.prepare(videoSource);*/

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
        try {
            Picasso.with(this)
                    .load(image)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .error(R.drawable.placeholder)
                    .into(thumbImageView, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(PopupPlayerActivity.this)
                                    .load(image)
                                    .error(R.drawable.placeholder)
                                    .into(thumbImageView);
                        }
                    });
        } catch (Exception ex) {
            thumbImageView.setImageResource(R.drawable.placeholder);
        }
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.root: case R.id.close_btn:
                finish();
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

            playBtn.getLayoutParams().width = Utils.dpToPx(getResources(), 120);
            pauseBtn.getLayoutParams().width = Utils.dpToPx(getResources(), 120);
            fullscreenImage.getLayoutParams().width = Utils.dpToPx(getResources(), 48);
            timeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

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
            playBtn.getLayoutParams().width = Utils.dpToPx(getResources(), 80);
            pauseBtn.getLayoutParams().width = Utils.dpToPx(getResources(), 80);
            fullscreenImage.getLayoutParams().width = Utils.dpToPx(getResources(), 24);
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
