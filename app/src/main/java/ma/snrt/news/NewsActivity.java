package ma.snrt.news;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import ma.snrt.news.adapter.NewsAdapter;
import ma.snrt.news.model.Post;
import ma.snrt.news.network.ApiCall;
import ma.snrt.news.network.GsonHelper;
import ma.snrt.news.ui.TextViewBold;
import ma.snrt.news.ui.TextViewRegular;
import ma.snrt.news.ui.TextViewExtraBold;
import ma.snrt.news.util.Cache;
import ma.snrt.news.util.MyContextWrapper;
import ma.snrt.news.util.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TextViewRegular emptyTextView;
    TextViewBold titleTextView;
    ProgressBar progressBar;
    ArrayList<Post> posts;
    int currentPage = 0;
    NewsAdapter newsAdapter;
    String tag;
    boolean isListLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isNightModeEnabled = AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false);
        if (isNightModeEnabled) {
            setTheme(R.style.ActivityThemeDark);
        }
        setContentView(R.layout.activity_news);

        Cache.initFavorisVideos();
        Cache.initFavoris();

        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayUseLogoEnabled(false);


        recyclerView = findViewById(R.id.recyclerview);
        emptyTextView = findViewById(R.id.empty_textview);
        progressBar = findViewById(R.id.progress_bar);
        titleTextView = findViewById(R.id.news_title);

        recyclerView.setItemAnimator(null);

        if(!getResources().getBoolean(R.bool.is_tablet)) {
            final LinearLayoutManager llm = new LinearLayoutManager(this);
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(llm);
        }
        else{
            final GridLayoutManager lm = new GridLayoutManager(this, 3);
            lm.setOrientation(LinearLayoutManager.VERTICAL);
            lm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if(position==0 || position%4==0)
                        return 3;
                    return 1;
                }
            });
            recyclerView.setLayoutManager(lm);
        }
        recyclerView.setHasFixedSize(false);

        tag = getIntent().getStringExtra("tag");

        titleTextView.setText("#"+tag);

        posts = new ArrayList<>();

        /*if(AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
            Glide.with(this).load(R.raw.loader_dark).into(progressBar);
        else
            Glide.with(this).load(R.raw.loader).into(progressBar);*/

       callNewsApi();
    }

    private void callNewsApi(){
        if(currentPage==0)
            progressBar.setVisibility(View.VISIBLE);
        ApiCall.getNewsByCatOrTag(false, 0, tag, currentPage, new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if(progressBar!=null)
                    progressBar.setVisibility(View.GONE);
                if(response.body()!=null && response.isSuccessful()){
                    ArrayList<Post> result = GsonHelper.getGson().fromJson(response.body(), new TypeToken<List<Post>>(){}.getType());
                    if(result.size()>0)
                        posts.addAll(result);
                    else
                        isListLoaded = true;
                }
                else {
                    if(currentPage == 0) {
                        Toast.makeText(NewsActivity.this, getString(R.string.error_load_data), Toast.LENGTH_SHORT).show();
                        isListLoaded = true;
                    }
                }
                setListAdapter();
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                if(progressBar!=null)
                    progressBar.setVisibility(View.GONE);
                if(currentPage==0) {
                    Toast.makeText(NewsActivity.this, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
                    isListLoaded = true;
                }
                setListAdapter();
            }
        });
    }

    private void setListAdapter(){
        if(posts.size()>0){
                if (currentPage == 0) {
                    newsAdapter = new NewsAdapter(this, posts, recyclerView);
                    recyclerView.setAdapter(newsAdapter);
                    newsAdapter.setOnLoadMoreListener(new NewsAdapter.OnLoadMoreListener() {
                        @Override
                        public void onLoadMore() {
                            if (!isListLoaded) {
                                currentPage++;
                                callNewsApi();
                            }
                        }
                    });
                } else
                    newsAdapter.setLoaded();
        }
        else{
            recyclerView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.VISIBLE);
        }
    }

    public void onClick(View view){
        if(view.getId() == R.id.back_btn)
            finish();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(MyContextWrapper.wrap(newBase, Utils.getAppCurrentLang()));
    }
}
