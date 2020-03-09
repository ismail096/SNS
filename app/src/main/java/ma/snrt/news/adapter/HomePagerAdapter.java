package ma.snrt.news.adapter;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import ma.snrt.news.fragment.FavFragment;
import ma.snrt.news.fragment.FlashFragment;
import ma.snrt.news.fragment.HomeFragment;
import ma.snrt.news.fragment.VideosFragment;

public class HomePagerAdapter extends FragmentPagerAdapter {

    public HomePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    // This determines the fragment for each tab
    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position){
            case 0:
                fragment = new HomeFragment();
                break;
            case 1:
                fragment = new FlashFragment();
                break;
            case 2:
                fragment = new VideosFragment();
                break;
            case 3:
                fragment = new FavFragment();
                break;
        }
        return fragment;
    }

    // This determines the number of tabs
    @Override
    public int getCount() {
        return 4;
    }

    // This determines the title for each tab
    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return "";
    }
}
