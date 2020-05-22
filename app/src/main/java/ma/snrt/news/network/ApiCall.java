package ma.snrt.news.network;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ma.snrt.news.AppController;
import ma.snrt.news.util.Utils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

public class ApiCall {


    public static void getCategories(Callback<JsonArray> callback) {
        Call<JsonArray> call = AppController.getAPIService().getCategories(Utils.getAppCurrentLang());
        call.enqueue(callback);
    }

    public static void getFeaturedNews(int page, int count, Callback<JsonArray> callback) {
        Call<JsonArray> call = AppController.getAPIService().getFeaturedNews(Utils.getAppCurrentLang(), page, count);
        call.enqueue(callback);
    }

    public static void getDetailNews(int id, Callback<JsonArray> callback) {
        Call<JsonArray> call = AppController.getAPIService().getDetailNews(Utils.getAppCurrentLang(), id);
        call.enqueue(callback);
    }

    public static void getRelatedNews(int id, Callback<JsonArray> callback) {
        Call<JsonArray> call = AppController.getAPIService().getRelatedNews(id, Utils.getAppCurrentLang());
        //Call<JsonArray> call = AppController.getAPIService().getLatestNews(Utils.getAppCurrentLang(), 0 , 3);
        call.enqueue(callback);
    }

    public static void likePost(boolean like, int id, String type, Callback<JsonObject> callback) {
        Call<JsonObject> call = AppController.getAPIService().postLike( id, type);
        if(!like)
            call = AppController.getAPIService().postDislike( id, type);
        call.enqueue(callback);
    }

    public static void getLatestNews(int page, Callback<JsonArray> callback) {
        Call<JsonArray> call = AppController.getAPIService().getLatestNews(Utils.getAppCurrentLang(), page, 20);
        call.enqueue(callback);
    }

    public static void getLatestNews(int page, int count, Callback<JsonArray> callback) {
        Call<JsonArray> call = AppController.getAPIService().getLatestNews(Utils.getAppCurrentLang(), page, count);
        call.enqueue(callback);
    }

    public static void getMosaic(int page, Callback<JsonObject> callback) {
        Call<JsonObject> call = AppController.getAPIService().getMosaic(Utils.getAppCurrentLang(), page, 20);
        call.enqueue(callback);
    }
    public static void getNewsByCatOrTag(boolean isCat, int categoryId, String term, int page, Callback<JsonArray> callback) {
        Call<JsonArray> call = AppController.getAPIService().getNewsByTag(Utils.getAppCurrentLang(), term, page, 20);
        if(isCat)
            call = AppController.getAPIService().getNewsByCat(Utils.getAppCurrentLang(), categoryId,  page, 20);
        call.enqueue(callback);
    }

    public static void getTags(int catId, Callback<JsonArray> callback) {
        Call<JsonArray> call = AppController.getAPIService().getTags(catId, Utils.getAppCurrentLang());
        call.enqueue(callback);
    }

    public static void getVideos(boolean isLatest, int page, int count, Callback<JsonArray> callback) {
        Call<JsonArray> call = AppController.getAPIService().getPopularVideos(Utils.getAppCurrentLang(), page, count);
        if(isLatest)
            call = AppController.getAPIService().getLatestVideos(Utils.getAppCurrentLang(), page, count);
        call.enqueue(callback);
    }

    public static void getImages(boolean isLatest, int page, int count, Callback<JsonArray> callback) {
        Call<JsonArray> call = AppController.getAPIService().getPopularImages(Utils.getAppCurrentLang(), page, count);
        if(isLatest)
            call = AppController.getAPIService().getLatestImages(Utils.getAppCurrentLang(), page, count);
        call.enqueue(callback);
    }

    public static void getVideosByCat(int catId, boolean isLatest, int page, Callback<JsonArray> callback) {
        if(catId != 0) {
            Call<JsonArray> call = AppController.getAPIService().getPopularVideosByCat(Utils.getAppCurrentLang(), catId, page, 20);
            if (isLatest)
                call = AppController.getAPIService().getLatestVideosByCat(Utils.getAppCurrentLang(), catId, page, 20);
            call.enqueue(callback);
        }
        else{
            Call<JsonArray> call = AppController.getAPIService().getPopularVideos(Utils.getAppCurrentLang(), page, 20);
            if(isLatest)
                call = AppController.getAPIService().getLatestVideos(Utils.getAppCurrentLang(), page, 20);
            call.enqueue(callback);
        }
    }

