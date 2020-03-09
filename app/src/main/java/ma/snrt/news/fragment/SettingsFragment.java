package ma.snrt.news.fragment;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import ma.snrt.news.AlertesActivity;
import ma.snrt.news.AppController;
import ma.snrt.news.MainActivity;
import ma.snrt.news.R;
import ma.snrt.news.model.Category;
import ma.snrt.news.network.GsonHelper;
import ma.snrt.news.ui.TextViewExtraBold;
import ma.snrt.news.util.Cache;
import ma.snrt.news.util.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {
    TextViewExtraBold langFr, langAr;
    ImageView normalMode, darkMode, closeBtn;
    Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        langFr = rootView.findViewById(R.id.lang_fr_btn);
        langAr = rootView.findViewById(R.id.lang_ar_btn);
        normalMode = rootView.findViewById(R.id.normal_mode_btn);
        darkMode = rootView.findViewById(R.id.dark_mode_btn);
        closeBtn = rootView.findViewById(R.id.close_btn);

        mContext = getActivity();

        if(AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false)) {
            normalMode.setBackground(null);
            darkMode.setImageResource(R.drawable.lune_dark_white);
            darkMode.setBackgroundResource(R.drawable.oval_red);
            closeBtn.setImageResource(R.drawable.close_menu_dark);
        }
        else {
            normalMode.setBackgroundResource(R.drawable.oval_red);
            darkMode.setImageResource(R.drawable.lune_inactive);
            darkMode.setBackground(null);
            closeBtn.setImageResource(R.drawable.close_menu);
        }

        if(Utils.getAppCurrentLang().equals("ar"))
        {
            langAr.setBackgroundResource(R.drawable.oval_red);
            langAr.setTextColor(Color.parseColor("#ffffff"));
            langFr.setBackground(null);
            if(!AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
                langFr.setTextColor(Color.parseColor("#000000"));
            else
                langFr.setTextColor(Color.parseColor("#ffffff"));
        }
        else{
            langFr.setBackgroundResource(R.drawable.oval_red);
            langFr.setTextColor(Color.parseColor("#ffffff"));
            langAr.setBackground(null);
            if(!AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
                langAr.setTextColor(Color.parseColor("#000000"));
            else
                langAr.setTextColor(Color.parseColor("#ffffff"));
        }
        langFr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLang("fr");
            }
        });
        langAr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLang("ar");
            }
        });

        View.OnClickListener onClickListener= new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent=null;
                switch (v.getId()){
                    case R.id.favoris_layout:
                        if(mContext!=null){
                            ((MainActivity) mContext).viewPager.setCurrentItem(3);
                            ((MainActivity) mContext).drawerLayout.closeDrawer(GravityCompat.START);
                        }
                        break;
                    case R.id.apropos_layout:
                        /*intent = new Intent(mContext, WebViewActivity.class);
                        intent.putExtra("pageId", 16);*/
                        break;
                    case R.id.alertes_layout:
                        intent = new Intent(mContext, AlertesActivity.class);
                        break;
                    case R.id.note_layout:
                        Utils.openAppForRating(mContext);
                        break;
                    case R.id.normal_mode_btn:
                        toggleDarkMode(false);
                        break;
                    case R.id.dark_mode_btn:
                        toggleDarkMode(true);
                        break;
                    case R.id.close_btn:
                        try {
                            ((MainActivity) mContext).drawerLayout.closeDrawer(GravityCompat.START);
                        }catch(Exception e){}
                        break;
                }
                if(intent!=null)
                    startActivity(intent);
            }
        };
        rootView.findViewById(R.id.favoris_layout).setOnClickListener(onClickListener);
        rootView.findViewById(R.id.apropos_layout).setOnClickListener(onClickListener);
        rootView.findViewById(R.id.note_layout).setOnClickListener(onClickListener);
        rootView.findViewById(R.id.alertes_layout).setOnClickListener(onClickListener);
        normalMode.setOnClickListener(onClickListener);
        darkMode.setOnClickListener(onClickListener);
        closeBtn.setOnClickListener(onClickListener);

        return rootView;
    }

    private void toggleDarkMode(boolean activate) {
        SharedPreferences.Editor editor = AppController.getSharedPreferences().edit();
        editor.putBoolean("NIGHT_MODE", activate);
        editor.commit();
        restartMainActivity();
    }

    private void restartMainActivity(){
        if(getActivity()!=null) {
            //((MainActivity) getActivity()).selectHome();
            getActivity().finish();
            startActivity(new Intent(mContext, MainActivity.class));
        }
    }

    private void setLang(String lang){
        unsubsriceFromTopics();
        SharedPreferences.Editor editor = AppController.getSharedPreferences().edit();
        editor.putString("lang", lang);
        editor.remove("notif_enabled");
        editor.commit();
        restartMainActivity();
    }

    private void unsubsriceFromTopics(){
        String resultFromCache = (String) Cache.getPermanentObject("categories_"+ Utils.getAppCurrentLang());
        ArrayList<Category> categories = new ArrayList<>();
        if(resultFromCache!=null)
            categories = GsonHelper.getGson().fromJson(resultFromCache, new TypeToken<List<Category>>(){}.getType());
        FirebaseMessaging.getInstance().unsubscribeFromTopic("snrtnews_live_"+ Utils.getAppCurrentLang());
        FirebaseMessaging.getInstance().unsubscribeFromTopic("snrtnews_story_"+ Utils.getAppCurrentLang());
        if(categories.size()>0){
            for(int i=0;i<categories.size();i++) {
                FirebaseMessaging.getInstance().unsubscribeFromTopic("snrtnews_" + categories.get(i).getId());
            }
        }
    }

}
