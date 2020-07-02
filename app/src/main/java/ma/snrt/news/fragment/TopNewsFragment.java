package ma.snrt.news.fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import ma.snrt.news.AppController;
import ma.snrt.news.MainActivity;
import ma.snrt.news.PostDetailActivity;
import ma.snrt.news.R;
import ma.snrt.news.adapter.TopNewsAdapter;
import ma.snrt.news.model.Post;
import ma.snrt.news.model.User;
import ma.snrt.news.network.ApiCall;
import ma.snrt.news.network.GsonHelper;
import ma.snrt.news.ui.TextViewBold;
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
    ProgressBar progressBar;
    SwipeRefreshLayout swipeRefreshLayout;
    LinearLayout liveLayout, watchBtn;
    ImageView liveImageView;
    TextViewBold liveTitle;
    Context mContext;
    ArrayList<Post> posts, videos, agendas;
    ArrayList<User> users;
    TopNewsAdapter newsAdapter;
    Post livePost;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_top_news, container, false);
        recyclerView = rootView.findViewById(R.id.news_recyclerview);
        emptyTextView = rootView.findViewById(R.id.empty_textview);
        progressBar = rootView.findViewById(R.id.progress_bar);
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        liveLayout = rootView.findViewById(R.id.live_layout);
        watchBtn = rootView.findViewById(R.id.watch_btn);
        liveImageView = rootView.findViewById(R.id.live_image);
        liveTitle = rootView.findViewById(R.id.live_title);

        mContext = getActivity();
        if(!getResources().getBoolean(R.bool.is_tablet)) {
            final LinearLayoutManager llm = new LinearLayoutManager(mContext);
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(llm);
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    //recyclerView.setEnabled(llm.findFirstCompletelyVisibleItemPosition() == 0); // 0 is for first item position
                    try {
                        int firstPos = llm.findFirstCompletelyVisibleItemPosition();
                        if (firstPos > 0) {
                            swipeRefreshLayout.setEnabled(false);
                        } else {
                            swipeRefreshLayout.setEnabled(true);
                            if(recyclerView.getScrollState() == 1)
                                if(swipeRefreshLayout.isRefreshing())
                                    recyclerView.stopScroll();
                        }

                    }catch(Exception e) {
                    }
                }
            });
       }
        else{
            final GridLayoutManager lm = new GridLayoutManager(mContext, 3);
            lm.setOrientation(LinearLayoutManager.VERTICAL);
            lm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                     if(position==0 || position==4 || position==5
                             || position==6 || position==10 || position == 11
                             || (position>11 && (position - 11) % 4 == 0))
                        return 3;
                     return 1;
                }
            });
            recyclerView.setLayoutManager(lm);
        }
        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(true);

        posts = new ArrayList<>();
        videos = new ArrayList<>();
        agendas = new ArrayList<>();
        users = new ArrayList<>();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                recyclerView.setVisibility(View.GONE);
                emptyTextView.setVisibility(View.GONE);
                liveLayout.setVisibility(View.GONE);
                posts.clear();
                agendas.clear();
                videos.clear();
                users.clear();
                getStories();
                checkLive();
            }
        });

        /*if(AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
            Glide.with(mContext).load(R.raw.loader_dark).into(progressBar);
        else
            Glide.with(mContext).load(R.raw.loader).into(progressBar);*/

        getStories();
        checkLive();

        return rootView;
    }

    private void checkLive() {
        ApiCall.getLive(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if(response.body()!=null && response.isSuccessful() && response.body().size()>0){
                    livePost = GsonHelper.getGson().fromJson(response.body().get(0), Post.class);
                    if(livePost!=null && mContext!=null) {
                        liveLayout.setVisibility(View.VISIBLE);
                        liveTitle.setText(Html.fromHtml(livePost.getTitle()));
                        liveLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(mContext, PostDetailActivity.class);
                                intent.putExtra("post", livePost);
                                startActivity(intent);
                            }
                        });
                        RequestOptions requestOptions = new RequestOptions();
                        requestOptions.placeholder(R.drawable.placeholder);
                        requestOptions.error(R.drawable.placeholder);
                        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL);
                        Glide.with(mContext).load(livePost.getImage()).apply(requestOptions).into(liveImageView);
                    }
                }
                else
                    liveLayout.setVisibility(View.GONE);

            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                liveLayout.setVisibility(View.GONE);
            }
        });
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
        ApiCall.getLatestAgenda(new Callback<JsonArray>() {
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
            /*if(!getResources().getBoolean(R.bool.is_tablet)) {
                if (posts.size() > 4 && users.size() > 0)
                    posts.add(4, new Post(1));
                if (posts.size() > 5 && videos.size() > 0)
                    posts.add(5, new Post(3));
                if (posts.size() > 10 && agendas.size() > 0)
                    posts.add(10, new Post(2));
            }
            else{*/
                if (posts.size() > 4 && users.size() > 0)
                    posts.add(4, new Post(1));
                if (posts.size() > 5 && videos.size() > 0)
                    posts.add(5, new Post(3));
                if (posts.size() > 10 && agendas.size() > 0)
                    posts.add(10, new Post(2));
            //}
            newsAdapter = new TopNewsAdapter(mContext, posts, users, videos, agendas);
            recyclerView.setAdapter(newsAdapter);
        }
        else{
            recyclerView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.VISIBLE);
        }
    }
}
