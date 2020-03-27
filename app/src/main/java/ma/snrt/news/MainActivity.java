package ma.snrt.news;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.duolingo.open.rtlviewpager.RtlViewPager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.JsonArray;

import ma.snrt.news.adapter.HomePagerAdapter;
import ma.snrt.news.fragment.HomeFragment;
import ma.snrt.news.fragment.SettingsFragment;
import ma.snrt.news.model.Post;
import ma.snrt.news.network.ApiCall;
import ma.snrt.news.network.GsonHelper;
import ma.snrt.news.ui.TextViewBold;
import ma.snrt.news.util.Cache;
import ma.snrt.news.util.MyContextWrapper;
import ma.snrt.news.util.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    public RtlViewPager viewPager;
    public DrawerLayout drawerLayout;
    ImageView liveBtn, mainLogo;
    TextViewBold title;
    Post livePost;
    HomePagerAdapter homePagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isNightModeEnabled = AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false);
        if (isNightModeEnabled) {
            setTheme(R.style.ActivityThemeDark);
        }
        setContentView(R.layout.activity_main);

        Cache.initFavorisVideos();
        Cache.initFavoris();

        Toolbar toolbar = findViewById(R.id.toolbar);
        viewPager = findViewById(R.id.home_viewpager);
        ImageView searchBtn = toolbar.findViewById(R.id.searchBtn);
        liveBtn = findViewById(R.id.liveBtn);
        mainLogo = findViewById(R.id.main_logo);
        title = findViewById(R.id.main_title);

        viewPager.setOffscreenPageLimit(4);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayUseLogoEnabled(false);

        drawerLayout = findViewById(R.id.drawer_layout);
       /* ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0);
        drawerLayout.addDrawerListener(toggle);

        toggle.setDrawerIndicatorEnabled(false);
        int drawerRes = R.drawable.menu_drawer;
        if(isNightModeEnabled)
            drawerRes = R.drawable.menu_drawer_dark;
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), drawerRes, getTheme());
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, Utils.dpToPx(getResources(), 14), Utils.dpToPx(getResources(), 14), true));
        toggle.setHomeAsUpIndicator(d);
        toggle.syncState();

        toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });*/

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SearchActivity.class));
            }
        });

        setupMenuDrawer();
        setupBottomMenu();
        setupHome();
        checkLive();
    }

    private void checkLive() {
        if(Utils.getAppCurrentLang().equals("fr"))
            Glide.with(this).load(R.raw.live_fr).into(liveBtn);
        else
            Glide.with(this).load(R.raw.live_ar).into(liveBtn);
        ApiCall.getLive(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if(response.body()!=null && response.isSuccessful() && response.body().size()>0){
                    livePost = GsonHelper.getGson().fromJson(response.body().get(0), Post.class);
                    if(livePost!=null && liveBtn!=null){
                        liveBtn.setVisibility(View.VISIBLE);
                        liveBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(MainActivity.this, PostDetailActivity.class);
                                intent.putExtra("post", livePost);
                                startActivity(intent);
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {

            }
        });
    }

    private void setupMenuDrawer() {
        SettingsFragment fragment = new SettingsFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.menu_lateral, fragment).commit();
    }

    private void setupBottomMenu() {
        bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.home_icon:
                        viewPager.setCurrentItem(0);
                        mainLogo.setVisibility(View.VISIBLE);
                        title.setVisibility(View.GONE);
                       break;
                    case R.id.flash_icon:
                        viewPager.setCurrentItem(1);
                        mainLogo.setVisibility(View.GONE);
                        title.setVisibility(View.VISIBLE);
                        title.setText(getString(R.string.flash_news));
                        break;
                    case R.id.media_icon:
                        viewPager.setCurrentItem(2);
                        mainLogo.setVisibility(View.GONE);
                        title.setVisibility(View.VISIBLE);
                        title.setText(getString(R.string.videos));
                        break;
                    case R.id.fav_icon:
                        viewPager.setCurrentItem(3);
                        mainLogo.setVisibility(View.GONE);
                        title.setVisibility(View.VISIBLE);
                        title.setText(getString(R.string.favoris));
                        break;
                }
                return false;
            }
        });
    }

    private void setupHome() {
        homePagerAdapter = new HomePagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(homePagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position){
                    case 0:
                        bottomNavigationView.getMenu().findItem(R.id.home_icon).setChecked(true);
                        mainLogo.setVisibility(View.VISIBLE);
                        title.setVisibility(View.GONE);
                        break;
                    case 1:
                        bottomNavigationView.getMenu().findItem(R.id.flash_icon).setChecked(true);
                        mainLogo.setVisibility(View.GONE);
                        title.setVisibility(View.VISIBLE);
                        title.setText(getString(R.string.flash_news));
                        break;
                    case 2:
                        bottomNavigationView.getMenu().findItem(R.id.media_icon).setChecked(true);
                        mainLogo.setVisibility(View.GONE);
                        title.setVisibility(View.VISIBLE);
                        title.setText(getString(R.string.videos));
                        break;
                    case 3:
                        bottomNavigationView.getMenu().findItem(R.id.fav_icon).setChecked(true);
                        mainLogo.setVisibility(View.GONE);
                        title.setVisibility(View.VISIBLE);
                        title.setText(getString(R.string.favoris));
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        bottomNavigationView.getMenu().findItem(R.id.home_icon).setChecked(true);
    }


    public void onClick(View v){
        if(v.getId() == R.id.menu_drawer){
            if(drawerLayout.isDrawerOpen(GravityCompat.START)){
                drawerLayout.closeDrawer(GravityCompat.START);
            }
            else
                drawerLayout.openDrawer(GravityCompat.START);
        }
        else if(v.getId() == R.id.main_logo){
            if(viewPager.getCurrentItem() == 0 && homePagerAdapter!=null){
                HomeFragment frag1 = (HomeFragment) viewPager
                        .getAdapter()
                        .instantiateItem(viewPager, viewPager.getCurrentItem());
                frag1.selectTopNews();
            }
        }
    }
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(MyContextWrapper.wrap(newBase, Utils.getAppCurrentLang()));
    }
}