    public static void getImagesByCat(int catId, boolean isLatest, int page, Callback<JsonArray> callback) {
        if(catId != 0) {
            Call<JsonArray> call = AppController.getAPIService().getPopularImagesByCat(Utils.getAppCurrentLang(), catId, page, 20);
            if(isLatest)
                call = AppController.getAPIService().getLatestImagesByCat(Utils.getAppCurrentLang(), catId, page, 20);
            call.enqueue(callback);
        }
        else{
            Call<JsonArray> call = AppController.getAPIService().getPopularImages(Utils.getAppCurrentLang(), page, 20);
            if(isLatest)
                call = AppController.getAPIService().getLatestImages(Utils.getAppCurrentLang(), page, 20);
            call.enqueue(callback);
        }
    }

    public static void searchContent(int mode, String keyword, int page, Callback<JsonArray> callback) {
        Call<JsonArray> call = AppController.getAPIService().searchAll(Utils.getAppCurrentLang(), keyword, page, 20);
        if(mode == 1)
            call = AppController.getAPIService().searchNews(Utils.getAppCurrentLang(), keyword, page, 20);
        else if(mode == 2)
            call = AppController.getAPIService().searchVideos(Utils.getAppCurrentLang(), keyword, page, 20);
        call.enqueue(callback);
    }

    public static void searchNewsByCat(int catId, String keyword, int page, Callback<JsonArray> callback) {
        Call<JsonArray> call = AppController.getAPIService().searchNewsByCat(Utils.getAppCurrentLang(), catId, keyword, page, 20);
        call.enqueue(callback);
    }

    public static void getStories(Callback<JsonArray> callback) {
        Call<JsonArray> call = AppController.getAPIService().getStories(Utils.getAppCurrentLang());
        call.enqueue(callback);
    }

    public static void getCategoriesAgenda(Callback<JsonArray> callback) {
        Call<JsonArray> call = AppController.getAPIService().getCategoriesAgenda(Utils.getAppCurrentLang());
        call.enqueue(callback);
    }

    public static void getAgendaByCat(int categoryId, String position, String date1, String date2, int page, Callback<JsonArray> callback) {
        Call<JsonArray> call = AppController.getAPIService().getAgendaByCat(Utils.getAppCurrentLang(), categoryId, position, date1, date2, page, 20);
        if(categoryId == 0)
            call = AppController.getAPIService().getAgendaWithoutCat(Utils.getAppCurrentLang(), position, date1, date2, page, 20);
        call.enqueue(callback);
    }

    public static void getFeaturedAgenda(int categoryId, Callback<JsonArray> callback) {
        Call<JsonArray> call = AppController.getAPIService().getAgendaFeatured(Utils.getAppCurrentLang(), categoryId);
        if(categoryId == 0)
            call = AppController.getAPIService().getAllAgendaFeatured(Utils.getAppCurrentLang());
        call.enqueue(callback);
    }

    public static void getLatestAgenda(Callback<JsonArray> callback) {
        Call<JsonArray> call = AppController.getAPIService().getAgendaLatest(Utils.getAppCurrentLang(), 0,  2);
        call.enqueue(callback);
    }

    public static void likePostAgenda(boolean like, int id, Callback<JsonObject> callback) {
        Call<JsonObject> call = AppController.getAPIService().postLike77(id);
        if(!like)
            call = AppController.getAPIService().postDislike77(id);
        call.enqueue(callback);
    }

    public static void getStaticPage(int id, Callback<JsonArray> callback) {
        Call<JsonArray> call = AppController.getAPIService().getStaticPage(id, Utils.getAppCurrentLang());
        call.enqueue(callback);
    }

    public static void getLive(Callback<JsonArray> callback) {
        Call<JsonArray> call = AppController.getAPIService().getLive(Utils.getAppCurrentLang());
        call.enqueue(callback);
    }

    public static void getUrlBytes(String url, Callback<ResponseBody> callback) {
        Call<ResponseBody> call = AppController.getAPIService().getUrlBytes(url);
        call.enqueue(callback);
    }

    public static void getLiveMatches(Callback<JsonObject> callback) {
        Call<JsonObject> call = AppController.getAPIService().getUrlAsJsonObject(AppController.LIVE_MATCHES_URL+"?lang="+Utils.getAppCurrentLang());
        call.enqueue(callback);
    }

    public static void sendQuestion(String question, String email, String nom, Callback<JsonObject> callback) {
        Call<JsonObject> call = AppController.getAPIService().sendQuestion(Utils.getAppCurrentLang(), question, email, nom);
        call.enqueue(callback);
    }

}
