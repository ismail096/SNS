package ma.snrt.news.adapter;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

import ma.snrt.news.fragment.CategoryAgendaFragment;
import ma.snrt.news.fragment.CategoryFragment;
import ma.snrt.news.fragment.TopAgendaFragment;
import ma.snrt.news.fragment.TopNewsFragment;
import ma.snrt.news.model.Category;
import ma.snrt.news.model.CategoryAgenda;

public class AgendaPagerAdapter extends FragmentPagerAdapter {
    private List<CategoryAgenda> categories;

    public AgendaPagerAdapter(List<CategoryAgenda> categories, FragmentManager fm) {
        super(fm);
        this.categories = categories;
    }

    // This determines the fragment for each tab
    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        if(position!=0) {
            fragment = new CategoryAgendaFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("category", categories.get(position));
            //bundle.putInt("position", position);
            fragment.setArguments(bundle);
        }
        else
            fragment = new TopAgendaFragment();
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

    @Override
    public int getItemPosition(Object object) {
        if(object instanceof CategoryAgendaFragment){
            CategoryAgendaFragment f = (CategoryAgendaFragment ) object;
            if (f != null) {
                f.update();
            }
        }
        return super.getItemPosition(object);
    }
}
