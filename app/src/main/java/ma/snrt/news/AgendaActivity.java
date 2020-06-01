package ma.snrt.news;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Typeface;
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
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.duolingo.open.rtlviewpager.RtlViewPager;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import ma.snrt.news.adapter.AgendaAdapter2;
import ma.snrt.news.adapter.TopAgendaAdapter;
import ma.snrt.news.model.CategoryAgenda;
import ma.snrt.news.model.Post;
import ma.snrt.news.model.Ville;
import ma.snrt.news.network.ApiCall;
import ma.snrt.news.network.GsonHelper;
import ma.snrt.news.ui.CenterZoomLayoutManager;
import ma.snrt.news.ui.EditTextRegular;
import ma.snrt.news.ui.TextViewBold;
import ma.snrt.news.ui.TextViewRegular;
import ma.snrt.news.ui.TextViewExtraBold;
import ma.snrt.news.util.Cache;
import ma.snrt.news.util.MyContextWrapper;
import ma.snrt.news.util.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AgendaActivity extends AppCompatActivity {

    TextViewRegular emptyTextView;
    TextViewBold pageTitle;
    ProgressBar progressBar;
    LinearLayout contentLayout, filtreLayout;
    RecyclerView recyclerView, topRecyclerview;

    boolean isDate1Clicked, isFiltreVisible;
    EditTextRegular date1Edit, date2Edit;
    AutoCompleteTextView positionEdit;
    public String dateDebut = "", dateFin = "";
    public String position = "";
    View blackView;
    CategoryAgenda category;
    ArrayList<Post> posts = new ArrayList<>();
    List<Post> tops = new ArrayList<>();
    int page = 0;
    boolean isListLoaded;
    AgendaAdapter2 agendaAdapter;
    TopAgendaAdapter topAdapter;
    LinearSnapHelper snapHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean isNightModeEnabled = AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false);
        if (isNightModeEnabled) {
            setTheme(R.style.ActivityThemeDark);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agenda);

        Cache.initFavorisAgenda();

        recyclerView = findViewById(R.id.agenda_recyclerview);
        topRecyclerview = findViewById(R.id.agenda_top_recyclerview);

        contentLayout = findViewById(R.id.content_layout);
        emptyTextView = findViewById(R.id.empty_textview);
        progressBar = findViewById(R.id.progress_bar);
        pageTitle = findViewById(R.id.main_title);

        filtreLayout = findViewById(R.id.filtre_layout);
        positionEdit = findViewById(R.id.filtre_position);
        date1Edit = findViewById(R.id.filtre_date1);
        date2Edit = findViewById(R.id.filtre_date2);
        blackView = findViewById(R.id.blackview);

        if(getIntent().hasExtra("category")) {
            category = (CategoryAgenda) getIntent().getSerializableExtra("category");
            pageTitle.setText(Html.fromHtml(category.getTitle()));
        }
        else
            category = new CategoryAgenda();

        /*if(AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
            Glide.with(this).load(R.raw.loader_dark).into(progressBar);
        else
            Glide.with(this).load(R.raw.loader).into(progressBar);*/

        final Calendar myCalendar = Calendar.getInstance();

        final DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                String day = dayOfMonth+"";
                String month = (monthOfYear+1)+"";
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
                new DatePickerDialog(AgendaActivity.this, dateSetListener, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        date2Edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDate1Clicked = false;
                new DatePickerDialog(AgendaActivity.this, dateSetListener, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        int itemsCount = 2;
        if(getResources().getBoolean(R.bool.is_tablet))
            itemsCount = 3;
        GridLayoutManager llm = new GridLayoutManager(this, itemsCount);
        recyclerView.setLayoutManager(llm);

        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Averta-Regular.otf");
        if(AppController.getSharedPreferences().getString("lang", "").equals("ar")) {
            tf = Typeface.createFromAsset(getAssets(), "fonts/ArbFONTS-DroidKufi-Regular.ttf");
            positionEdit.setIncludeFontPadding(false);
        }
        positionEdit.setTypeface(tf);

        setPositionAutoCompelete();
        getAgendas();
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

    private void getAgendas(){
        String cacheTag = "agenda_"+category.getId()+"_"+ Utils.getAppCurrentLang();
        if(page == 0)
            progressBar.setVisibility(View.VISIBLE);
        ApiCall.getAgendaByCat(category.getId(), position, dateDebut, dateFin, page, new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if(progressBar!=null)
                    progressBar.setVisibility(View.GONE);
                /*if(swipeRefreshLayout!=null)
                    swipeRefreshLayout.setRefreshing(false);*/
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
                        Toast.makeText(AgendaActivity.this, AgendaActivity.this.getString(R.string.error_load_data), Toast.LENGTH_SHORT).show();
                        isListLoaded = true;
                    }
                }
                setListAdapter();
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                if(progressBar!=null)
                    progressBar.setVisibility(View.GONE);
                /*if(swipeRefreshLayout!=null)
                    swipeRefreshLayout.setRefreshing(false);*/
                if(page ==0) {
                    String resultFromCache = (String) Cache.getPermanentObject(cacheTag);
                    if(resultFromCache!=null)
                        posts = GsonHelper.getGson().fromJson(resultFromCache, new TypeToken<List<Post>>(){}.getType());
                    Toast.makeText(AgendaActivity.this, AgendaActivity.this.getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
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
                agendaAdapter = new AgendaAdapter2(AgendaActivity.this, posts, recyclerView);
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
        ApiCall.getFeaturedAgenda(category.getId(), position, dateDebut, dateFin, new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                /*if(swipeRefreshLayout!=null)
                    swipeRefreshLayout.setRefreshing(false);*/
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
                /*if(swipeRefreshLayout!=null)
                    swipeRefreshLayout.setRefreshing(false);*/
                String resultFromCache = (String) Cache.getPermanentObject(cacheTag);
                if(resultFromCache!=null)
                    tops = GsonHelper.getGson().fromJson(resultFromCache, new TypeToken<List<Post>>(){}.getType());
                setTopListAdapter();
            }
        });
    }

    private void setTopListAdapter(){
        if(tops.size()>0){
            if(tops.size()>4)
                tops = tops.subList(0, 5);
            if(Utils.getAppCurrentLang().equals("ar"))
                Collections.reverse(tops);
            topRecyclerview.setVisibility(View.VISIBLE);
            CenterZoomLayoutManager layoutManager =
                    new CenterZoomLayoutManager(AgendaActivity.this, LinearLayoutManager.HORIZONTAL, false);
            topRecyclerview.setLayoutManager(layoutManager);
            topAdapter = new TopAgendaAdapter(AgendaActivity.this, tops);
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
                    page = 0;
                    posts.clear();
                    tops.clear();
                    topRecyclerview.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                    emptyTextView.setVisibility(View.GONE);
                    position = positionEdit.getText().toString();
                    getAgendas();
                }
                isFiltreVisible = false;
                break;
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(MyContextWrapper.wrap(newBase, Utils.getAppCurrentLang()));
    }
}
