package ma.snrt.news;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.dailymotion.android.player.sdk.PlayerWebView;

public class DailymotionActivity extends AppCompatActivity {
    PlayerWebView playerWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dailymotion);

        playerWebView = findViewById(R.id.dm_player_web_view);
        String id = getIntent().getStringExtra("url");
        playerWebView.load(id);
    }

    @Override
    protected void onPause() {
        super.onPause();
        playerWebView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        playerWebView.onResume();
    }
}
