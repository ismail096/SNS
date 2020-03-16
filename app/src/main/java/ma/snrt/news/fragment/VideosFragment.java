package ma.snrt.news.fragment;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import ma.snrt.news.AppController;
import ma.snrt.news.R;
import ma.snrt.news.adapter.VideosPagerAdapter;
import ma.snrt.news.model.Category;
import ma.snrt.news.network.ApiCall;
import ma.snrt.news.network.GsonHelper;
import ma.snrt.news.ui.TextViewExtraBold;
import ma.snrt.news.ui.TextViewRegular;
import ma.snrt.news.util.Cache;
import ma.snrt.news.util.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class VideosFragment extends Fragment {
    Context mContext;
    List<Category> categories;
    ViewPager viewPager;
    TabLayout tabLayout;
    TextViewRegular emptyTextView;
    ImageView progressBar;
    LinearLayout contentLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_videos, container, false);
        mContext = getActivity();
        viewPager = rootView.findViewById(R.id.videos_viewpager);
        tabLayout = rootView.findViewById(R.id.tablayout);
        contentLayout = rootView.findViewById(R.id.content_layout);
        emptyTextView = rootView.findViewById(R.id.empty_textview);
        progressBar = rootView.findViewById(R.id.progress_bar);

        categories = new ArrayList<>();
        categories.add(new Category(0, getString(R.string.all), "#eeeeee"));

        if(AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
            Glide.with(mContext).load(R.raw.loader_dark).into(progressBar);
        else
            Glide.with(mContext).load(R.raw.loader).into(progressBar);

        getCategories();
        return rootView;
    }

    private void getCategories(){
        progressBar.setVisibility(View.VISIBLE);
        ApiCall.getCategories(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if(progressBar!=null)
                    progressBar.setVisibility(View.GONE);
                if(response.body()!=null && response.isSuccessful()){
                    categories.addAll(GsonHelper.getGson().fromJson(response.body(), new TypeToken<List<Category>>(){}.getType()));
                }
                else {
                    String resultFromCache = (String) Cache.getPermanentObject("categories_"+ Utils.getAppCurrentLang());
                    if(resultFromCache!=null)
                        categories.addAll(GsonHelper.getGson().fromJson(resultFromCache, new TypeToken<List<Category>>(){}.getType()));
                }
                setPagerAdapter();
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                if(progressBar!=null)
                    progressBar.setVisibility(View.GONE);
                String resultFromCache = (String) Cache.getPermanentObject("categories_"+ Utils.getAppCurrentLang());
                if(resultFromCache!=null)
                    categories.addAll(GsonHelper.getGson().fromJson(resultFromCache, new TypeToken<List<Category>>(){}.getType()));
                setPagerAdapter();
            }
        });
    }

    private void setPagerAdapter(){
        if(categories.size()>0){
            viewPager.setOffscreenPageLimit(categories.size());
            contentLayout.setVisibility(View.VISIBLE);
            VideosPagerAdapter adapter = new VideosPagerAdapter(categories, getActivity().getSupportFragmentManager());
            viewPager.setAdapter(adapter);
            tabLayout.setupWithViewPager(viewPager);
            for (int i = 0; i < tabLayout.getTabCount(); i++) {
                View container = LayoutInflater.from(mContext).inflate(R.layout.tab_item_layout,null);
                TextViewExtraBold tv = container.findViewById(R.id.tab_textview);
                tv.setText(Html.fromHtml(categories.get(i).getTitle()));
                if(!AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
                    tv.setTextColor(ContextCompat.getColor(mContext, R.color.app_black));
                else
                    tv.setTextColor(ContextCompat.getColor(mContext, R.color.app_white));
                tabLayout.getTabAt(i).setCustomView(container);
                tabLayout.getTabAt(i).setTag(categories.get(i).getColor());
            }

            TabLayout.Tab firstTab = tabLayout.getTabAt(0);
            TextViewExtraBold tv = firstTab.getCustomView().findViewById(R.id.tab_textview);
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Averta-Black.otf");
            if(Utils.getAppCurrentLang().equals("ar"))
                tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/ApercuArabicPro-Bold.otf");
            tv.setTypeface(tf);

            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    TextViewExtraBold tv = tab.getCustomView().findViewById(R.id.tab_textview);
                    Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Averta-Black.otf");
                    if(Utils.getAppCurrentLang().equals("ar"))
                        tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/ApercuArabicPro-Bold.otf");
                    tv.setTypeface(tf);
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                    TextViewExtraBold tv = tab.getCustomView().findViewById(R.id.tab_textview);
                    Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Averta-ExtraBold.otf");
                    if(Utils.getAppCurrentLang().equals("ar"))
                        tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/ApercuArabicPro-Medium.otf");
                    tv.setTypeface(tf);
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                    TextViewExtraBold tv = tab.getCustomView().findViewById(R.id.tab_textview);
                    Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Averta-Black.otf");
                    if(Utils.getAppCurrentLang().equals("ar"))
                        tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/ApercuArabicPro-Bold.otf");
                    tv.setTypeface(tf);
                }
            });
        }
        else{
            emptyTextView.setVisibility(View.VISIBLE);
        }
    }
}
