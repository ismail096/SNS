package ma.snrt.news.fragment;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ma.snrt.news.AgendaActivity;
import ma.snrt.news.AppController;
import ma.snrt.news.R;
import ma.snrt.news.adapter.LifeAdapter;
import ma.snrt.news.adapter.LifeTagAdapter;
import ma.snrt.news.adapter.LifeTopAdapter;
import ma.snrt.news.model.Category;
import ma.snrt.news.model.Post;
import ma.snrt.news.model.Tag;
import ma.snrt.news.network.ApiCall;
import ma.snrt.news.network.GsonHelper;
import ma.snrt.news.ui.CenterZoomLayoutManager;
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
public class LifeFragment extends Fragment {

    TextViewRegular emptyTextView;
    TextViewBold recommandedTitle;
    RecyclerView recyclerView, tagsRecyclerView, topRecyclerView;
    ImageView progressBar;
    SwipeRefreshLayout swipeRefreshLayout;
    Context mContext;
    ArrayList<Post> posts, tops;
    ArrayList<Tag> tags;
    int newsPage = 0;
    boolean isNewsListLoaded, isNewsByTag;
    LifeAdapter newsAdapter;
    LifeTagAdapter tagAdapter;
    Category category;
    String selectedTag = "";
    LinearSnapHelper snapHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_life, container, false);
        recyclerView = rootView.findViewById(R.id.news_recyclerview);
        tagsRecyclerView = rootView.findViewById(R.id.tags_recyclerview);
        topRecyclerView = rootView.findViewById(R.id.top_news_recyclerview);
        emptyTextView = rootView.findViewById(R.id.empty_textview);
        progressBar = rootView.findViewById(R.id.progress_bar);
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        recommandedTitle = rootView.findViewById(R.id.recommand_title);

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

        /*FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(mContext);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);
        tagsRecyclerView.setLayoutManager(layoutManager);*/
        tagsRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        //topRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));

        posts = new ArrayList<>();
        tags = new ArrayList<>();
        tops = new ArrayList<>();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                newsPage = 0;
                posts.clear();
                tags.clear();
                selectedTag = "";
                emptyTextView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                getTags();
                getNews();
                getTopNews();
            }
        });

        if(AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
            Glide.with(mContext).load(R.raw.loader_dark).into(progressBar);
        else
            Glide.with(mContext).load(R.raw.loader).into(progressBar);

        getTags();
        getTopNews();
        getNews();

        return rootView;
    }

    private void getNews(){
        isNewsByTag = false;
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

    private void getNewsByTag() {
        isNewsByTag = true;
        String cacheTag = "news_" + selectedTag + "_" + Utils.getAppCurrentLang();
        if (newsPage == 0)
            progressBar.setVisibility(View.VISIBLE);
        ApiCall.getNewsByCatOrTag(false, 0, selectedTag, newsPage, new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if (progressBar != null)
                    progressBar.setVisibility(View.GONE);
                if (swipeRefreshLayout != null)
                    swipeRefreshLayout.setRefreshing(false);
                if (response.body() != null && response.isSuccessful()) {
                    ArrayList<Post> result = GsonHelper.getGson().fromJson(response.body(), new TypeToken<List<Post>>() {
                    }.getType());
                    if (newsPage == 0)
                        Cache.putPermanentObject(response.body().toString(), cacheTag);
                    if (result.size() > 0)
                        posts.addAll(result);
                    else
                        isNewsListLoaded = true;
                } else {
                    if (newsPage == 0) {
                        String resultFromCache = (String) Cache.getPermanentObject(cacheTag);
                        if (resultFromCache != null)
                            posts = GsonHelper.getGson().fromJson(resultFromCache, new TypeToken<List<Post>>() {
                            }.getType());
                        Toast.makeText(mContext, mContext.getString(R.string.error_load_data), Toast.LENGTH_SHORT).show();
                        isNewsListLoaded = true;
                    }
                }
                setListAdapter();
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                if (progressBar != null)
                    progressBar.setVisibility(View.GONE);
                if (swipeRefreshLayout != null)
                    swipeRefreshLayout.setRefreshing(false);
                if (newsPage == 0) {
                    String resultFromCache = (String) Cache.getPermanentObject(cacheTag);
                    if (resultFromCache != null)
                        posts = GsonHelper.getGson().fromJson(resultFromCache, new TypeToken<List<Post>>() {
                        }.getType());
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
                newsAdapter = new LifeAdapter(mContext, posts, recyclerView);
                recyclerView.setAdapter(newsAdapter);
                newsAdapter.setOnLoadMoreListener(new LifeAdapter.OnLoadMoreListener() {
                    @Override
                    public void onLoadMore() {
                        if(!isNewsListLoaded){
                            newsPage++;
                            if(!isNewsByTag)
                                getNews();
                            else
                                getNewsByTag();
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

    private void getTopNews(){
        String cacheTag = "top_"+selectedTag+"_"+ Utils.getAppCurrentLang();
        boolean isCat = false;
        int catId = category.getId();
        if(selectedTag.equals(""))
            isCat = true;
        ApiCall.getNewsByCatOrTag(isCat, catId, selectedTag, 0, new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if(progressBar!=null)
                    progressBar.setVisibility(View.GONE);
                if(swipeRefreshLayout!=null)
                    swipeRefreshLayout.setRefreshing(false);
                if(response.body()!=null && response.isSuccessful()){
                    tops = GsonHelper.getGson().fromJson(response.body(), new TypeToken<List<Post>>(){}.getType());
                        Cache.putPermanentObject(response.body().toString(), cacheTag);
                }
                else {
                    String resultFromCache = (String) Cache.getPermanentObject(cacheTag);
                    if(resultFromCache!=null)
                        tops = GsonHelper.getGson().fromJson(resultFromCache, new TypeToken<List<Post>>(){}.getType());
                    Toast.makeText(mContext, mContext.getString(R.string.error_load_data), Toast.LENGTH_SHORT).show();
                }
                setTopsAdapter();
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                if(progressBar!=null)
                    progressBar.setVisibility(View.GONE);
                if(swipeRefreshLayout!=null)
                    swipeRefreshLayout.setRefreshing(false);
                    String resultFromCache = (String) Cache.getPermanentObject(cacheTag);
                    if(resultFromCache!=null)
                        tops = GsonHelper.getGson().fromJson(resultFromCache, new TypeToken<List<Post>>(){}.getType());
                    Toast.makeText(mContext, mContext.getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
                setTopsAdapter();
            }
        });
    }

    private void setTopsAdapter(){
        if(tops.size()>0){
            topRecyclerView.setVisibility(View.VISIBLE);
            recommandedTitle.setVisibility(View.VISIBLE);
            if(Utils.getAppCurrentLang().equals("ar"))
                Collections.reverse(tops);
            CenterZoomLayoutManager layoutManager =
                    new CenterZoomLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
            topRecyclerView.setLayoutManager(layoutManager);
            topRecyclerView.setAdapter(new LifeTopAdapter(mContext, tops));

            // Scroll to the position we want to snap to
            if(tops.size()>2)
                layoutManager.scrollToPosition(tops.size() / 2);
            // Wait until the RecyclerView is laid out.
            topRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    // Shift the view to snap  near the center of the screen.
                    // This does not have to be precise.
                    try {
                        int dx = (topRecyclerView.getWidth() - topRecyclerView.getChildAt(0).getWidth()) / 2;
                        if(tops.size()>2)
                        topRecyclerView.scrollBy(-dx, 0);
                        // Assign the LinearSnapHelper that will initially snap the near-center view.
                        if (snapHelper == null) {
                            snapHelper = new LinearSnapHelper();
                            snapHelper.attachToRecyclerView(topRecyclerView);
                        }
                    }catch(Exception ex){}
                }
            });
        }
        else{
            topRecyclerView.setVisibility(View.GONE);
            recommandedTitle.setVisibility(View.GONE);
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
            tags.add(0, new Tag(0, getString(R.string.all), true));
            tagsRecyclerView.setVisibility(View.VISIBLE);
            tagAdapter = new LifeTagAdapter(mContext, tags, tag -> {
                for(int i=0;i<tags.size();i++) {
                    if(tag.getId()!=tags.get(i).getId())
                        tags.get(i).setSelected(false);
                    else
                        tags.get(i).setSelected(true);
                }
                tagAdapter.notifyDataSetChanged();
                posts.clear();
                newsPage = 0;
                recyclerView.setVisibility(View.GONE);
                emptyTextView.setVisibility(View.GONE);
                if(tag.getId() == 0) {
                    selectedTag = "";
                    getNews();
                }
                else {
                    selectedTag = tag.getName();
                    getNewsByTag();
                }
                getTopNews();
            });
            tagsRecyclerView.setAdapter(tagAdapter);
        }
        else{
            tagsRecyclerView.setVisibility(View.GONE);
        }
    }
}
