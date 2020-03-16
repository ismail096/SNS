package ma.snrt.news.fragment;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import ma.snrt.news.AppController;
import ma.snrt.news.R;
import ma.snrt.news.adapter.TopNewsAdapter;
import ma.snrt.news.model.Post;
import ma.snrt.news.model.User;
import ma.snrt.news.network.ApiCall;
import ma.snrt.news.network.GsonHelper;
import ma.snrt.news.ui.TextViewRegular;
import ma.snrt.news.util.Cache;
import ma.snrt.news.util.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class TopNewsFragment extends Fragment {

    TextViewRegular emptyTextView;
    RecyclerView recyclerView;
    ImageView progressBar;
    SwipeRefreshLayout swipeRefreshLayout;
    Context mContext;
    ArrayList<Post> posts, videos, agendas;
    ArrayList<User> users;
    TopNewsAdapter newsAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_top_news, container, false);
        recyclerView = rootView.findViewById(R.id.news_recyclerview);
        emptyTextView = rootView.findViewById(R.id.empty_textview);
        progressBar = rootView.findViewById(R.id.progress_bar);
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);

        mContext = getActivity();

        final LinearLayoutManager llm = new LinearLayoutManager(mContext);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(llm);

        recyclerView.setLayoutManager(llm);

        posts = new ArrayList<>();
        videos = new ArrayList<>();
        agendas = new ArrayList<>();
        users = new ArrayList<>();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                recyclerView.setVisibility(View.GONE);
                emptyTextView.setVisibility(View.GONE);
                posts.clear();
                agendas.clear();
                videos.clear();
                users.clear();
                getStories();
            }
        });

        if(AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
            Glide.with(mContext).load(R.raw.loader_dark).into(progressBar);
        else
            Glide.with(mContext).load(R.raw.loader).into(progressBar);

        getStories();

        return rootView;
    }

    private void getStories(){
        progressBar.setVisibility(View.VISIBLE);
        ApiCall.getStories(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if(response.body()!=null && response.isSuccessful()){
                    users = GsonHelper.getGson().fromJson(response.body(), new TypeToken<List<User>>(){}.getType());
                    Cache.putPermanentObject(response.body().toString(), "stories_"+Utils.getAppCurrentLang());
                }
                else {
                    String resultFromCache = (String) Cache.getPermanentObject("stories_"+ Utils.getAppCurrentLang());
                    if(resultFromCache!=null)
                        users = GsonHelper.getGson().fromJson(resultFromCache, new TypeToken<List<User>>(){}.getType());
                }

                getAgenda();
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                String resultFromCache = (String) Cache.getPermanentObject("stories_"+ Utils.getAppCurrentLang());
                if(resultFromCache!=null)
                    users = GsonHelper.getGson().fromJson(resultFromCache, new TypeToken<List<User>>(){}.getType());
                getAgenda();
            }
        });
    }

    private void getAgenda(){
        ApiCall.getLatestAgenda( new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if(response.body()!=null && response.isSuccessful()){
                    agendas = GsonHelper.getGson().fromJson(response.body(), new TypeToken<List<Post>>(){}.getType());
                    Cache.putPermanentObject(response.body().toString(), "agenda_"+Utils.getAppCurrentLang());
                }
                else {
                    String resultFromCache = (String) Cache.getPermanentObject("agenda_"+ Utils.getAppCurrentLang());
                    if(resultFromCache!=null)
                        agendas = GsonHelper.getGson().fromJson(resultFromCache, new TypeToken<List<Post>>(){}.getType());
                }
                getVideos();
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                String resultFromCache = (String) Cache.getPermanentObject("agenda_"+ Utils.getAppCurrentLang());
                if(resultFromCache!=null)
                    agendas = GsonHelper.getGson().fromJson(resultFromCache, new TypeToken<List<Post>>(){}.getType());
                getVideos();
            }
        });
    }

    private void getVideos(){
        ApiCall.getVideos(false,0, 3, new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if(response.body()!=null && response.isSuccessful()){
                    videos = GsonHelper.getGson().fromJson(response.body(), new TypeToken<List<Post>>(){}.getType());
                    Cache.putPermanentObject(response.body().toString(), "videos_"+Utils.getAppCurrentLang());
                }
                else {
                    String resultFromCache = (String) Cache.getPermanentObject("videos_"+ Utils.getAppCurrentLang());
                    if(resultFromCache!=null)
                        videos = GsonHelper.getGson().fromJson(resultFromCache, new TypeToken<List<Post>>(){}.getType());
                }
                getNews();
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                String resultFromCache = (String) Cache.getPermanentObject("videos_"+ Utils.getAppCurrentLang());
                if(resultFromCache!=null)
                    videos = GsonHelper.getGson().fromJson(resultFromCache, new TypeToken<List<Post>>(){}.getType());
                getNews();
            }
        });
    }

    private void getNews(){
        String cacheTag = "top_news_"+ Utils.getAppCurrentLang();
        ApiCall.getFeaturedNews(0, 20, new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if(progressBar!=null)
                    progressBar.setVisibility(View.GONE);
                if(swipeRefreshLayout!=null)
                    swipeRefreshLayout.setRefreshing(false);
                if(response.body()!=null && response.isSuccessful()){
                    posts.addAll(GsonHelper.getGson().fromJson(response.body(), new TypeToken<List<Post>>(){}.getType()));
                    Cache.putPermanentObject(response.body().toString(), cacheTag);
                }
                else {
                    String resultFromCache = (String) Cache.getPermanentObject(cacheTag);
                    if(resultFromCache!=null)
                        posts = GsonHelper.getGson().fromJson(resultFromCache, new TypeToken<List<Post>>(){}.getType());
                    Toast.makeText(mContext, mContext.getString(R.string.error_load_data), Toast.LENGTH_SHORT).show();
                }
                setListAdapter();
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                if(progressBar!=null)
                    progressBar.setVisibility(View.GONE);
                if(swipeRefreshLayout!=null)
                    swipeRefreshLayout.setRefreshing(false);
                String resultFromCache = (String) Cache.getPermanentObject(cacheTag);
                if(resultFromCache!=null)
                    posts = GsonHelper.getGson().fromJson(resultFromCache, new TypeToken<List<Post>>(){}.getType());
                Toast.makeText(mContext, mContext.getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
                setListAdapter();
            }
        });
    }

    private void setListAdapter(){
        if(posts.size()>0){
            recyclerView.setVisibility(View.VISIBLE);
            if(posts.size()>4 && users.size()>0)
                posts.add(4, new Post(1));
            if(posts.size()>5 && agendas.size()>0)
                posts.add(5, new Post(2));
            if(posts.size()>10 && videos.size()>0)
                posts.add(10, new Post(3));
            newsAdapter = new TopNewsAdapter(mContext, new ArrayList<>(posts.subList(0, posts.size())), users, videos, agendas);
            recyclerView.setAdapter(newsAdapter);
        }
        else{
            recyclerView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.VISIBLE);
        }
    }
}
