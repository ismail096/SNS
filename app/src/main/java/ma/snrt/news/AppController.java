package ma.snrt.news;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.multidex.MultiDex;

import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import ma.snrt.news.network.ApiInterface;
import ma.snrt.news.network.GsonHelper;
import ma.snrt.news.util.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class AppController extends Application {

    private static Retrofit retrofit;
    private static ApiInterface service;
    public static final String BASE_URL = "https://snrtnews.aramobile.com";
    //public static final String BASE_IMG_URL = BASE_URL + "uploads/";
    private static Context context;
    public static FirebaseAnalytics mFirebaseAnalytics;
    public static OkHttpClient okHttpClient;
    public static String API_KEY = "AIzaSyBgD-Ser1WWDIOeF7IhsbpFjMmfdAVYSd0";
    public static int AGENDA_FR_ID = 2020;
    public static int AGENDA_AR_ID = 2021;

    public static ApiInterface getAPIService() {
        if(service == null)
            service = retrofit.create(ApiInterface.class);
        return service;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);

        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();
                        if(request.method().equalsIgnoreCase("post")) {
                            String url = request.url().toString().replace("https", "http");
                            Request finalRequest = request.newBuilder()
                                    .url(url)
                                    .tag(null)
                                    .build();
                            return chain.proceed(finalRequest);
                        }
                        return  chain.proceed(request);
                    }
                })
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .cache(null)
                .build();

        /*Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(false);
        built.setLoggingEnabled(false);
        Picasso.setSingletonInstance(built);*/

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL + "/api/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(GsonHelper.getGson()))
                .build();

        service = getAPIService();
        context = this;

        Cache.initCache(context);
    }


    public static SharedPreferences getSharedPreferences(){
        return context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
