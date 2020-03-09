package ma.snrt.news;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.RawResourceDataSource;
import com.google.android.exoplayer2.util.Util;

import ma.snrt.news.model.Post;
import ma.snrt.news.util.Utils;

public class SplashActivity extends AppCompatActivity {
    PlayerView playerView;
    SimpleExoPlayer player;
    TextView frBtn, arBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        playerView = findViewById(R.id.player_view);
        frBtn = findViewById(R.id.lang_fr_btn);
        arBtn = findViewById(R.id.lang_ar_btn);
        player = ExoPlayerFactory.newSimpleInstance(this);
        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
        playerView.setPlayer(player);
        player.setPlayWhenReady(true);

        Log.e("Splash", "fcm token: "+ AppController.getSharedPreferences().getString("device_token", ""));
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, getString(R.string.app_name)));

        MediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .setExtractorsFactory(new DefaultExtractorsFactory())
                .createMediaSource(RawResourceDataSource.buildRawResourceUri(R.raw.splash_video));
        player.prepare(mediaSource);
        player.addListener(new Player.EventListener() {
            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Log.e("PlayerActivity", "exo player error: "+error.getMessage());
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == Player.STATE_ENDED){
                    //player back ended
                    if(Utils.getAppCurrentLang().equals("")){
                        frBtn.setVisibility(View.VISIBLE);
                        arBtn.setVisibility(View.VISIBLE);
                    }
                    else {
                        Bundle extras = getIntent().getExtras();
                        if(extras!=null && extras.containsKey("id")){
                            Post post = new Post();
                            post.setId(Integer.parseInt(extras.getString("id")));
                            post.setTitle(extras.getString("title"));
                            post.setDatePublication(extras.getString("date"));
                            post.setImage(extras.getString("image"));
                            post.setPosition(extras.getString("localisation"));
                            post.setTags(extras.getString("tags"));
                            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                            intent.putExtra("post", post);
                            startActivity(intent);
                            finish();
                            return;
                        }
                        startMainActivity();
                    }
                }
            }
        });




    }

    public void startMainActivity(){
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        finish();
    }

    public void setAppLang(String lang){
        SharedPreferences.Editor editor = AppController.getSharedPreferences().edit();
        editor.putString("lang", lang);
        editor.commit();
        startMainActivity();
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.lang_fr_btn:
                setAppLang("fr");
                break;
            case R.id.lang_ar_btn:
                setAppLang("ar");
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if(player!=null)
            player.release();
        super.onDestroy();
    }
}
