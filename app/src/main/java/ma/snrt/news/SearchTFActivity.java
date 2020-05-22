package ma.snrt.news;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import ma.snrt.news.adapter.NewsAdapter;
import ma.snrt.news.adapter.NewsFavAdapter;
import ma.snrt.news.adapter.NewsVidAdapter;
import ma.snrt.news.adapter.Videos2Adapter;
import ma.snrt.news.model.Category;
import ma.snrt.news.model.Post;
import ma.snrt.news.network.ApiCall;
import ma.snrt.news.network.GsonHelper;
import ma.snrt.news.ui.TextViewExtraBold;
import ma.snrt.news.ui.TextViewRegular;
import ma.snrt.news.util.Cache;
import ma.snrt.news.util.MyContextWrapper;
import ma.snrt.news.util.PermissionsHelper;
import ma.snrt.news.util.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchTFActivity extends AppCompatActivity {

    private static final int RESULT_SPEECH = 112;
    RecyclerView recyclerView;
    TextViewRegular emptyTextView, resultCountTV, cancelBtn;
    ProgressBar progressBar;
    ImageView searchBtn, micBtn;
    RelativeLayout listLayout, micLayout;
    AppCompatEditText searchEditText;
    ArrayList<Post> posts;
    int currentPage = 0;
    boolean isListLoaded, isMicLayoutVisible;
    NewsFavAdapter newsAdapter;
    ImageView searchByMicBtn;
    Category category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isNightModeEnabled = AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false);
        if (isNightModeEnabled) {
            setTheme(R.style.ActivityThemeDark);
        }
        setContentView(R.layout.activity_search_tf);
        Cache.initFavoris();

        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayUseLogoEnabled(false);

        category = (Category) getIntent().getSerializableExtra("category");
        recyclerView = findViewById(R.id.recyclerview);
        emptyTextView = findViewById(R.id.empty_textview);
        progressBar = findViewById(R.id.progress_bar);
        searchBtn = findViewById(R.id.search_btn);
        micBtn = findViewById(R.id.mic_btn);
        listLayout = findViewById(R.id.search_list_layout);
        micLayout = findViewById(R.id.search_mic_layout);
        searchEditText = findViewById(R.id.search_edittext);
        searchByMicBtn = findViewById(R.id.search_mic_btn);
        resultCountTV = findViewById(R.id.resultCountTextView);
        cancelBtn = findViewById(R.id.cancelSearchBtn);

        recyclerView.setItemAnimator(null);

        if(!getResources().getBoolean(R.bool.is_tablet)) {
            final LinearLayoutManager llm = new LinearLayoutManager(this);
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(llm);
        }
        else{
            final GridLayoutManager lm = new GridLayoutManager(this, 2);
            lm.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(lm);
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) recyclerView.getLayoutParams();
            int margin = Utils.dpToPx(getResources(), 5);
            lp.setMargins(margin, margin *2 , margin, margin * 2);
            recyclerView.setLayoutParams(lp);
        }
        recyclerView.setHasFixedSize(true);

        posts = new ArrayList<>();

        /*if(AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
            Glide.with(this).load(R.raw.loader_dark).into(progressBar);
        else
            Glide.with(this).load(R.raw.loader).into(progressBar);
        */
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.mic_btn:
                        micLayout.setVisibility(View.VISIBLE);
                        listLayout.setVisibility(View.GONE);
                        break;
                    case R.id.search_mic_btn:
                        if(PermissionsHelper.canAccessMicrophone(SearchTFActivity.this))
                            searchByMic();
                        else
                            PermissionsHelper.askMicrophonePermission(SearchTFActivity.this);
                        break;
                    case R.id.cancelSearchBtn:
                        searchEditText.setText("");
                        currentPage=0;
                        posts.clear();
                        listLayout.setVisibility(View.GONE);
                        micLayout.setVisibility(View.GONE);
                        break;
                }
            }
        };

        micBtn.setOnClickListener(onClickListener);
        searchByMicBtn.setOnClickListener(onClickListener);
        cancelBtn.setOnClickListener(onClickListener);

        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH)
                {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    currentPage = 0;
                    posts.clear();
                    isListLoaded = false;
                    callSearchApi();
                    return true;
                }
                return false;
            }
        });

    }

    private void searchByMic() {
        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            if(Utils.getAppCurrentLang().equals("fr"))
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fr-FR");
            else {
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-MA");
            }
            startActivityForResult(intent, RESULT_SPEECH);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(this,getString(R.string.stt_not_supported),Toast.LENGTH_SHORT).show();
        }
    }

    private void callSearchApi(){
        Utils.closeKeyboard(this, searchEditText);
        if(searchEditText.getText().toString().isEmpty()){
            currentPage = 0;
            posts.clear();
            isListLoaded = false;
            recyclerView.setAdapter(null);
            return;
        }
        if(currentPage==0) {
            listLayout.setVisibility(View.VISIBLE);
            micLayout.setVisibility(View.GONE);
            isMicLayoutVisible = false;
            emptyTextView.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }
        ApiCall.searchNewsByCat(category.getId(), searchEditText.getText().toString(), currentPage, new Callback<JsonArray>() {
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
                        Toast.makeText(SearchTFActivity.this, getString(R.string.error_load_data), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(SearchTFActivity.this, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
                    isListLoaded = true;
                }
                setListAdapter();
            }
        });
    }

    private void setListAdapter(){
        if(posts.size()>0){
            resultCountTV.setText(posts.size()+" "+getString(R.string.results));
            recyclerView.setVisibility(View.VISIBLE);
            emptyTextView.setVisibility(View.GONE);
                if (currentPage == 0) {
                    newsAdapter = new NewsFavAdapter(this, posts, recyclerView);
                    recyclerView.setAdapter(newsAdapter);
                    newsAdapter.setOnLoadMoreListener(new NewsFavAdapter.OnLoadMoreListener() {
                        @Override
                        public void onLoadMore() {
                            if (!isListLoaded) {
                                currentPage++;
                                callSearchApi();
                            }
                        }
                    });
                } else
                    newsAdapter.setLoaded();
        }
        else{
            resultCountTV.setText("0 "+getString(R.string.results));
            recyclerView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.VISIBLE);
        }
    }

    public void onClick(View view){
        if(view.getId() == R.id.back_btn)
            finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESULT_SPEECH: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if(text.size()>0){
                        searchEditText.setText(text.get(0));
                        currentPage = 0;
                        posts.clear();
                        isListLoaded = false;
                        callSearchApi();
                    }
                }break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if(requestCode == PermissionsHelper.MICROPHONE){
            boolean permissionGranted = true;
            for (int grantResult : grantResults) {
                permissionGranted &= grantResult == PackageManager.PERMISSION_GRANTED;
            }
            if(permissionGranted)
                searchByMic();
            else
                Toast.makeText(this, getString(R.string.permissions_needed), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(MyContextWrapper.wrap(newBase, Utils.getAppCurrentLang()));
    }
}
