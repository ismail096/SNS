package ma.snrt.news;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import ma.snrt.news.adapter.RelatedAdapter;
import ma.snrt.news.model.Post;
import ma.snrt.news.network.ApiCall;
import ma.snrt.news.network.GsonHelper;
import ma.snrt.news.ui.TextViewBold;
import ma.snrt.news.ui.TextViewEBItalic;
import ma.snrt.news.ui.TextViewRegular;
import ma.snrt.news.ui.TextViewExtraBold;
import ma.snrt.news.util.Cache;
import ma.snrt.news.util.MyContextWrapper;
import ma.snrt.news.util.PlayAudioManager;
import ma.snrt.news.util.Utils;
import ozaydin.serkan.com.image_zoom_view.ImageViewZoom;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static ma.snrt.news.AppController.mFirebaseAnalytics;

//import static ma.snrt.news.AppController.mFirebaseAnalytics;

public class PostDetailActivity extends AppCompatActivity {
    TextViewBold titleTextView;
    TextViewExtraBold likeTextView, relatedTextView, tagsTextView;
    TextViewEBItalic categoryTextView;
    TextViewRegular dateTextView, authorTextView;
    WebView descriptionWv;
    ImageView likeBtn, bookMarksBtn, fontBtn;
    ImageViewZoom postImageview;
    RecyclerView relatedRecyclerView;
    ProgressBar progressBar;
    SeekBar fontSeekBar;
    boolean isFontLayoutVisible;
    Post post;
    ArrayList<Post> related;
    int oldProgress = 0;
    RelatedAdapter relatedAdapter;
    int descriptionTextSize = 15;
    LinearLayout ttsLayout;
    //View ttsShadow;
    //RecyclerView tagsRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean isNightModeEnabled = AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false);
        if (isNightModeEnabled) {
            setTheme(R.style.ActivityThemeDark);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        Cache.initFavoris();
        Cache.initLikedPosts();

        titleTextView = findViewById(R.id.post_title);
        dateTextView = findViewById(R.id.post_date);
        authorTextView = findViewById(R.id.post_author);
        categoryTextView = findViewById(R.id.post_category);
        descriptionWv = findViewById(R.id.post_description);
        postImageview = findViewById(R.id.post_image);
        progressBar = findViewById(R.id.progress_bar);
        relatedRecyclerView = findViewById(R.id.related_recyclerview);
        likeBtn = findViewById(R.id.like_btn);
        likeTextView = findViewById(R.id.like_title);
        relatedTextView = findViewById(R.id.related_title);
        fontSeekBar = findViewById(R.id.font_seekBar);
        bookMarksBtn = findViewById(R.id.bookmark_btn);
        tagsTextView = findViewById(R.id.post_tags);
        fontBtn = findViewById(R.id.fontBtn);
        ttsLayout = findViewById(R.id.post_bottom_layout);
        //ttsShadow = findViewById(R.id.bottom_shadow);
        //tagsRecyclerView = findViewById(R.id.tags_recyclerview);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        relatedRecyclerView.setHasFixedSize(true);
        relatedRecyclerView.setLayoutManager(llm);

        /*LinearLayoutManager llm2 = new LinearLayoutManager(this);
        llm2.setOrientation(LinearLayoutManager.HORIZONTAL);
        tagsRecyclerView.setHasFixedSize(true);
        tagsRecyclerView.setLayoutManager(llm2);*/

        post = (Post) getIntent().getSerializableExtra("post");
        related = new ArrayList<>();
        getPostFromApi(post.getId());

        if(Cache.existsInLikes(post.getId()+""))
            likeBtn.setImageResource(R.drawable.like_full);
        else
            likeBtn.setImageResource(R.drawable.like_empty);

        if(Cache.existsInFavoris(post.getId()+"")){
            bookMarksBtn.setImageResource(R.drawable.bookmarks);
        }
        else{
            if(isNightModeEnabled)
                bookMarksBtn.setImageResource(R.drawable.bookmarks_dark);
            else
                bookMarksBtn.setImageResource(R.drawable.bookmarks_empty);
        }

        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postLike(!Cache.existsInLikes(post.getId()+""));
            }
        });
        oldProgress = AppController.getSharedPreferences().getInt("font_scale", 0);
        fontSeekBar.setProgress(oldProgress);
        fontSeekBar.setMax(4);
        fontSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                SharedPreferences.Editor editor = AppController.getSharedPreferences().edit();
                editor.putInt("font_scale", progress);
                editor.commit();
                setFontSize(oldProgress, progress);
                oldProgress = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        if(Utils.getAppCurrentLang().equals("ar"))
            descriptionTextSize = 18;

        setFontSize(0, oldProgress);

        descriptionWv.getSettings().setJavaScriptEnabled(true);
        descriptionWv.getSettings().setBuiltInZoomControls(false);
        fillPost();

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, post.getId()+"");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, post.getTitle());
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "post");

        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    private void setFontSize(int oldValue, int value) {
        float ratio = value-oldValue;
        float wvRatio = (value  - oldValue) * 2;
        if(Utils.getAppCurrentLang().equals("ar")) {
            //ratio *= Utils.spToPx(getResources(), 4);
            wvRatio = (value  - oldValue) * 4;
        }else
            ratio *= Utils.spToPx(getResources(), 2);
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextView.getTextSize()  + ratio);
        dateTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, dateTextView.getTextSize()  + ratio);
        authorTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, authorTextView.getTextSize()  + ratio);
        tagsTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, tagsTextView.getTextSize()  + ratio);
        categoryTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, categoryTextView.getTextSize()  + ratio);
        likeTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, likeTextView.getTextSize()  + ratio);
        relatedTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, relatedTextView.getTextSize()  + ratio);
        if(relatedAdapter!=null)
            relatedAdapter.notifyDataSetChanged();
        descriptionTextSize += wvRatio;
        descriptionWv.getSettings().setDefaultFontSize(descriptionTextSize);
    }

    private void getPostFromApi(int postId){
        //progressBar.setVisibility(View.VISIBLE);
        final String cacheTag = "post_"+postId+"_"+ Utils.getAppCurrentLang();
        ApiCall.getDetailNews(postId, new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                /*if(progressBar!=null)
                    progressBar.setVisibility(View.GONE);*/
                if(response.body()!=null && response.isSuccessful() && response.body().size()>0){
                    post = GsonHelper.getGson().fromJson(response.body().get(0), Post.class);
                    Cache.putPermanentObject(response.body().get(0).toString(), cacheTag);
                }
                else {
                    String resultFromCache = (String) Cache.getPermanentObject(cacheTag);
                    if(resultFromCache!=null)
                        post = GsonHelper.getGson().fromJson(resultFromCache, Post.class);
                    else
                        Toast.makeText(PostDetailActivity.this, getString(R.string.error_load_data), Toast.LENGTH_SHORT).show();

                }
                fillPost();
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                /*if(progressBar!=null)
                    progressBar.setVisibility(View.GONE);*/
                String resultFromCache = (String) Cache.getPermanentObject(cacheTag);
                if(resultFromCache!=null)
                    post = GsonHelper.getGson().fromJson(resultFromCache, Post.class);
                else
                    Toast.makeText(PostDetailActivity.this, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
                fillPost();
            }
        });
    }

    private void fillPost() {
        if(post!=null) {
            titleTextView.setText(Html.fromHtml(post.getTitle()));
            if(post.getDatePublication()!=null) {
                dateTextView.setText(Utils.getPostRelativeDate(this, post.getDatePublication()));
            }
            if(post.getAuthor()!=null)
                authorTextView.setText(post.getAuthor());
            else {
                authorTextView.setVisibility(View.GONE);
                findViewById(R.id.post_author_divider).setVisibility(View.GONE);
            }
            if(post.getCategory()!=null)
            categoryTextView.setText(Html.fromHtml(post.getCategory()));

            if(Utils.getAppCurrentLang().equals("ar"))
                categoryTextView.setBackgroundResource(R.drawable.category_bg_ar);
            else
                categoryTextView.setBackgroundResource(R.drawable.category_bg);
            if(post.getColor()!=null)
                categoryTextView.getBackground().setColorFilter(Color.parseColor(post.getColor()), PorterDuff.Mode.SRC_ATOP);
            try {
                Picasso.with(this)
                        .load(post.getImage())
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .error(R.drawable.placeholder)
                        .placeholder(R.drawable.placeholder)
                        .into(postImageview, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(PostDetailActivity.this)
                                        .load(post.getImage())
                                         .error(R.drawable.placeholder)
                                        .placeholder(R.drawable.placeholder)
                                        .into(postImageview);
                            }
                        });
            } catch (Exception ex) {
                postImageview.setImageResource(R.drawable.placeholder);
            }

            if(post.getText_speech()!=null && post.getText_speech().contains(".mp3")){
                ttsLayout.setVisibility(View.VISIBLE);
                //ttsShadow.setVisibility(View.VISIBLE);
            }
            else {
                ttsLayout.setVisibility(View.GONE);
                //ttsShadow.setVisibility(View.GONE);
            }

            if(post.getTags()!=null && !post.getTags().isEmpty()) {
                String result = "";
                String[] tagsAsString = post.getTags().replaceAll(" ", "").split(",");
                for(int i=0;i<tagsAsString.length;i++)
                    result+= "#"+tagsAsString[i]+" ";
                SpannableString ss = new SpannableString(result);
                String[] words = result.split(" ");
                for (final String word : words) {
                        ClickableSpan clickableSpan = new ClickableSpan() {
                            @Override
                            public void onClick(View textView) {
                                //use word here to make a decision
                                Intent intent = new Intent(PostDetailActivity.this, NewsActivity.class);
                                //intent.putExtra("is_cat", false);
                                intent.putExtra("tag", word.replace("#", ""));
                                startActivity(intent);
                            }

                            @Override
                            public void updateDrawState(TextPaint ds)
                            {
                                super.updateDrawState(ds);
                                ds.setColor(ContextCompat.getColor(PostDetailActivity.this, R.color.text_grey));
                                ds.setUnderlineText(false);
                            }
                        };
                        if(result.indexOf(word)>-1)
                            ss.setSpan(clickableSpan, result.indexOf(word), result.indexOf(word) + word.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                tagsTextView.setText(ss);
                tagsTextView.setMovementMethod(LinkMovementMethod.getInstance());
                tagsTextView.setHighlightColor(Color.TRANSPARENT);
                tagsTextView.setVisibility(View.VISIBLE);
            }
            else
                tagsTextView.setVisibility(View.GONE);
            //setTagsAdapter();
            getRelatedNews();
            findViewById(R.id.detail_scroll).setVisibility(View.VISIBLE);
            if(post.getDescriptionArticle()!=null)
            {
                String font = "fontFr";
                String color = "#000000";
                String bgColor = "#ffffff";
                String dir= "ltr";
                if(AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false)) {
                    color = "#ffffff";
                    bgColor = "#000000";
                }
                if(Utils.getAppCurrentLang().equals("ar")) {
                    font = "fontAr";
                    dir = "rtl";
                }
                String text = Utils.loadJSONFromAsset("index.html", this);
                text = text.replace("{{content}}", post.getDescriptionArticle());
                text = text.replace("{{myFont}}", font);
                text = text.replace("{{color}}", color);
                text = text.replace("{{bgColor}}", bgColor);
                text = text.replace("{{direction}}", dir);
                descriptionWv.loadDataWithBaseURL("file:///android_asset/", text, "text/html", "utf-8", null);
                descriptionWv.setVisibility(View.VISIBLE);
            }
            else
                descriptionWv.setVisibility(View.GONE);
        }
        else{
            findViewById(R.id.detail_scroll).setVisibility(View.GONE);
            findViewById(R.id.empty_textview).setVisibility(View.VISIBLE);
            relatedRecyclerView.setVisibility(View.GONE);
            findViewById(R.id.related_title).setVisibility(View.GONE);
            findViewById(R.id.related_divider).setVisibility(View.GONE);
        }

    }

    /*private void setTagsAdapter(){
        if(post!=null && post.getTags()!=null && !post.getTags().isEmpty()) {
            ArrayList<Tag> tags = new ArrayList<>();
            String[] tagsAsString = post.getTags().split(",");
            for(int i=0;i<30;i++)
                tags.add(new Tag(i+1, tagsAsString[0]));
            TagAdapter adapter = new TagAdapter(this, tags);
            tagsRecyclerView.setAdapter(adapter);
            tagsRecyclerView.setVisibility(View.VISIBLE);
        }
        else {
            tagsRecyclerView.setVisibility(View.GONE);
        }
    }*/

    private void getRelatedNews(){
        ApiCall.getRelatedNews(post.getId(), new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if(response.body()!=null && response.isSuccessful()){
                    related = GsonHelper.getGson().fromJson(response.body(), new TypeToken<List<Post>>(){}.getType());
                }
                setRelatedAdapter();
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                setRelatedAdapter();
            }
        });
    }

    private void setRelatedAdapter(){
        if(related.size()>0){
            relatedAdapter = new RelatedAdapter(this, related);
            relatedRecyclerView.setAdapter(relatedAdapter);
            relatedRecyclerView.setVisibility(View.VISIBLE);
            findViewById(R.id.related_title).setVisibility(View.VISIBLE);
            findViewById(R.id.related_divider).setVisibility(View.VISIBLE);
        }
        else{
            relatedRecyclerView.setVisibility(View.GONE);
            findViewById(R.id.related_title).setVisibility(View.GONE);
            findViewById(R.id.related_divider).setVisibility(View.GONE);
        }
    }

    private void postLike(final boolean like){
        ApiCall.likePost(like, post.getId(), "article", new Callback<JsonObject>(){
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if(response.isSuccessful() && response.body()!=null){
                    if(response.body().has("status") && response.body().get("status").getAsBoolean()){
                        if(like){
                            Cache.likePost(post.getId()+"");
                            likeBtn.setImageResource(R.drawable.like_full);
                            Utils.setAnimation(PostDetailActivity.this, likeBtn, R.anim.bounce_in);
                        }
                        else{
                            Cache.unLikePost(post.getId()+"");
                            likeBtn.setImageResource(R.drawable.like_empty);
                            Utils.setAnimation(PostDetailActivity.this, likeBtn, R.anim.bounce_out);
                        }
                        return;
                    }
                }
                Toast.makeText(PostDetailActivity.this, getString(R.string.api_error), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(PostDetailActivity.this, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void togglefontLayout(){
        if(isFontLayoutVisible){
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_up);
            findViewById(R.id.font_size_layout).startAnimation(animation);
            findViewById(R.id.font_size_layout).setVisibility(View.GONE);
            findViewById(R.id.blackview).setVisibility(View.GONE);
            fontBtn.setColorFilter(null);
        }
        else{
            findViewById(R.id.font_size_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.blackview).setVisibility(View.VISIBLE);
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_down);
            findViewById(R.id.font_size_layout).startAnimation(animation);
            fontBtn.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
        }
        isFontLayoutVisible = ! isFontLayoutVisible;
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.back_btn:
                onBackPressed();
                break;
            case R.id.fontBtn:
                    togglefontLayout();
                break;
            case R.id.share_btn:
                try {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                    intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_post)+" "+post.getUrl());
                    intent.setType("text/plain");
                    startActivity(Intent.createChooser(intent, getString(R.string.share)));
                } catch(Exception e) {
                    Toast.makeText(this, getString(R.string.api_error), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.bookmark_btn:
                if(!Cache.existsInFavoris(post.getId()+"")){
                    Cache.putPost(post.getId()+"", post);
                    bookMarksBtn.setImageResource(R.drawable.bookmarks);
                    Utils.setAnimation(PostDetailActivity.this, bookMarksBtn, R.anim.bounce_in);
                    //Toast.makeText(this, getString(R.string.add_to_bookmarks), Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Utils.setAnimation(PostDetailActivity.this, bookMarksBtn, R.anim.bounce_out);
                    Cache.removePost(post.getId()+"");
                    if(AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
                        bookMarksBtn.setImageResource(R.drawable.bookmarks_dark);
                    else
                        bookMarksBtn.setImageResource(R.drawable.bookmarks_empty);
                    //Toast.makeText(this, getString(R.string.remove_from_bookmarks), Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.blackview:
                togglefontLayout();
                break;
            case R.id.read_text_btn:
                //startActivity(new Intent(this, TTSActivity.class));
                if(post.getText_speech()!=null && post.getText_speech().contains(".mp3")) {
                        try {
                            PlayAudioManager.playAudio(this, post.getText_speech());
                        } catch (Exception e) {
                            Toast.makeText(this , getString(R.string.api_error), Toast.LENGTH_SHORT).show();
                        }
                }
                else
                    Toast.makeText(this , getString(R.string.api_error), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if(PlayAudioManager.mediaPlayer!=null)
            PlayAudioManager.killMediaPlayer();
        descriptionWv.setVisibility(View.GONE);
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(MyContextWrapper.wrap(newBase, Utils.getAppCurrentLang()));
    }
}
