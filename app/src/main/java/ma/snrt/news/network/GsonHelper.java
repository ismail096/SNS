package ma.snrt.news.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Modifier;

/**
 * Created by dell on 7/21/2016.
 */
public class GsonHelper {

    private static Gson gson;
    public  static Gson getGson(){
        if(gson == null)
            gson =new GsonBuilder()
                //.serializeNulls()
                //.excludeFieldsWithoutExposeAnnotation()
                    .setLenient()
                .excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC).create();
        return gson;
    }
}
