package ma.snrt.news.fragment;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import ma.snrt.news.adapter.FlashAdapter;
import ma.snrt.news.model.Post;
import ma.snrt.news.network.ApiCall;
import ma.snrt.news.network.GsonHelper;
import ma.snrt.news.ui.TextViewRegular;
import ma.snrt.news.ui.TextViewExtraBold;
import ma.snrt.news.util.Cache;
import ma.snrt.news.util.DateTimeUtils;
import ma.snrt.news.util.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class FlashFragment extends Fragment {

    TextViewRegular emptyTextView;
    TextViewExtraBold dayTextView;
    RecyclerView recyclerView;
    ProgressBar progressBar;
    SwipeRefreshLayout swipeRefreshLayout;
    Context mContext;
    ArrayList<Post> posts;
    int newsPage = 0;
    boolean isNewsListLoaded;
    FlashAdapter newsAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_flash, container, false);
        recyclerView = rootView.findViewById(R.id.flash_recyclerview);
        emptyTextView = rootView.findViewById(R.id.empty_textview);
        progressBar = rootView.findViewById(R.id.progress_bar);
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        dayTextView = rootView.findViewById(R.id.flash_day_text);

        mContext = getActivity();

        final LinearLayoutManager llm = new LinearLayoutManager(mContext);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(llm);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int i = llm.findFirstCompletelyVisibleItemPosition();
                if(i > -1) {
                    if(posts.size()>0 && posts.get(i)!=null) {
                        if(Utils.getAppCurrentLang().equals("ar")) {
                            if(!DateTimeUtils.getRelativeDayAr(posts.get(i).getDatePublication()).equals(""))
                                dayTextView.setText(DateTimeUtils.getRelativeDayAr(posts.get(i).getDatePublication()));
                            else
                                dayTextView.setText(Utils.getPostRelativeDate(mContext, posts.get(i).getDatePublication()));
                        }
                        else{
                            if(!DateTimeUtils.getRelativeDayFr(posts.get(i).getDatePublication()).equals(""))
                                dayTextView.setText(DateTimeUtils.getRelativeDayFr(posts.get(i).getDatePublication()));
                            else
                                dayTextView.setText(Utils.getPostRelativeDate(mContext, posts.get(i).getDatePublication()));
                        }
                    }
                }
            }
        });

        /*if(AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
            Glide.with(mContext).load(R.raw.loader_dark).into(progressBar);
        else
            Glide.with(mContext).load(R.raw.loader).into(progressBar);*/

        posts = new ArrayList<>();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                newsPage = 0;
                posts.clear();
                recyclerView.setVisibility(View.GONE);
                emptyTextView.setVisibility(View.GONE);
                getFlashNews();
            }
        });

        getFlashNews();

        return rootView;
    }

    private void getFlashNews(){
        if(newsPage ==0)
            progressBar.setVisibility(View.VISIBLE);
        ApiCall.getLatestNews(newsPage, new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if(progressBar!=null)
                    progressBar.setVisibility(View.GONE);
                if(swipeRefreshLayout!=null)
                    swipeRefreshLayout.setRefreshing(false);
                if(response.body()!=null && response.isSuccessful()){
                    ArrayList<Post> result = GsonHelper.getGson().fromJson(response.body(), new TypeToken<List<Post>>(){}.getType());
                    if(newsPage == 0)
                        Cache.putPermanentObject(response.body().toString(), "latest_news_"+ Utils.getAppCurrentLang());
                    if(result.size()>0)
                        posts.addAll(result);
                    else
                        isNewsListLoaded = true;
                }
                else {
                    if(newsPage == 0) {
                        String resultFromCache = (String) Cache.getPermanentObject("latest_news_"+ Utils.getAppCurrentLang());
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
                    String resultFromCache = (String) Cache.getPermanentObject("latest_news_"+ Utils.getAppCurrentLang());
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
                newsAdapter = new FlashAdapter(mContext, posts, recyclerView);
                recyclerView.setAdapter(newsAdapter);
                newsAdapter.setOnLoadMoreListener(new FlashAdapter.OnLoadMoreListener() {
                    @Override
                    public void onLoadMore() {
                        if(!isNewsListLoaded){
                            newsPage++;
                            getFlashNews();
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
}
