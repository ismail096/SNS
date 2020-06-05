package ma.snrt.news.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.duolingo.open.rtlviewpager.RtlViewPager;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import ma.snrt.news.AgendaActivity;
import ma.snrt.news.AppController;
import ma.snrt.news.R;

import ma.snrt.news.adapter.CategoryPagerAdapter;
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


public class HomeFragment extends Fragment {
    Context mContext;
    List<Category> categories;
    public RtlViewPager viewPager;
    TabLayout tabLayout;
    TextViewRegular emptyTextView;
    ProgressBar progressBar;
    LinearLayout contentLayout;
    int currentPage = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        mContext = getActivity();
        viewPager = rootView.findViewById(R.id.viewpager);
        tabLayout = rootView.findViewById(R.id.tablayout);
        contentLayout = rootView.findViewById(R.id.content_layout);
        emptyTextView = rootView.findViewById(R.id.empty_textview);
        progressBar = rootView.findViewById(R.id.progress_bar);
        categories = new ArrayList<>();
        categories.add(new Category(2020, getString(R.string.top_news), "#ff0000"));

        /*if(AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
            Glide.with(mContext).load(R.raw.loader_dark).into(progressBar);
        else
            Glide.with(mContext).load(R.raw.loader).into(progressBar);*/

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
                    Cache.putPermanentObject(response.body().toString(), "categories_"+ Utils.getAppCurrentLang());
                    categories.addAll(GsonHelper.getGson().fromJson(response.body(), new TypeToken<List<Category>>(){}.getType()));
                }
                else {
                    String resultFromCache = (String) Cache.getPermanentObject("categories_"+ Utils.getAppCurrentLang());
                    if(resultFromCache!=null)
                        categories.addAll(GsonHelper.getGson().fromJson(resultFromCache, new TypeToken<List<Category>>(){}.getType()));
                }
                setPagerAdapter();
                enableNotifs();
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
            if(Utils.getAppCurrentLang().equals("fr"))
                categories.add(new Category(AppController.AGENDA_FR_ID, getString(R.string.agenda), "#FF265D"));
            else
                categories.add(new Category(AppController.AGENDA_AR_ID, getString(R.string.agenda), "#FF265D"));
            CategoryPagerAdapter adapter = new CategoryPagerAdapter(categories, ((AppCompatActivity) mContext).getSupportFragmentManager());
            viewPager.setAdapter(adapter);
            tabLayout.setupWithViewPager(viewPager);


            for (int i = 0; i < tabLayout.getTabCount(); i++) {
                View container = LayoutInflater.from(mContext).inflate(R.layout.tab_item_layout,null);
                TextViewExtraBold tv = container.findViewById(R.id.tab_textview);
                tv.setText(Html.fromHtml(categories.get(i).getTitle()));
                Typeface tf = Typeface.createFromAsset(mContext.getAssets(), "fonts/Averta-ExtraBold.otf");
                if(Utils.getAppCurrentLang().equals("ar")) {
                    tf = Typeface.createFromAsset(mContext.getAssets(), "fonts/ArbFONTS-DroidKufi-Regular.ttf");
                }
                tv.setTypeface(tf);
                tv.setIncludeFontPadding(false);
                tabLayout.getTabAt(i).setCustomView(container);
                tabLayout.getTabAt(i).setTag(categories.get(i).getColor());

                if(getResources().getBoolean(R.bool.is_tablet)) {
                    Rect bounds = new Rect();
                    Paint textPaint = tv.getPaint();
                    textPaint.setTypeface(tf);
                    textPaint.setTextSize(tv.getTextSize());
                    textPaint.setStyle(Paint.Style.FILL);
                    textPaint.getTextBounds(tv.getText().toString(), 0, tv.getText().toString().length(), bounds);
                    int width = (int) textPaint.measureText(Html.fromHtml(tv.getText().toString()).toString()) * 2;
                    if(Utils.getAppCurrentLang().equals("ar"))
                        width = (int) (textPaint.measureText(tv.getText().toString()) * 1.8f);
                   /* int width = Utils.getTabSize(mContext, i);
                    if(Utils.getAppCurrentLang().equals("ar"))
                        width = Utils.getTabSizeAr(mContext, i);*/
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT);
                    ((LinearLayout) tabLayout.getChildAt(0)).getChildAt(i).setLayoutParams(layoutParams);
                }
            }

            TabLayout.Tab firstTab = tabLayout.getTabAt(0);
            TextViewExtraBold tv = firstTab.getCustomView().findViewById(R.id.tab_textview);
            Typeface tf = Typeface.createFromAsset(mContext.getAssets(), "fonts/Averta-Black.otf");
            if(Utils.getAppCurrentLang().equals("ar"))
                tf = Typeface.createFromAsset(mContext.getAssets(), "fonts/ArbFONTS-DroidKufi-Bold.ttf");
            tv.setTypeface(tf);

            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    TextViewExtraBold tv = tab.getCustomView().findViewById(R.id.tab_textview);
                    Typeface tf = Typeface.createFromAsset(mContext.getAssets(), "fonts/Averta-Black.otf");
                    if(Utils.getAppCurrentLang().equals("ar"))
                        tf = Typeface.createFromAsset(mContext.getAssets(), "fonts/ArbFONTS-DroidKufi-Bold.ttf");
                    tv.setTypeface(tf);
                    tabLayout.setBackgroundColor(Color.parseColor((String) tab.getTag()));
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                    TextViewExtraBold tv = tab.getCustomView().findViewById(R.id.tab_textview);
                    Typeface tf = Typeface.createFromAsset(mContext.getAssets(), "fonts/Averta-ExtraBold.otf");
                    if(Utils.getAppCurrentLang().equals("ar"))
                        tf = Typeface.createFromAsset(mContext.getAssets(), "fonts/ArbFONTS-DroidKufi-Regular.ttf");
                    tv.setTypeface(tf);
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                    TextViewExtraBold tv = tab.getCustomView().findViewById(R.id.tab_textview);
                    Typeface tf = Typeface.createFromAsset(mContext.getAssets(), "fonts/Averta-Black.otf");
                    if(Utils.getAppCurrentLang().equals("ar"))
                        tf = Typeface.createFromAsset(mContext.getAssets(), "fonts/ArbFONTS-DroidKufi-Bold.ttf");
                    tv.setTypeface(tf);
                    tabLayout.setBackgroundColor(Color.parseColor((String) tab.getTag()));
                }
            });

            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    /*if(position==categories.size()-1){
                        startActivity(new Intent(mContext, AgendaActivity.class));
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                viewPager.setCurrentItem(currentPage);
                            }
                        }, 1000);
                        return;
                    }*/
                    currentPage = position;
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }
        else{
            emptyTextView.setVisibility(View.VISIBLE);
        }
    }

    public void selectTopNews(){
        viewPager.setCurrentItem(0);
    }

    public void selectAgenda(){
        viewPager.setCurrentItem(categories.size()-1);
    }

    private void enableNotifs(){
        if(!AppController.getSharedPreferences().contains("notif_enabled")) {
            SharedPreferences.Editor editor = AppController.getSharedPreferences().edit();
            FirebaseMessaging.getInstance().subscribeToTopic("snrtnews_story_"+Utils.getAppCurrentLang());
            if(categories.size()>0){
                for(int i=0;i<categories.size();i++) {
                    FirebaseMessaging.getInstance().subscribeToTopic("snrtnews_" + categories.get(i).getId());
                    editor.putBoolean("notif_"+categories.get(i).getId(), true);
                }
            }
            editor.putBoolean("notif_enabled", true);
            editor.putBoolean("notif_story", true);
            editor.commit();
        }
    }
}
