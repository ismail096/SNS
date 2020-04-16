package ma.snrt.news.adapter;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

import ma.snrt.news.AppController;
import ma.snrt.news.fragment.CategoryFragment;
import ma.snrt.news.fragment.LifeFragment;
import ma.snrt.news.fragment.SportFragment;
import ma.snrt.news.fragment.TechFragment;
import ma.snrt.news.fragment.TopAgendaFragment;
import ma.snrt.news.fragment.TopNewsFragment;
import ma.snrt.news.fragment.TrueFakeFragment;
import ma.snrt.news.model.Category;

public class CategoryPagerAdapter extends FragmentPagerAdapter {
    private List<Category> categories;

    public CategoryPagerAdapter(List<Category> categories, FragmentManager fm) {
        super(fm);
        this.categories = categories;
    }

    // This determines the fragment for each tab
    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        if(position==0)
            fragment = new TopNewsFragment();
        /*else if(position==categories.size()-1)
            fragment = new Fragment();*/
        else if(categories.get(position).getId() == 10 || categories.get(position).getId() == 155) {
            fragment = new TrueFakeFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("category", categories.get(position));
            fragment.setArguments(bundle);
        }
        else if(categories.get(position).getId() == 184 || categories.get(position).getId() == 210) {
            fragment = new TechFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("category", categories.get(position));
            fragment.setArguments(bundle);
        }
        else if(categories.get(position).getId() == 8 || categories.get(position).getId() == 153) {
            fragment = new LifeFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("category", categories.get(position));
            fragment.setArguments(bundle);
        }
        else if(categories.get(position).getId() == 7 || categories.get(position).getId() == 152) {
            fragment = new SportFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("category", categories.get(position));
            fragment.setArguments(bundle);
        }
        else if(categories.get(position).getId() == AppController.AGENDA_FR_ID || categories.get(position).getId() == AppController.AGENDA_AR_ID) {
            fragment = new TopAgendaFragment();
        }
        else{
            fragment = new CategoryFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("category", categories.get(position));
            fragment.setArguments(bundle);
        }

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
