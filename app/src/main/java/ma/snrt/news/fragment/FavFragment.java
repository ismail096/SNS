package ma.snrt.news.fragment;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;

import ma.snrt.news.AppController;
import ma.snrt.news.R;
import ma.snrt.news.adapter.NewsFavAdapter;
import ma.snrt.news.adapter.Videos2Adapter;
import ma.snrt.news.model.Post;
import ma.snrt.news.ui.TextViewExtraBold;
import ma.snrt.news.util.Cache;
import ma.snrt.news.util.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class FavFragment extends Fragment {

    RecyclerView recyclerView;
    LinearLayout emptyLayout;
    TextViewExtraBold newsBtn, videosBtn;
    ArrayList<Post> posts;
    int selectedMode = 0;
    Context mContext;
    SwipeRefreshLayout swipeRefreshLayout;
    ImageView favEmptyImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for mContext fragment
        View rootView = inflater.inflate(R.layout.fav_fragment, container, false);
        Cache.initFavoris();
        Cache.initFavorisVideos();
        Cache.initLikedPosts();
        recyclerView = rootView.findViewById(R.id.recyclerview);
        emptyLayout = rootView.findViewById(R.id.empty_layout);
        newsBtn = rootView.findViewById(R.id.news_btn);
        videosBtn = rootView.findViewById(R.id.video_btn);
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        favEmptyImage = rootView.findViewById(R.id.fav_empty_image);
        
        mContext = getActivity();

        if(!mContext.getResources().getBoolean(R.bool.is_tablet)) {
            final LinearLayoutManager llm = new LinearLayoutManager(mContext);
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(llm);
        }
        else{
            final GridLayoutManager lm = new GridLayoutManager(mContext, 2);
            lm.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(lm);
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) recyclerView.getLayoutParams();
            int margin = Utils.dpToPx(getResources(), 5);
            lp.setMargins(margin, margin *2 , margin, margin * 2);
            recyclerView.setLayoutParams(lp);
        }
        recyclerView.setHasFixedSize(true);

        videosBtn.setOnClickListener(v->{
            getVideos();
        });

        newsBtn.setOnClickListener(v->{
            getNews();
        });

        posts = new ArrayList<>();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(selectedMode == 0)
                    getNews();
                else if(selectedMode ==1)
                    getVideos();
            }
        });

        if(AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false)){
            rootView.findViewById(R.id.fav_menu).setBackgroundColor(ContextCompat.getColor(mContext, R.color.bgGrey2Dark));
            favEmptyImage.setImageResource(R.drawable.fav_empty_dark);
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        update();
    }

    private void getNews(){
        if(swipeRefreshLayout!=null)
            swipeRefreshLayout.setRefreshing(false);
        selectedMode = 0;
        newsBtn.setTextColor(ContextCompat.getColor(mContext, R.color.app_white));
        newsBtn.setBackgroundResource(R.drawable.btn_red_left);
        videosBtn.setTextColor(ContextCompat.getColor(mContext, R.color.app_black));
        videosBtn.setBackgroundResource(R.drawable.btn_grey_right);
        posts = new ArrayList<>(Cache.getFavoris().values());
        if(posts.size()>0){
            NewsFavAdapter adapter = new NewsFavAdapter(mContext, posts, recyclerView);
            recyclerView.setAdapter(adapter);
            recyclerView.setVisibility(View.VISIBLE);
            emptyLayout.setVisibility(View.GONE);
        }
        else{
            recyclerView.setVisibility(View.GONE);
            emptyLayout.setVisibility(View.VISIBLE);
        }
    }

    private void getVideos(){
        if(swipeRefreshLayout!=null)
            swipeRefreshLayout.setRefreshing(false);
        selectedMode = 1;
        newsBtn.setTextColor(ContextCompat.getColor(mContext, R.color.app_black));
        newsBtn.setBackgroundResource(R.drawable.btn_grey_left);
        videosBtn.setTextColor(ContextCompat.getColor(mContext, R.color.app_white));
        videosBtn.setBackgroundResource(R.drawable.btn_red_right);
        posts = new ArrayList<>(Cache.getFavVideos().values());
        if(posts.size()>0){
            Videos2Adapter adapter = new Videos2Adapter(mContext, posts, recyclerView);
            recyclerView.setAdapter(adapter);
            recyclerView.setVisibility(View.VISIBLE);
            emptyLayout.setVisibility(View.GONE);
        }
        else{
            recyclerView.setVisibility(View.GONE);
            emptyLayout.setVisibility(View.VISIBLE);
        }
    }

    public void update(){
        if(selectedMode == 0)
            getNews();
        else if(selectedMode ==1)
            getVideos();
    }
}
