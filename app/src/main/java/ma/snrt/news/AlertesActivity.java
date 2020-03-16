package ma.snrt.news;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.suke.widget.SwitchButton;

import java.util.ArrayList;
import java.util.List;

import ma.snrt.news.adapter.AlertesAdapter;
import ma.snrt.news.model.Category;
import ma.snrt.news.network.ApiCall;
import ma.snrt.news.network.GsonHelper;
import ma.snrt.news.ui.TextViewRegular;
import ma.snrt.news.util.Cache;
import ma.snrt.news.util.MyContextWrapper;
import ma.snrt.news.util.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlertesActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    ImageView progressBar;
    TextViewRegular emptyTextView;
    ArrayList<Category> categories;
    SwitchButton globalBtn, storyBtn;
    NestedScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean isNightModeEnabled = AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false);
        if (isNightModeEnabled) {
            setTheme(R.style.ActivityThemeDark);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alertes);
        scrollView = findViewById(R.id.nested_scroll);
        recyclerView = findViewById(R.id.recyclerview);
        progressBar = findViewById(R.id.progress_bar);
        emptyTextView = findViewById(R.id.empty_textview);
        globalBtn = findViewById(R.id.config_switch);
        storyBtn = findViewById(R.id.story_switch);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(llm);

        if(AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
            Glide.with(this).load(R.raw.loader_dark).into(progressBar);
        else
            Glide.with(this).load(R.raw.loader).into(progressBar);
        categories = new ArrayList<>();
        getCategoriesFromApi();
    }

    private void getCategoriesFromApi(){
        progressBar.setVisibility(View.VISIBLE);
        ApiCall.getCategories(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if(progressBar!=null)
                    progressBar.setVisibility(View.GONE);
                if(response.body()!=null && response.isSuccessful()){
                    Cache.putPermanentObject(response.body().toString(), "categories_"+ Utils.getAppCurrentLang());
                    categories = GsonHelper.getGson().fromJson(response.body(), new TypeToken<List<Category>>(){}.getType());
                }
                else {
                    String resultFromCache = (String) Cache.getPermanentObject("categories_"+ Utils.getAppCurrentLang());
                    if(resultFromCache!=null)
                        categories = GsonHelper.getGson().fromJson(resultFromCache, new TypeToken<List<Category>>(){}.getType());
                    Toast.makeText(AlertesActivity.this, getString(R.string.error_load_data), Toast.LENGTH_SHORT).show();
                }
                setAdapter();
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                if(progressBar!=null)
                    progressBar.setVisibility(View.GONE);
                String resultFromCache = (String) Cache.getPermanentObject("categories_"+ Utils.getAppCurrentLang());
                if(resultFromCache!=null)
                    categories = GsonHelper.getGson().fromJson(resultFromCache, new TypeToken<List<Category>>(){}.getType());
                Toast.makeText(AlertesActivity.this, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
                setAdapter();
            }
        });
    }

    private void setAdapter(){
        if(categories.size()>0){
            Category category = new Category();
            if(Utils.getAppCurrentLang().equals("fr"))
                category.setId(12040);
            else
                category.setId(12041);
            category.setTitle(getString(R.string.agenda));
            category.setColor("#eeff41");
            categories.add(category);
            AlertesAdapter adapter = new AlertesAdapter(this, categories);
            recyclerView.setAdapter(adapter);
            scrollView.setVisibility(View.VISIBLE);
            handleSwitches();
        }
        else{
            scrollView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.VISIBLE);
        }
    }

    private void handleSwitches(){
        if(AppController.getSharedPreferences().contains("notif_enabled")) {
            if (AppController.getSharedPreferences().getBoolean("notif_enabled", false)) {
                globalBtn.setChecked(true);
                if(AppController.getSharedPreferences().getBoolean("notif_story", false))
                    storyBtn.setChecked(true);
                else
                    storyBtn.setChecked(false);
            } else {
                globalBtn.setChecked(false);
                storyBtn.setChecked(false);
                storyBtn.setEnabled(false);
            }
        }
        else{
            SharedPreferences.Editor editor = AppController.getSharedPreferences().edit();
            FirebaseMessaging.getInstance().subscribeToTopic("snrtnews_story_"+ Utils.getAppCurrentLang());
            storyBtn.setChecked(true);
            globalBtn.setChecked(true);
            editor.putBoolean("notif_enabled", true);
            editor.putBoolean("notif_story", true);
            editor.commit();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                globalBtn.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                        SharedPreferences.Editor editor = AppController.getSharedPreferences().edit();
                        if(isChecked){
                            FirebaseMessaging.getInstance().subscribeToTopic("snrtnews_story_"+ Utils.getAppCurrentLang());
                            if(categories.size()>0){
                                for(int i=0;i<categories.size();i++) {
                                    FirebaseMessaging.getInstance().subscribeToTopic("snrtnews_" + categories.get(i).getId());
                                    editor.putBoolean("notif_"+categories.get(i).getId(), true);
                                }
                            }
                            storyBtn.setEnabled(true);
                            storyBtn.setChecked(true);
                        }
                        else{
                            FirebaseMessaging.getInstance().unsubscribeFromTopic("snrtnews_story_"+ Utils.getAppCurrentLang());
                            if(categories.size()>0){
                                for(int i=0;i<categories.size();i++) {
                                    FirebaseMessaging.getInstance().unsubscribeFromTopic("snrtnews_" + categories.get(i).getId());
                                    editor.putBoolean("notif_"+categories.get(i).getId(), false);
                                }
                            }
                            storyBtn.setChecked(isChecked);
                            storyBtn.setEnabled(isChecked);
                        }
                        editor.putBoolean("notif_enabled", isChecked);
                        editor.putBoolean("notif_story", isChecked);
                        editor.commit();
                        AlertesAdapter adapter = new AlertesAdapter(AlertesActivity.this, categories);
                        recyclerView.setAdapter(adapter);
                    }
                });

                storyBtn.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                        if(isChecked)
                            FirebaseMessaging.getInstance().subscribeToTopic("snrtnews_story_"+ Utils.getAppCurrentLang());
                        else
                            FirebaseMessaging.getInstance().unsubscribeFromTopic("snrtnews_story_"+ Utils.getAppCurrentLang());
                        SharedPreferences.Editor editor = AppController.getSharedPreferences().edit();
                        editor.putBoolean("notif_story", isChecked);
                        editor.commit();
                    }
                });

            }
        }, 1000);
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.back_btn:
                finish();
                break;
            case R.id.select_all_btn:
                if(!globalBtn.isChecked())
                    globalBtn.setChecked(true);
                SharedPreferences.Editor editor = AppController.getSharedPreferences().edit();
                FirebaseMessaging.getInstance().subscribeToTopic("snrtnews_story_"+ Utils.getAppCurrentLang());
                if(categories.size()>0){
                    for(int i=0;i<categories.size();i++) {
                        FirebaseMessaging.getInstance().subscribeToTopic("snrtnews_" + categories.get(i).getId());
                        editor.putBoolean("notif_"+categories.get(i).getId(), true);
                    }
                    AlertesAdapter adapter = new AlertesAdapter(AlertesActivity.this, categories);
                    recyclerView.setAdapter(adapter);
                }
                editor.putBoolean("notif_story", true);
                editor.commit();
                storyBtn.setEnabled(true);
                storyBtn.setChecked(true);
                break;
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(MyContextWrapper.wrap(newBase, Utils.getAppCurrentLang()));
    }
}
