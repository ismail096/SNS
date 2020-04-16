package ma.snrt.news;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import ma.snrt.news.util.MyContextWrapper;
import ma.snrt.news.util.Utils;

public class TutorialActivity extends AppCompatActivity {
    ViewPager viewPager;
    int imagesFr[] =  {R.drawable.tuto1, R.drawable.tuto2, R.drawable.tuto3, R.drawable.tuto4, R.drawable.tuto4};
    //int imagesAr[] =  {R.drawable.slider_1, R.drawable.slider_1, R.drawable.slider_1, R.drawable.slider_1};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        viewPager = findViewById(R.id.viewpager);
        //if(Utils.getAppCurrentLang().equals("fr"))
            viewPager.setAdapter(new TutorialPagerAdapter(imagesFr));
        //else
            //viewPager.setAdapter(new TutorialPagerAdapter(imagesAr));

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                if(i == imagesFr.length - 1)
                    setTutorialShowed();
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    private void setTutorialShowed() {
        SharedPreferences.Editor editor = AppController.getSharedPreferences().edit();
        editor.putBoolean("isTutorialShowed", true);
        editor.commit();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void onClick(View view){
        /*switch (view.getId()){
            case R.id.ignore_btn:
                setTutorialShowed();
                finish();
                break;
        }*/
    }

    class TutorialPagerAdapter extends PagerAdapter {

        LayoutInflater mLayoutInflater;
        int tutos[];

        public TutorialPagerAdapter(int tutos[]) {
            mLayoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.tutos = tutos;
        }

        @Override
        public int getCount() {
            return tutos.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View itemView = mLayoutInflater.inflate(R.layout.tutorial_slider_item, container, false);
            final ImageView image = itemView.findViewById(R.id.tutorial_image);
            if(tutos[position]!=0 && position < tutos.length-1)
                image.setImageResource(tutos[position]);
            container.addView(itemView);
            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((RelativeLayout) object);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(MyContextWrapper.wrap(newBase, Utils.getAppCurrentLang()));
    }
}
