package ma.snrt.news;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
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
    //SimpleExoPlayer player;
    TextView frBtn, arBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isNightModeEnabled = AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false);
        if (isNightModeEnabled) {
            setTheme(R.style.AppThemeDark);
        }
        setContentView(R.layout.activity_splash);
        playerView = findViewById(R.id.player_view);
        frBtn = findViewById(R.id.lang_fr_btn);
        arBtn = findViewById(R.id.lang_ar_btn);
        if(isNightModeEnabled)
            arBtn.setBackgroundResource(R.drawable.btn_white);
        else
            arBtn.setBackgroundResource(R.drawable.btn_black);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
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
        }, 2000);

    }

    public void startMainActivity(){
        if(AppController.getSharedPreferences().getBoolean("isTutorialShowed", false))
            startActivity(new Intent(this, MainActivity.class));
        else
            startActivity(new Intent(this, TutorialActivity.class));
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
        /*if(player!=null)
            player.release();*/
        super.onDestroy();
    }
}
