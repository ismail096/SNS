package ma.snrt.news;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonArray;

import ma.snrt.news.model.Page;
import ma.snrt.news.network.ApiCall;
import ma.snrt.news.network.GsonHelper;
import ma.snrt.news.ui.TextViewBold;
import ma.snrt.news.ui.TextViewRegular;
import ma.snrt.news.ui.TextViewExtraBold;
import ma.snrt.news.util.Cache;
import ma.snrt.news.util.MyContextWrapper;
import ma.snrt.news.util.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WebViewActivity extends AppCompatActivity {
    WebView webView;
    TextViewRegular emptyView;
    TextViewBold pageTitle;
    ProgressBar progressBar;
    Page page;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean isNightModeEnabled = AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false);
        if (isNightModeEnabled) {
            setTheme(R.style.ActivityThemeDark);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        webView = findViewById(R.id.webview);
        emptyView = findViewById(R.id.empty_textview);
        pageTitle = findViewById(R.id.page_title);
        progressBar = findViewById(R.id.progress_bar);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(false);
        if(Utils.getAppCurrentLang().equals("ar"))
            webView.getSettings().setDefaultFontSize(18);
        else
            webView.getSettings().setDefaultFontSize(15);

        int pageId =  getIntent().getIntExtra("pageId", 0);
        loadPage(pageId);
    }

    private void loadPage(final int pageId) {
        final String cacheTag = "page_"+pageId+""+ Utils.getAppCurrentLang();
        progressBar.setVisibility(View.VISIBLE);
        ApiCall.getStaticPage(pageId, new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {

                if(progressBar!=null)
                    progressBar.setVisibility(View.GONE);
                if(response.body()!=null && response.isSuccessful()){
                    page = GsonHelper.getGson().fromJson(response.body().get(0), Page.class);
                    Cache.putPermanentObject(response.body().get(0).toString(), cacheTag);
                }
                else {
                    String resultFromCache = (String) Cache.getPermanentObject(cacheTag);
                    if(resultFromCache!=null)
                        page = GsonHelper.getGson().fromJson(resultFromCache, Page.class);
                    Toast.makeText(WebViewActivity.this, getString(R.string.error_load_data), Toast.LENGTH_SHORT).show();
                }
                setPageContent();
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                if(progressBar!=null)
                    progressBar.setVisibility(View.GONE);
                String resultFromCache = (String) Cache.getPermanentObject(cacheTag);
                if(resultFromCache!=null)
                    page = GsonHelper.getGson().fromJson(resultFromCache, Page.class);
                Toast.makeText(WebViewActivity.this, getString(R.string.error_load_data), Toast.LENGTH_SHORT).show();
                setPageContent();
            }
        });
    }

    private void setPageContent() {
        if(page!=null){
            pageTitle.setText(Html.fromHtml(page.getTitle()));
            String font = "fontFr";
            String color = "#000000";
            String bgColor = "#ffffff";
            String dir= "ltr";
            if(AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false)) {
                color = "#ffffff";
                bgColor = "#1e1e23";
            }
            if(Utils.getAppCurrentLang().equals("ar")) {
                font = "fontAr";
                dir = "rtl";
            }
            String text = Utils.loadJSONFromAsset("index.html", this);
            text = text.replace("{{content}}", page.getBody());
            text = text.replace("{{myFont}}", font);
            text = text.replace("{{color}}", color);
            text = text.replace("{{bgColor}}", bgColor);
            text = text.replace("{{direction}}", dir);
            webView.loadDataWithBaseURL("file:///android_asset/", text, "text/html", "utf-8", null);
        }
        else{
            webView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.back_btn:
                finish();
                break;
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(MyContextWrapper.wrap(newBase, Utils.getAppCurrentLang()));
    }
}
