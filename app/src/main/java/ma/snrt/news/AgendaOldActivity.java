package ma.snrt.news;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.duolingo.open.rtlviewpager.RtlViewPager;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ma.snrt.news.adapter.AgendaPagerAdapter;
import ma.snrt.news.model.CategoryAgenda;
import ma.snrt.news.model.Post;
import ma.snrt.news.model.Ville;
import ma.snrt.news.network.ApiCall;
import ma.snrt.news.network.GsonHelper;
import ma.snrt.news.ui.TextViewExtraBold;
import ma.snrt.news.ui.TextViewRegular;
import ma.snrt.news.util.Cache;
import ma.snrt.news.util.MyContextWrapper;
import ma.snrt.news.util.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AgendaOldActivity extends AppCompatActivity {
    List<CategoryAgenda> categories;
    RtlViewPager viewPager;
    TabLayout tabLayout;
    TextViewRegular emptyTextView;
    ImageView progressBar;
    LinearLayout contentLayout;
    boolean isDate1Clicked, isFiltreVisible;
    LinearLayout filtreLayout;
    AppCompatEditText date1Edit, date2Edit;
    AutoCompleteTextView positionEdit;
    public String dateDebut = "", dateFin = "";
    public String position = "";
    View blackView;
    AgendaPagerAdapter adapter;
    int page = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean isNightModeEnabled = AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false);
        if (isNightModeEnabled) {
            setTheme(R.style.ActivityThemeDark);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agenda);

        Cache.initFavorisAgenda();

        viewPager = findViewById(R.id.viewpager);
        tabLayout = findViewById(R.id.tablayout);
        contentLayout = findViewById(R.id.content_layout);
        emptyTextView = findViewById(R.id.empty_textview);
        progressBar = findViewById(R.id.progress_bar);

        filtreLayout = findViewById(R.id.filtre_layout);
        positionEdit = findViewById(R.id.filtre_position);
        date1Edit = findViewById(R.id.filtre_date1);
        date2Edit = findViewById(R.id.filtre_date2);
        blackView = findViewById(R.id.blackview);

        viewPager.setOffscreenPageLimit(3);
        categories = new ArrayList<>();

        page = getIntent().getIntExtra("position", 0);

        if(AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
            Glide.with(this).load(R.raw.loader_dark).into(progressBar);
        else
            Glide.with(this).load(R.raw.loader).into(progressBar);

        final Calendar myCalendar = Calendar.getInstance();

        final DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                String day = dayOfMonth+"";
                String month = monthOfYear+"";
                if(dayOfMonth<10)
                    day = "0"+ day;
                if(monthOfYear<10)
                    month = "0"+month;
                if(isDate1Clicked){
                    date1Edit.setText(day+"/"+month+"/"+year);
                    dateDebut = year+"-"+month+"-"+day;
                }
                else {
                    date2Edit.setText(day + "/" + month + "/" + year);
                    dateFin = year+"-"+month+"-"+day;
                }
            }
        };

        date1Edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDate1Clicked = true;
                new DatePickerDialog(AgendaOldActivity.this, dateSetListener, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        date2Edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDate1Clicked = false;
                new DatePickerDialog(AgendaOldActivity.this, dateSetListener, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        setPositionAutoCompelete();
        getCategories();
    }

    private void setPositionAutoCompelete(){
        String villesFromJson = "";
        if(Utils.getAppCurrentLang().equals("fr"))
            villesFromJson = Utils.loadJSONFromAsset("ville.json", this);
        else
            villesFromJson = Utils.loadJSONFromAsset("ville_ar.json", this);
        ArrayList<Ville> villes = GsonHelper.getGson().fromJson(villesFromJson, new TypeToken<List<Ville>>(){}.getType());
        ArrayList<String> villesAsString = new ArrayList<>();
        for(int i=0;i<villes.size();i++){
            villesAsString.add(villes.get(i).getVille());
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,
                R.layout.autocomplete_item, villesAsString);
        positionEdit.setThreshold(1);
        positionEdit.setAdapter(arrayAdapter);
    }

    private void getCategories(){
        progressBar.setVisibility(View.VISIBLE);
        ApiCall.getCategoriesAgenda(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if(progressBar!=null)
                    progressBar.setVisibility(View.GONE);
                if(response.body()!=null && response.isSuccessful()){
                    categories.addAll(GsonHelper.getGson().fromJson(response.body(), new TypeToken<List<CategoryAgenda>>(){}.getType()));
                }
                else {
                    String resultFromCache = (String) Cache.getPermanentObject("categories_ag_"+ Utils.getAppCurrentLang());
                    if(resultFromCache!=null)
                        categories.addAll(GsonHelper.getGson().fromJson(resultFromCache, new TypeToken<List<Post>>(){}.getType()));
                }
                setPagerAdapter();
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                if(progressBar!=null)
                    progressBar.setVisibility(View.GONE);
                String resultFromCache = (String) Cache.getPermanentObject("categories_ag_"+ Utils.getAppCurrentLang());
                if(resultFromCache!=null)
                    categories.addAll(GsonHelper.getGson().fromJson(resultFromCache, new TypeToken<List<Post>>(){}.getType()));
                setPagerAdapter();
            }
        });
    }

    TabLayout.OnTabSelectedListener tabListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            TextViewExtraBold tv = tab.getCustomView().findViewById(R.id.tab_textview);
            tv.setTextColor(ContextCompat.getColor(AgendaOldActivity.this, R.color.app_white));
            tv.setBackgroundColor(ContextCompat.getColor(AgendaOldActivity.this, R.color.tab_agenda_select));
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
            TextViewExtraBold tv = tab.getCustomView().findViewById(R.id.tab_textview);
            tv.setTextColor(ContextCompat.getColor(AgendaOldActivity.this, R.color.app_black));
            tv.setBackgroundColor(ContextCompat.getColor(AgendaOldActivity.this, R.color.agenda_color));
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
            TextViewExtraBold tv = tab.getCustomView().findViewById(R.id.tab_textview);
            tv.setTextColor(ContextCompat.getColor(AgendaOldActivity.this, R.color.app_white));
            tv.setBackgroundColor(ContextCompat.getColor(AgendaOldActivity.this, R.color.tab_agenda_select));
        }
    };

    private void setPagerAdapter(){
        if(categories.size()>0){
            contentLayout.setVisibility(View.VISIBLE);
            adapter = new AgendaPagerAdapter(categories, getSupportFragmentManager());
            viewPager.setAdapter(adapter);
            tabLayout.setupWithViewPager(viewPager);
            for (int i = 0; i < tabLayout.getTabCount(); i++) {
                View container = LayoutInflater.from(this).inflate(R.layout.tab_ag_item_layout,null);
                TextViewExtraBold tv = container.findViewById(R.id.tab_textview);
                tv.setText(Html.fromHtml(categories.get(i).getTitle()));
                tv.setTextColor(ContextCompat.getColor(this, R.color.app_black));
                if(Utils.getAppCurrentLang().equals("ar"))
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                tv.setBackgroundColor(ContextCompat.getColor(AgendaOldActivity.this, R.color.agenda_color));
                tabLayout.getTabAt(i).setCustomView(container);
                ViewGroup.LayoutParams layoutParams = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                tabLayout.getTabAt(i).getCustomView().setLayoutParams(layoutParams);
            }

            TabLayout.Tab firstTab = tabLayout.getTabAt(0);
            TextViewExtraBold tv = firstTab.getCustomView().findViewById(R.id.tab_textview);
            tv.setTextColor(ContextCompat.getColor(AgendaOldActivity.this, R.color.app_white));
            tv.setBackgroundColor(ContextCompat.getColor(AgendaOldActivity.this, R.color.tab_agenda_select));

            tabLayout.addOnTabSelectedListener(tabListener);

            /*viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                        page = position;
                        if(position==0)
                            findViewById(R.id.filtre_btn).setVisibility(View.GONE);
                        else
                            findViewById(R.id.filtre_btn).setVisibility(View.VISIBLE);
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });*/
            viewPager.setCurrentItem(page);
        }
        else{
            emptyTextView.setVisibility(View.VISIBLE);
        }
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.back_btn:
                onBackPressed();
                break;
            case R.id.filtre_btn: case R.id.blackview:
                if(isFiltreVisible){
                    Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_up);
                    filtreLayout.startAnimation(animation);
                    filtreLayout.setVisibility(View.GONE);
                    blackView.setVisibility(View.GONE);
                }
                else{
                    filtreLayout.setVisibility(View.VISIBLE);
                    blackView.setVisibility(View.VISIBLE);
                    Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_down);
                    filtreLayout.startAnimation(animation);
                }
                isFiltreVisible = !isFiltreVisible;
                break;

            case R.id.submit_btn:
                if(isFiltreVisible){
                    Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_up);
                    filtreLayout.startAnimation(animation);
                    filtreLayout.setVisibility(View.GONE);
                    blackView.setVisibility(View.GONE);
                    Utils.closeKeyboard(this, positionEdit);
                }
                isFiltreVisible = false;
                position = positionEdit.getText().toString();
                //if(page>0){
                    tabLayout.removeOnTabSelectedListener(tabListener);
                    adapter.notifyDataSetChanged();

                    for (int i = 0; i < tabLayout.getTabCount(); i++) {
                        View container = LayoutInflater.from(this).inflate(R.layout.tab_ag_item_layout,null);
                        TextViewExtraBold tv = container.findViewById(R.id.tab_textview);
                        tv.setText(Html.fromHtml(categories.get(i).getTitle()));
                        tv.setTextColor(ContextCompat.getColor(this, R.color.app_black));
                        tv.setBackgroundColor(ContextCompat.getColor(AgendaOldActivity.this, R.color.agenda_color));
                        tabLayout.getTabAt(i).setCustomView(container);
                        ViewGroup.LayoutParams layoutParams = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        tabLayout.getTabAt(i).getCustomView().setLayoutParams(layoutParams);
                    }

                    TabLayout.Tab tab = tabLayout.getTabAt(page);
                    TextViewExtraBold tv = tab.getCustomView().findViewById(R.id.tab_textview);
                    tv.setTextColor(ContextCompat.getColor(AgendaOldActivity.this, R.color.app_white));
                    tv.setBackgroundColor(ContextCompat.getColor(AgendaOldActivity.this, R.color.tab_agenda_select));
                    tabLayout.addOnTabSelectedListener(tabListener);
                //}
                break;
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(MyContextWrapper.wrap(newBase, Utils.getAppCurrentLang()));
    }
}
