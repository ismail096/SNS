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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import ma.snrt.news.AgendaActivity;
import ma.snrt.news.AppController;
import ma.snrt.news.R;
import ma.snrt.news.adapter.AgendaAdapter2;
import ma.snrt.news.adapter.TopAgendaAdapter;
import ma.snrt.news.model.CategoryAgenda;
import ma.snrt.news.model.Post;
import ma.snrt.news.network.ApiCall;
import ma.snrt.news.network.GsonHelper;
import ma.snrt.news.ui.CenterZoomLayoutManager;
import ma.snrt.news.ui.TextViewRegular;
import ma.snrt.news.util.Cache;
import ma.snrt.news.util.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

interface Updateable {
    public void update();
}

/**
 * A simple {@link Fragment} subclass.
 */
public class CategoryAgendaFragment extends Fragment implements  Updateable{

    TextViewRegular emptyTextView;
    RecyclerView recyclerView, topRecyclerview;
    ImageView progressBar;
    SwipeRefreshLayout swipeRefreshLayout;
    Context mContext;
    ArrayList<Post> posts, tops;
    int page = 0;
    boolean isListLoaded;
    AgendaAdapter2 agendaAdapter;
    TopAgendaAdapter topAdapter;
    CategoryAgenda category;
    String startDate = "", endDate = "", position ="";
    LinearSnapHelper snapHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_category_agenda, container, false);
        recyclerView = rootView.findViewById(R.id.agenda_recyclerview);
        topRecyclerview = rootView.findViewById(R.id.agenda_top_recyclerview);
        emptyTextView = rootView.findViewById(R.id.empty_textview);
        progressBar = rootView.findViewById(R.id.progress_bar);
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);

        mContext = getActivity();
        category = (CategoryAgenda) getArguments().getSerializable("category");

        GridLayoutManager llm = new GridLayoutManager(mContext, 2);
        recyclerView.setLayoutManager(llm);

        posts = new ArrayList<>();
        tops = new ArrayList<>();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page = 0;
                posts.clear();
                tops.clear();
                recyclerView.setVisibility(View.GONE);
                emptyTextView.setVisibility(View.GONE);
                getAgendas();
            }
        });

        if(AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
            Glide.with(mContext).load(R.raw.loader_dark).into(progressBar);
        else
            Glide.with(mContext).load(R.raw.loader).into(progressBar);

        getAgendas();

        return rootView;
    }

    private void getAgendas(){
        String cacheTag = "agenda_"+category.getId()+"_"+ Utils.getAppCurrentLang();
        if(page == 0)
            progressBar.setVisibility(View.VISIBLE);
        ApiCall.getAgendaByCat(category.getId(), position, startDate, endDate, page, new Callback<JsonArray>() {
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
                getTopAgendas();
                agendaAdapter = new AgendaAdapter2(mContext, posts, recyclerView);
                recyclerView.setAdapter(agendaAdapter);
                agendaAdapter.setOnLoadMoreListener(new AgendaAdapter2.OnLoadMoreListener() {
                    @Override
                    public void onLoadMore() {
                        if(!isListLoaded){
                            page++;
                            getAgendas();
                        }
                    }
                });
            }
            else
                agendaAdapter.setLoaded();
        }
        else{
            recyclerView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.VISIBLE);
        }
    }

    private void getTopAgendas(){
        String cacheTag = "top_agenda_"+category.getId()+"_"+ Utils.getAppCurrentLang();
        ApiCall.getAgendaByCat(category.getId(), "", "", "", 0, new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if(swipeRefreshLayout!=null)
                    swipeRefreshLayout.setRefreshing(false);
                if(response.body()!=null && response.isSuccessful()){
                    Cache.putPermanentObject(response.body().toString(), cacheTag);
                    tops = GsonHelper.getGson().fromJson(response.body(), new TypeToken<List<Post>>(){}.getType());
                }
                else {
                    String resultFromCache = (String) Cache.getPermanentObject(cacheTag);
                    if(resultFromCache!=null)
                        tops = GsonHelper.getGson().fromJson(resultFromCache, new TypeToken<List<Post>>(){}.getType());
                }
                setTopListAdapter();
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                if(swipeRefreshLayout!=null)
                    swipeRefreshLayout.setRefreshing(false);
                    String resultFromCache = (String) Cache.getPermanentObject(cacheTag);
                    if(resultFromCache!=null)
                        tops = GsonHelper.getGson().fromJson(resultFromCache, new TypeToken<List<Post>>(){}.getType());
                setTopListAdapter();
            }
        });
    }

    private void setTopListAdapter(){
        if(tops.size()>0){
            topRecyclerview.setVisibility(View.VISIBLE);
            CenterZoomLayoutManager layoutManager =
                    new CenterZoomLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
            topRecyclerview.setLayoutManager(layoutManager);
            topAdapter = new TopAgendaAdapter(mContext, tops);
            topRecyclerview.setAdapter(topAdapter);

            // Scroll to the position we want to snap to
            layoutManager.scrollToPosition(tops.size() / 2);
            // Wait until the RecyclerView is laid out.
            topRecyclerview.post(new Runnable() {
                @Override
                public void run() {
                    // Shift the view to snap  near the center of the screen.
                    // This does not have to be precise.
                    int dx = (topRecyclerview.getWidth() - topRecyclerview.getChildAt(0).getWidth()) / 2;
                    topRecyclerview.scrollBy(-dx, 0);
                    // Assign the LinearSnapHelper that will initially snap the near-center view.
                    if(snapHelper==null) {
                        snapHelper = new LinearSnapHelper();
                        snapHelper.attachToRecyclerView(topRecyclerview);
                    }
                }
            });
        }
        else{
            topRecyclerview.setVisibility(View.GONE);
        }
    }

    @Override
    public void update() {
        page = 0;
        posts.clear();
        tops.clear();
        position = ((AgendaActivity) mContext).position;
        startDate = ((AgendaActivity) mContext).dateDebut;
        endDate = ((AgendaActivity) mContext).dateFin;
        topRecyclerview.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        emptyTextView.setVisibility(View.GONE);
        getAgendas();
    }
}
