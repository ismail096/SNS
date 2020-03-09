package ma.snrt.news.adapter;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

import ma.snrt.news.fragment.CategoryFragment;
import ma.snrt.news.fragment.TopNewsFragment;
import ma.snrt.news.fragment.VideoCatFragment;
import ma.snrt.news.model.Category;

public class VideosPagerAdapter extends FragmentPagerAdapter {
    private List<Category> categories;

    public VideosPagerAdapter(List<Category> categories, FragmentManager fm) {
        super(fm);
        this.categories = categories;
    }

    // This determines the fragment for each tab
    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        fragment = new VideoCatFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("category", categories.get(position));
        //bundle.putInt("position", position);
        fragment.setArguments(bundle);
        return fragment;
    }

    // This determines the number of tabs
    @Override
    public int getCount() {
        return categories.size();
    }

    // This determines the title for each tab
    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return categories.get(position).getTitle();
    }
}
