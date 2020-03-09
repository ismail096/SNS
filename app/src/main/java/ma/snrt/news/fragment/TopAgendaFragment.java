package ma.snrt.news.fragment;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import ma.snrt.news.R;
import ma.snrt.news.adapter.AllAgendaAdapter;
import ma.snrt.news.adapter.TopAgendaAdapter;
import ma.snrt.news.model.CategoryAgenda;
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
public class TopAgendaFragment extends Fragment {

    TextViewRegular emptyTextView;
    RecyclerView recyclerView, topRecyclerview;
    ProgressBar progressBar;
    SwipeRefreshLayout swipeRefreshLayout;
    Context mContext;
    ArrayList<CategoryAgenda> categories;
    AllAgendaAdapter adapter;
    TopAgendaAdapter topAdapter;
    ArrayList<Post> tops;

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

        final LinearLayoutManager llm = new LinearLayoutManager(mContext);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(llm);
        recyclerView.setPadding(0, 0, 0, 0);

        final LinearLayoutManager llm2 = new LinearLayoutManager(mContext);
        llm2.setOrientation(LinearLayoutManager.HORIZONTAL);
        topRecyclerview.setHasFixedSize(false);
        topRecyclerview.setLayoutManager(llm2);

        recyclerView.setLayoutManager(llm);

        categories = new ArrayList<>();
        tops = new ArrayList<>();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                categories.clear();
                tops.clear();
                getCategories();
                getTopAgendas();
            }
        });

        getCategories();

        return rootView;
    }

    private void getCategories(){
        progressBar.setVisibility(View.VISIBLE);
        ApiCall.getCategoriesAgenda(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if(progressBar!=null)
                    progressBar.setVisibility(View.GONE);
                if(swipeRefreshLayout!=null)
                    swipeRefreshLayout.setRefreshing(false);
                if(response.body()!=null && response.isSuccessful()){
                    categories.addAll(GsonHelper.getGson().fromJson(response.body(), new TypeToken<List<CategoryAgenda>>(){}.getType()));
                }
                else {
                    String resultFromCache = (String) Cache.getPermanentObject("categories_ag_"+ Utils.getAppCurrentLang());
                    if(resultFromCache!=null)
                        categories.addAll(GsonHelper.getGson().fromJson(resultFromCache, new TypeToken<List<Post>>(){}.getType()));
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
                Toast.makeText(mContext, mContext.getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
                String resultFromCache = (String) Cache.getPermanentObject("categories_ag_"+ Utils.getAppCurrentLang());
                if(resultFromCache!=null)
                    categories.addAll(GsonHelper.getGson().fromJson(resultFromCache, new TypeToken<List<Post>>(){}.getType()));
                setListAdapter();
            }
        });
    }

    private void setListAdapter(){
        if(categories.size()>0){
            adapter = new AllAgendaAdapter(mContext, categories);
            recyclerView.setAdapter(adapter);
            getTopAgendas();
        }
        else{
            recyclerView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.VISIBLE);
        }
    }

    private void getTopAgendas(){
        String cacheTag = "top_agenda_"+ Utils.getAppCurrentLang();
        ApiCall.getLatestAgenda( new Callback<JsonArray>() {
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
            topAdapter = new TopAgendaAdapter(mContext, tops);
            topRecyclerview.setAdapter(topAdapter);
        }
        else{
            topRecyclerview.setVisibility(View.GONE);
        }
    }
}
