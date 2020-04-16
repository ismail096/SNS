package ma.snrt.news;

import android.content.Context;
import android.net.http.SslError;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
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
    ImageView progressBar;
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

        if(AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
            Glide.with(this).load(R.raw.loader_dark).into(progressBar);
        else
            Glide.with(this).load(R.raw.loader).into(progressBar);

        int pageId =  getIntent().getIntExtra("pageId", 0);
        if(pageId != 0) {
            if(Utils.getAppCurrentLang().equals("ar"))
                webView.getSettings().setDefaultFontSize(18);
            else
                webView.getSettings().setDefaultFontSize(15);
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) webView.getLayoutParams();
            int marg = Utils.dpToPx(getResources(), 10);
            lp.setMargins(marg, 0, marg, 0);
            webView.setLayoutParams(lp);
            loadPage(pageId);
        }
        else {
            progressBar.setVisibility(View.VISIBLE);
            webView.setBackground(null);
            webView.getSettings().setSupportZoom(false);
            webView.getSettings().setDomStorageEnabled(true);
            webView.setWebViewClient(new WebViewClient(){

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError er) {
                    handler.proceed();
                }

            });
            webView.loadUrl("http://botola.snrt.ma/resultats");
        }
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
