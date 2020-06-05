package ma.snrt.news;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.Html;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

import ma.snrt.news.model.Post;
import ma.snrt.news.ui.TextViewRegular;
import ma.snrt.news.ui.TextViewExtraBold;
import ma.snrt.news.util.Cache;
import ma.snrt.news.util.DateTimeUtils;
import ma.snrt.news.util.MyContextWrapper;
import ma.snrt.news.util.PermissionsHelper;
import ma.snrt.news.util.Utils;

public class AgendaDetailActivity extends AppCompatActivity {
    TextViewExtraBold titleTextView, categoryTextView;
    TextViewRegular dateTextView, positionTextView;
    ImageView fontBtn, favBtn, postImageView, coverImageView;
    WebView descriptionWv;
    ProgressBar progressBar;
    SeekBar fontSeekBar;
    boolean isFontLayoutVisible;
    Post post;
    int oldProgress = 0;
    int descriptionTextSize = 14;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean isNightModeEnabled = AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false);
        if (isNightModeEnabled) {
            setTheme(R.style.ActivityThemeDark);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agenda_detail);

        Cache.initFavorisAgenda();


        titleTextView = findViewById(R.id.post_title);
        dateTextView = findViewById(R.id.post_date);
        positionTextView = findViewById(R.id.post_position);
        descriptionWv = findViewById(R.id.post_description);
        progressBar = findViewById(R.id.progress_bar);
        fontSeekBar = findViewById(R.id.font_seekBar);
        fontBtn = findViewById(R.id.fontBtn);
        favBtn = findViewById(R.id.fav_btn);
        postImageView = findViewById(R.id.agenda_post_image);
        coverImageView = findViewById(R.id.agenda_cover_image);
        categoryTextView = findViewById(R.id.post_category);

        post = (Post) getIntent().getSerializableExtra("post");

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

        //setFontSize(0, oldProgress);
        if(getResources().getBoolean(R.bool.is_tablet)) {
            descriptionTextSize = 18;
            float ratio = Utils.spToPx(getResources(), 4);
            categoryTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, categoryTextView.getTextSize() + ratio);
            titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextView.getTextSize() + ratio);
            dateTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, dateTextView.getTextSize() + ratio);
            positionTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, positionTextView.getTextSize() + ratio);
        }

        /*if (AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
            Glide.with(this).load(R.raw.loader_dark).into(progressBar);
        else
            Glide.with(this).load(R.raw.loader).into(progressBar);*/

        fillPost();

        descriptionWv.getSettings().setJavaScriptEnabled(true);
        descriptionWv.getSettings().setBuiltInZoomControls(false);
        descriptionWv.getSettings().setDefaultFontSize(descriptionTextSize);
    }

    private void setFontSize(int oldValue, int value) {
        float ratio = value - oldValue;
        float wvRatio = (value - oldValue) * 2;
        ratio *= Utils.spToPx(getResources(), 2);
        categoryTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, categoryTextView.getTextSize() + ratio);
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextView.getTextSize() + ratio);
        dateTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, dateTextView.getTextSize() + ratio);
        positionTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, positionTextView.getTextSize() + ratio);
        descriptionTextSize += wvRatio;
        descriptionWv.getSettings().setDefaultFontSize(descriptionTextSize);
    }

    private void fillPost() {
        if (post != null) {
            if (post.getCategory() != null)
                categoryTextView.setText(Html.fromHtml(post.getCategory()));
            titleTextView.setText(Html.fromHtml(post.getTitle()));
            if (post.getDatePublication() != null) {
                dateTextView.setText(Utils.getPostRelativeDate(this, post.getDatePublication()));
            }
            positionTextView.setText(post.getPosition());
            findViewById(R.id.detail_scroll).setVisibility(View.VISIBLE);
            setImage(postImageView, post.getImage());
            setImage(coverImageView, post.getImage());
            if (post.getColor() != null && post.getColor().length() > 7)
                categoryTextView.setBackgroundColor(Color.parseColor(post.getColor()));
            else
                categoryTextView.setBackgroundColor(ContextCompat.getColor(this, R.color.tab_agenda_select));
            if (!Cache.existsInAgendaFav(post.getId() + "")) {
                if (!AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
                    favBtn.setImageResource(R.drawable.bookmarks_empty);
                else
                    favBtn.setImageResource(R.drawable.bookmarks_dark);
            } else
                favBtn.setImageResource(R.drawable.bookmarks);

            if (post.getDescription() != null) {
                String font = "fontFr";
                String color = "#000000";
                String bgColor = "#ffffff";
                String dir= "ltr";

                if (AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false)) {
                    color = "#ffffff";
                    bgColor = "#000000";
                }
                if(Utils.getAppCurrentLang().equals("ar")) {
                    font = "fontAr";
                    dir = "rtl";
                }
                String text = Utils.loadJSONFromAsset("index.html", this);
                text = text.replace("{{resumeContent}}", "");
                text = text.replace("{{content}}", post.getDescription());
                text = text.replace("{{myFont}}", font);
                text = text.replace("{{color}}", color);
                text = text.replace("{{bgColor}}", bgColor);
                text = text.replace("{{direction}}", dir);
                descriptionWv.loadDataWithBaseURL("file:///android_asset/", text, "text/html", "utf-8", null);
            } else
                descriptionWv.setVisibility(View.GONE);
        } else {
            findViewById(R.id.detail_scroll).setVisibility(View.GONE);
            findViewById(R.id.empty_textview).setVisibility(View.VISIBLE);
            findViewById(R.id.related_title).setVisibility(View.GONE);
            findViewById(R.id.related_divider).setVisibility(View.GONE);
        }

    }

    private void setImage(ImageView imageView, String url) {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.placeholder);
        requestOptions.error(R.drawable.placeholder);
        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL);

        try {

            Glide.with(this)
                    .load(url)
                    .apply(requestOptions)
                    .into(imageView);
        }catch(Exception e){
            imageView.setImageResource(R.drawable.placeholder);
        }
    }


    private void togglefontLayout() {
        if (isFontLayoutVisible) {
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_up);
            findViewById(R.id.font_size_layout).startAnimation(animation);
            findViewById(R.id.font_size_layout).setVisibility(View.GONE);
            findViewById(R.id.blackview).setVisibility(View.GONE);
            fontBtn.setColorFilter(null);
        } else {
            findViewById(R.id.font_size_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.blackview).setVisibility(View.VISIBLE);
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_down);
            findViewById(R.id.font_size_layout).startAnimation(animation);
            fontBtn.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
        }
        isFontLayoutVisible = !isFontLayoutVisible;
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_btn:
                onBackPressed();
                break;
            case R.id.fontBtn:
                togglefontLayout();
                break;
            case R.id.share_btn:
                try {
                    String shareText = /*getString(R.string.share_post) +" "+;*/ post.getTitle()+" " + post.getUrl();
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                    intent.putExtra(Intent.EXTRA_TEXT, shareText);
                    intent.setType("text/plain");
                    startActivity(Intent.createChooser(intent, getString(R.string.share)));
                } catch (Exception e) {
                    Toast.makeText(this, getString(R.string.api_error), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.fav_btn:
                if (!Cache.existsInAgendaFav(post.getId() + "")) {
                    Utils.setAnimation(this, favBtn, R.anim.bounce_in);
                    Cache.putAgendaToFav(post.getId() + "", post);
                    favBtn.setImageResource(R.drawable.bookmarks);
                } else {
                    Utils.setAnimation(this, favBtn, R.anim.bounce_out);
                    Cache.removeAgendaFromFav(post.getId() + "");
                    if (AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
                        favBtn.setImageResource(R.drawable.bookmarks_dark);
                    else
                        favBtn.setImageResource(R.drawable.bookmarks_empty);
                }
                break;
            case R.id.blackview:
                togglefontLayout();
                break;

            case R.id.pinBtn:
                Utils.setAnimation(this, findViewById(R.id.pinBtn), R.anim.bounce_in);
                addToCalendar();
                break;
        }
    }

    private void addToCalendar() {
        /*Calendar beginTime = Calendar.getInstance();
        beginTime.set(2012, 0, 19, 7, 30);
        Calendar endTime = Calendar.getInstance();
        endTime.set(2012, 0, 19, 8, 30);*/
        long dateToTimeStamp = DateTimeUtils.getTimeStamp(post.getDate());
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, dateToTimeStamp)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, dateToTimeStamp)
                .putExtra(CalendarContract.Events.TITLE, post.getTitle())
                //.putExtra(CalendarContract.Events.DESCRIPTION, post.getDescription())
                .putExtra(CalendarContract.Events.EVENT_LOCATION, post.getPosition());
                //.putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_FREE)
                //.putExtra(Intent.EXTRA_EMAIL, "rowan@example.com,trevor@example.com")
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==1){
            if(PermissionsHelper.hasPermission(this, Manifest.permission.WRITE_CALENDAR)){
                addToCalendar();
            }
            else
                PermissionsHelper.askPermission(this, Manifest.permission.WRITE_CALENDAR, 1);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(MyContextWrapper.wrap(newBase, Utils.getAppCurrentLang()));
    }
}
