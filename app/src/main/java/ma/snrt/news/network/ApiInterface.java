package ma.snrt.news.network;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiInterface {

    @GET("categories")
    Call<JsonArray> getCategories(@Query("lang") String lang);

    @GET("articles/featured")
    Call<JsonArray> getFeaturedNews(@Query("lang") String lang, @Query("page") int page, @Query("items_per_page") int count);

    @GET("articles")
    Call<JsonArray> getDetailNews(@Query("lang") String lang, @Query("id") int id);

    @GET("related/article/{id}")
    Call<JsonArray> getRelatedNews(@Path("id") int id, @Query("lang") String lang);

    @POST("add/like/{type}/{id}")
    Call<JsonObject> postLike(@Path("id") int id, @Path("type") String type);

    @POST("add/dislike/{type}/{id}")
    Call<JsonObject> postDislike(@Path("id") int id, @Path("type") String type);

    @GET("articles/latest")
    Call<JsonArray> getLatestNews(@Query("lang") String lang, @Query("page") int page, @Query("items_per_page") int count);

    @GET("mosaic/{lang}/{count}")
    Call<JsonObject> getMosaic(@Path("lang") String lang, @Path("count") int count, @Query("page") int page);

    @GET("tags")
    Call<JsonArray> getTags(@Query("category_id") int catId, @Query("lang") String lang);

    @GET("articles/by_category")
    Call<JsonArray> getNewsByCat(@Query("lang") String lang, @Query("category_id") int categoryId, @Query("page") int page, @Query("items_per_page") int count);

    @GET("articles/by_tag")
    Call<JsonArray> getNewsByTag(@Query("lang") String lang, @Query("term") String term, @Query("page") int page, @Query("items_per_page") int count);

    @GET("videos")
    Call<JsonArray> getLatestVideos(@Query("lang") String lang, @Query("page") int page, @Query("items_per_page") int count);

    @GET("videos/most_read")
    Call<JsonArray> getPopularVideos(@Query("lang") String lang, @Query("page") int page, @Query("items_per_page") int count);

    @GET("images")
    Call<JsonArray> getLatestImages(@Query("lang") String lang, @Query("page") int page, @Query("items_per_page") int count);

    @GET("images/most_read")
    Call<JsonArray> getPopularImages(@Query("lang") String lang, @Query("page") int page, @Query("items_per_page") int count);

    @GET("videos/by_category")
    Call<JsonArray> getLatestVideosByCat(@Query("lang") String lang, @Query("category_id") int catId, @Query("page") int page, @Query("items_per_page") int count);

    @GET("videos/most_read")
    Call<JsonArray> getPopularVideosByCat(@Query("lang") String lang, @Query("category_id") int catId, @Query("page") int page, @Query("items_per_page") int count);

    @GET("images/by_category")
    Call<JsonArray> getLatestImagesByCat(@Query("lang") String lang, @Query("category_id") int catId, @Query("page") int page, @Query("items_per_page") int count);

    @GET("images/most_read")
    Call<JsonArray> getPopularImagesByCat(@Query("lang") String lang, @Query("category_id") int catId, @Query("page") int page, @Query("items_per_page") int count);

    @GET("articles")
    Call<JsonArray> searchNews(@Query("lang") String lang, @Query("keyword") String keyword, @Query("page") int page, @Query("items_per_page") int count);

    @GET("search")
    Call<JsonArray> searchAll(@Query("lang") String lang, @Query("keyword") String keyword, @Query("page") int page, @Query("items_per_page") int count);

    @GET("articles/by_category")
    Call<JsonArray> searchNewsByCat(@Query("lang") String lang, @Query("category_id") int catId, @Query("keyword") String keyword, @Query("page") int page, @Query("items_per_page") int count);

    @GET("videos")
    Call<JsonArray> searchVideos(@Query("lang") String lang, @Query("keyword") String keyword, @Query("page") int page, @Query("items_per_page") int count);

    @GET("stories/{lang}")
    Call<JsonArray> getStories(@Path("lang") String lang);//, @Query("page") int page, @Query("items_per_page") int count);

    @GET("7_7/categories")
    Call<JsonArray> getCategoriesAgenda(@Query("lang") String lang);

    @GET("7_7")
    Call<JsonArray> getAgendaByCat(@Query("lang") String lang, @Query("category") int categoryId, @Query("position") String position,
                                   @Query("created_start") String date1, @Query("created_end") String date2, @Query("page") int page, @Query("items_per_page") int count);

    @POST("like/7_7/{id}")
    Call<JsonObject> postLike77(@Path("id") int id);

    @POST("add/dislike/7_7/{id}")
    Call<JsonObject> postDislike77(@Path("id") int id);

    @GET("page")
    Call<JsonArray> getStaticPage(@Query("id") int id, @Query("lang") String lang);

    @GET("articles/live")
    Call<JsonArray> getLive(@Query("lang") String lang);
}
