package ma.snrt.news.fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import ma.snrt.news.AppController;
import ma.snrt.news.R;
import ma.snrt.news.WebViewActivity;
import ma.snrt.news.adapter.MatchAdapter;
import ma.snrt.news.model.Match;
import ma.snrt.news.adapter.SportAdapter;
import ma.snrt.news.adapter.TagAdapter;
import ma.snrt.news.model.Category;
import ma.snrt.news.model.Post;
import ma.snrt.news.model.Tag;
import ma.snrt.news.network.ApiCall;
import ma.snrt.news.network.GsonHelper;
import ma.snrt.news.ui.CirclePagerIndicatorDecoration;
import ma.snrt.news.ui.TextViewRegular;
import ma.snrt.news.util.Cache;
import ma.snrt.news.util.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class SportFragment extends Fragment {

    TextViewRegular emptyTextView;
    RecyclerView recyclerView, matchesRecyclerView, tagsRecyclerView;
    RelativeLayout matchesLayout;
    LinearLayout botolaLink;
    ImageView progressBar;
    SwipeRefreshLayout swipeRefreshLayout;
    Context mContext;
    ArrayList<Post> posts;
    ArrayList<Match> matches;
    ArrayList<Tag> tags;
    int newsPage = 0;
    boolean isNewsListLoaded;
    SportAdapter newsAdapter;
    TagAdapter tagAdapter;
    Category category;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_sport, container, false);
        recyclerView = rootView.findViewById(R.id.news_recyclerview);
        tagsRecyclerView = rootView.findViewById(R.id.tags_recyclerview);
        matchesRecyclerView = rootView.findViewById(R.id.top_matches_recyclerview);
        matchesLayout = rootView.findViewById(R.id.top_matches_layout);
        botolaLink = rootView.findViewById(R.id.botola_link);
        emptyTextView = rootView.findViewById(R.id.empty_textview);
        progressBar = rootView.findViewById(R.id.progress_bar);
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);

        mContext = getActivity();
        category = (Category) getArguments().getSerializable("category");

        //if(!mContext.getResources().getBoolean(R.bool.is_tablet)) {
            final LinearLayoutManager llm = new LinearLayoutManager(mContext);
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(llm);
        /*}
        else{
            final GridLayoutManager lm = new GridLayoutManager(mContext, 2);
            lm.setOrientation(LinearLayoutManager.VERTICAL);
            lm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if(position==0 || position%5==0)
                        return 2;
                    return 1;
                }
            });
            recyclerView.setLayoutManager(lm);
        }*/
        recyclerView.setHasFixedSize(false);

        LinearLayoutManager llm2 = new LinearLayoutManager(mContext);
        llm2.setOrientation(LinearLayoutManager.HORIZONTAL);
        matchesRecyclerView.setLayoutManager(llm2);
        matchesRecyclerView.setHasFixedSize(true);


        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(mContext);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.FLEX_END);
        tagsRecyclerView.setLayoutManager(layoutManager);

        posts = new ArrayList<>();
        matches = new ArrayList<>();
        tags = new ArrayList<>();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                newsPage = 0;
                posts.clear();
                tags.clear();
                emptyTextView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                matchesLayout.setVisibility(View.GONE);
                getNews();
                getTags();
                getMatches();
            }
        });

        if(AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
            Glide.with(mContext).load(R.raw.loader_dark).into(progressBar);
        else
            Glide.with(mContext).load(R.raw.loader).into(progressBar);

        botolaLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, WebViewActivity.class);
                intent.putExtra("pageId", 0);
                startActivity(intent);
            }
        });

        getNews();
        getMatches();
        getTags();

        return rootView;
    }

    private void getNews(){
        String cacheTag = "news_"+category.getId()+"_"+ Utils.getAppCurrentLang();
        if(newsPage ==0)
            progressBar.setVisibility(View.VISIBLE);
        ApiCall.getNewsByCatOrTag(true, category.getId(), "", newsPage, new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if(progressBar!=null)
                    progressBar.setVisibility(View.GONE);
                if(swipeRefreshLayout!=null)
                    swipeRefreshLayout.setRefreshing(false);
                if(response.body()!=null && response.isSuccessful()){
                    ArrayList<Post> result = GsonHelper.getGson().fromJson(response.body(), new TypeToken<List<Post>>(){}.getType());
                    if(newsPage == 0)
                        Cache.putPermanentObject(response.body().toString(), cacheTag);
                    if(result.size()>0)
                        posts.addAll(result);
                    else
                        isNewsListLoaded = true;
                }
                else {
                    if(newsPage == 0) {
                        String resultFromCache = (String) Cache.getPermanentObject(cacheTag);
                        if(resultFromCache!=null)
                            posts = GsonHelper.getGson().fromJson(resultFromCache, new TypeToken<List<Post>>(){}.getType());
                        Toast.makeText(mContext, mContext.getString(R.string.error_load_data), Toast.LENGTH_SHORT).show();
                        isNewsListLoaded = true;
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
                if(newsPage ==0) {
                    String resultFromCache = (String) Cache.getPermanentObject(cacheTag);
                    if(resultFromCache!=null)
                        posts = GsonHelper.getGson().fromJson(resultFromCache, new TypeToken<List<Post>>(){}.getType());
                    Toast.makeText(mContext, mContext.getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
                    isNewsListLoaded = true;
                }
                setListAdapter();
            }
        });
    }

    private void setListAdapter(){
        if(posts.size()>0){
            recyclerView.setVisibility(View.VISIBLE);
            if(newsPage ==0){
                newsAdapter = new SportAdapter(mContext, posts, recyclerView);
                recyclerView.setAdapter(newsAdapter);
                newsAdapter.setOnLoadMoreListener(new SportAdapter.OnLoadMoreListener() {
                    @Override
                    public void onLoadMore() {
                        if(!isNewsListLoaded){
                            newsPage++;
                            getNews();
                        }
                    }
                });
            }
            else
                newsAdapter.setLoaded();
        }
        else{
            recyclerView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.VISIBLE);
        }
    }

    private void getMatches(){
        String cacheTag = "matches_"+ Utils.getAppCurrentLang();
        progressBar.setVisibility(View.VISIBLE);
        ApiCall.getLiveMatches( new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if(progressBar!=null)
                    progressBar.setVisibility(View.GONE);
                if(swipeRefreshLayout!=null)
                    swipeRefreshLayout.setRefreshing(false);
                if(response.body()!=null && response.isSuccessful()){
                    JsonArray jsonArray = response.body().getAsJsonArray("FinishedGames");
                    matches = GsonHelper.getGson().fromJson(jsonArray, new TypeToken<List<Match>>(){}.getType());
                    Cache.putPermanentObject(jsonArray.toString(), cacheTag);
                }
                else {
                    String resultFromCache = (String) Cache.getPermanentObject(cacheTag);
                    if(resultFromCache!=null)
                        matches = GsonHelper.getGson().fromJson(resultFromCache, new TypeToken<List<Match>>(){}.getType());
                }
                setMatchesAdapter();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if(progressBar!=null)
                    progressBar.setVisibility(View.GONE);
                if(swipeRefreshLayout!=null)
                    swipeRefreshLayout.setRefreshing(false);
                    String resultFromCache = (String) Cache.getPermanentObject(cacheTag);
                    if(resultFromCache!=null)
                        matches = GsonHelper.getGson().fromJson(resultFromCache, new TypeToken<List<Match>>(){}.getType());
                setMatchesAdapter();
            }
        });
    }

    private void setMatchesAdapter(){
        if(matches.size()>0){
            matchesLayout.setVisibility(View.VISIBLE);
            MatchAdapter adapter = new MatchAdapter(mContext, matches);
            matchesRecyclerView.setAdapter(adapter);
            matchesRecyclerView.addItemDecoration(new CirclePagerIndicatorDecoration());
        }
        else{
            matchesLayout.setVisibility(View.GONE);
        }
    }

    private void getTags(){
        String cacheTag = "tags_"+category.getId()+"_"+ Utils.getAppCurrentLang();
        ApiCall.getTags(category.getId(), new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if(response.body()!=null && response.isSuccessful()){
                    tags = GsonHelper.getGson().fromJson(response.body(), new TypeToken<List<Tag>>(){}.getType());
                    Cache.putPermanentObject(response.body().toString(), cacheTag);
                }
                else {
                    String resultFromCache = (String) Cache.getPermanentObject(cacheTag);
                    if(resultFromCache!=null)
                        tags = GsonHelper.getGson().fromJson(resultFromCache, new TypeToken<List<Tag>>(){}.getType());
                }
                setTagsAdapter();
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                String resultFromCache = (String) Cache.getPermanentObject(cacheTag);
                if(resultFromCache!=null)
                    tags = GsonHelper.getGson().fromJson(resultFromCache, new TypeToken<List<Tag>>(){}.getType());
                setTagsAdapter();
            }
        });
    }

    private void setTagsAdapter(){
        if(tags.size()>0){
            tagsRecyclerView.setVisibility(View.VISIBLE);
            tagAdapter = new TagAdapter(mContext, tags);
            tagsRecyclerView.setAdapter(tagAdapter);
        }
        else{
            tagsRecyclerView.setVisibility(View.GONE);
        }
    }
}
