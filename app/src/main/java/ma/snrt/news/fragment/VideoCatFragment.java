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
import ma.snrt.news.adapter.Videos2Adapter;
import ma.snrt.news.model.Category;
import ma.snrt.news.model.Post;
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
public class VideoCatFragment extends Fragment {

    TextViewRegular emptyTextView;
    RecyclerView recyclerView;
    ImageView progressBar;
    SwipeRefreshLayout swipeRefreshLayout;
    Context mContext;
    ArrayList<Post> posts;
    int page = 0;
    boolean isListLoaded;
    Videos2Adapter adapter;
    Category category;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_category, container, false);
        recyclerView = rootView.findViewById(R.id.news_recyclerview);
        emptyTextView = rootView.findViewById(R.id.empty_textview);
        progressBar = rootView.findViewById(R.id.progress_bar);
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);

        mContext = getActivity();
        category = (Category) getArguments().getSerializable("category");

        final LinearLayoutManager llm = new LinearLayoutManager(mContext);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(llm);

        posts = new ArrayList<>();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page = 0;
                posts.clear();
                emptyTextView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                getVideos();
            }
        });
        if(AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
            Glide.with(mContext).load(R.raw.loader_dark).into(progressBar);
        else
            Glide.with(mContext).load(R.raw.loader).into(progressBar);

        getVideos();

        return rootView;
    }

    private void getVideos(){
        if(page ==0)
            progressBar.setVisibility(View.VISIBLE);
        String cacheTag = "videos_"+category+"_"+Utils.getAppCurrentLang();
        ApiCall.getVideosByCat( category.getId(), true, page, new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if(progressBar!=null)
                    progressBar.setVisibility(View.GONE);
                if(swipeRefreshLayout!=null)
                    swipeRefreshLayout.setRefreshing(false);
                if(response.body()!=null && response.isSuccessful()){
                    ArrayList<Post> result = GsonHelper.getGson().fromJson(response.body(), new TypeToken<List<Post>>(){}.getType());
                    if(page == 0)
                        Cache.putPermanentObject(response.body().toString(), cacheTag);
                    if(result.size()>0)
                        posts.addAll(result);
                    else
                        isListLoaded = true;
                }
                else {
                    if(page == 0) {
                        String resultFromCache = (String) Cache.getPermanentObject(cacheTag);
                        if(resultFromCache!=null)
                            posts = GsonHelper.getGson().fromJson(resultFromCache, new TypeToken<List<Post>>(){}.getType());
                        Toast.makeText(mContext, mContext.getString(R.string.error_load_data), Toast.LENGTH_SHORT).show();
                        isListLoaded = true;
                    }
                }
                setListAdapter();
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                if(progressBar!=null)
                    progressBar.setVisibility(View.GONE);
                if(swipeRefreshLayout!=null)
                    swipeRefreshLayout.setRefreshing(false);
                if(page ==0) {
                    String resultFromCache = (String) Cache.getPermanentObject(cacheTag);
                    if(resultFromCache!=null)
                        posts = GsonHelper.getGson().fromJson(resultFromCache, new TypeToken<List<Post>>(){}.getType());
                    Toast.makeText(mContext, mContext.getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
                    isListLoaded = true;
                }
                setListAdapter();
            }
        });
    }

    private void setListAdapter(){
        if(posts.size()>0){
            recyclerView.setVisibility(View.VISIBLE);
            if(page ==0){
                adapter = new Videos2Adapter(mContext, posts, recyclerView);
                recyclerView.setAdapter(adapter);
                adapter.setOnLoadMoreListener(new Videos2Adapter.OnLoadMoreListener() {
                    @Override
                    public void onLoadMore() {
                        if(!isListLoaded){
                            page++;
                            getVideos();
                        }
                    }
                });
            }
            else
                adapter.setLoaded();
        }
        else{
            recyclerView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.VISIBLE);
        }
    }
}
