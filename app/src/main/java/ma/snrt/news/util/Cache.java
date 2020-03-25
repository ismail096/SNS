package ma.snrt.news.util;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import ma.snrt.news.model.Post;

public class Cache {

	private static final String CACHE_NAME = "SnrtNews" ;
	private static final String APP_NAME = "SnrtNews";
	private static final String FAV_NAME = "favoris_news_";
	private static final String FAV_VIDEO_NAME = "favoris_video_";
	private static final String FAV_AG_NAME = "favoris_agenda_";
	private static final String LIKE_NAME = "liked_news_";
	private static String _permanentDir;

	protected static String getPermanentDir() { return _permanentDir;}
	protected static String getPermanentFile() {return getPermanentDir()+"/"+APP_NAME;}
	protected static String getPermanentFileForKey(String key) {return getPermanentDir()+"/"+APP_NAME+"."+key;}

	private static HashMap<String, Post> f_posts;
	private static HashMap<String, Post> f_videos;
	private static HashMap<String, Post> f_agendas;
	private static ArrayList<String> liked_posts;

	public static void initCache(Context c){
		File f = null;
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			if (android.os.Build.VERSION.SDK_INT < 8) {
				f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/"+APP_NAME);
				f.mkdirs();
			} else f = c.getExternalCacheDir();
		} else f = c.getCacheDir();
		if (f == null) {
			f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/"+APP_NAME);
			f.mkdirs();
		}
		_permanentDir = f.getAbsolutePath()+"/"+CACHE_NAME;
		try {
			File permanent = new File(getPermanentDir());
			if (!permanent.isDirectory()) permanent.delete();
			if (!permanent.exists()) permanent.mkdirs();
		} catch (Exception e) {
			//Log.e("Cache","Cache",e);
		}
	}

	public static void initFavoris(){
		f_posts = (HashMap<String, Post>) Cache.getPermanentObject(FAV_NAME+Utils.getAppCurrentLang());
		if(f_posts == null){
			Cache.putPermanentObject(new HashMap<String, Post>(), FAV_NAME+Utils.getAppCurrentLang());
			f_posts = new HashMap<String, Post>();
		}
	}

	public static void initFavorisVideos(){
		f_videos = (HashMap<String, Post>) Cache.getPermanentObject(FAV_VIDEO_NAME+Utils.getAppCurrentLang());
		if(f_videos == null){
			Cache.putPermanentObject(new HashMap<String, Post>(), FAV_VIDEO_NAME+Utils.getAppCurrentLang());
			f_videos = new HashMap<String, Post>();
		}
	}

	public static void initFavorisAgenda(){
		f_agendas = (HashMap<String, Post>) Cache.getPermanentObject(FAV_AG_NAME+Utils.getAppCurrentLang());
		if(f_agendas == null){
			Cache.putPermanentObject(new HashMap<String, Post>(), FAV_AG_NAME+Utils.getAppCurrentLang());
			f_agendas = new HashMap<String, Post>();
		}
	}

	public static void initLikedPosts(){
		liked_posts = (ArrayList<String>) Cache.getPermanentObject(LIKE_NAME+Utils.getAppCurrentLang());
		if(liked_posts == null){
			Cache.putPermanentObject(new ArrayList<>(), LIKE_NAME+Utils.getAppCurrentLang());
			liked_posts = new ArrayList<>();
		}
	}

	public static HashMap<String, Post> getFavoris(){
		if(f_posts != null)
			return f_posts;
		else
			return null;
	}

	public static HashMap<String, Post> getFavVideos(){
		if(f_videos != null)
			return f_videos;
		else
			return null;
	}

	public static HashMap<String, Post> getFavAgenda(){
		if(f_agendas != null)
			return f_agendas;
		else
			return null;
	}

	public static void putPost(String id, Post post) {
		if(!f_posts.containsKey(id)){
			f_posts.put(id, post);
			Cache.putPermanentObject(f_posts, FAV_NAME+Utils.getAppCurrentLang());
		}
	}

	public static void removePost(String id) {
		if(f_posts.containsKey(id)){
			f_posts.remove(id);
			Cache.putPermanentObject(f_posts, FAV_NAME+Utils.getAppCurrentLang());
		}
	}

	public static void putVidToFav(String id, Post post) {
		if(!f_videos.containsKey(id)){
			f_videos.put(id, post);
			Cache.putPermanentObject(f_videos, FAV_VIDEO_NAME+Utils.getAppCurrentLang());
		}
	}

	public static void removeVidFromFav(String id) {
		if(f_videos.containsKey(id)){
			f_videos.remove(id);
			Cache.putPermanentObject(f_videos, FAV_VIDEO_NAME+Utils.getAppCurrentLang());
		}
	}

	public static void putAgendaToFav(String id, Post post) {
		if(!f_agendas.containsKey(id)){
			f_agendas.put(id, post);
			Cache.putPermanentObject(f_agendas, FAV_AG_NAME+Utils.getAppCurrentLang());
		}
	}

	public static void removeAgendaFromFav(String id) {
		if(f_agendas.containsKey(id)){
			f_agendas.remove(id);
			Cache.putPermanentObject(f_agendas, FAV_AG_NAME+Utils.getAppCurrentLang());
		}
	}

	public static boolean existsInFavoris(String id) {
		if(f_posts.containsKey(id)){
			return true;
		}
		return false;
	}

	public static boolean existsInVidFav(String id) {
		if(f_videos.containsKey(id)){
			return true;
		}
		return false;
	}

	public static boolean existsInAgendaFav(String id) {
		if(f_agendas.containsKey(id)){
			return true;
		}
		return false;
	}

	public static void likePost(String id) {

		if(!liked_posts.contains(id)){
			liked_posts.add(id);
			Cache.putPermanentObject(liked_posts, LIKE_NAME+Utils.getAppCurrentLang());
		}
	}

	public static void unLikePost(String id) {
		if(liked_posts.contains(id)){
			liked_posts.remove(id);
			Cache.putPermanentObject(liked_posts, LIKE_NAME+Utils.getAppCurrentLang());
		}
	}

	public static boolean existsInLikes(String id) {
		if(liked_posts.contains(id)){
			return true;
		}
		return false;
	}

	public static Object getPermanentObject(String key) {
		try {
			String file = getPermanentFileForKey(key);
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
			Object object = in.readObject();
			in.close();
			//Log.i("Cache","Permanent object ["+file+"] retrieved for key ["+key+"]");
			return object;

		} catch (Exception e) {
			//Log.e("Cache","getPermanentObject("+key+")",e);
		}
		return null;
	}

	public static void putPermanentObject(Serializable object, String key) {
		try {
			removePermanentForKey(key);
			String file = getPermanentFileForKey(key);
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
			out.writeObject(object);
			out.close();
			//Log.i("Cache","Permanent object ["+file+"] cached for key ["+key+"]");
		} catch (Exception e) {
			//Log.e("Cache","putPermanentObject("+key+")",e);
		}
	}


	public static void removePermanentForKey(String key) {
		try {
			File ff = new File(getPermanentFileForKey(key));
			if (ff.exists()) ff.delete();
			//Log.i("Cache","Permanent object removed for key ["+key+"]");
		} catch (Exception e) {
			//Log.e("Cache","removePermanentForKey("+key+")",e);
		}
	}
}
