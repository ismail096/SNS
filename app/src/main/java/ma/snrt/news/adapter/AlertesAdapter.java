package ma.snrt.news.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.messaging.FirebaseMessaging;
import com.suke.widget.SwitchButton;

import java.util.ArrayList;

import ma.snrt.news.AppController;
import ma.snrt.news.R;
import ma.snrt.news.model.Category;
import ma.snrt.news.ui.TextViewExtraBold;


public class AlertesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    ArrayList<Category> items;
    Context context;

    public AlertesAdapter(Context context, ArrayList<Category> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final Category item = items.get(position);
        final ViewHolder mHolder = (ViewHolder) holder;
        mHolder.title.setText(Html.fromHtml(item.getTitle()));
        mHolder.title.setClickable(false);
        if(position<items.size()-1)
            mHolder.divider.setVisibility(View.VISIBLE);
        else
            mHolder.divider.setVisibility(View.GONE);
        if(AppController.getSharedPreferences().contains("notif_enabled")) {
            if (AppController.getSharedPreferences().getBoolean("notif_enabled", true)) {
                if(AppController.getSharedPreferences().getBoolean("notif_" + item.getId(), false)) {
                    mHolder.switchButton.setEnabled(true);
                    mHolder.switchButton.setChecked(true);
                }
                else {
                    mHolder.switchButton.setEnabled(true);
                    mHolder.switchButton.setChecked(false);
                }
            } else {
                mHolder.switchButton.setChecked(false);
                mHolder.switchButton.setEnabled(false);
            }
        }
        else{
            FirebaseMessaging.getInstance().subscribeToTopic("snrtnews_" + item.getId());
            mHolder.switchButton.setChecked(true);
            SharedPreferences.Editor editor = AppController.getSharedPreferences().edit();
            editor.putBoolean("notif_" + item.getId(), true);
            editor.commit();
        }
        mHolder.switchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (isChecked)
                    FirebaseMessaging.getInstance().subscribeToTopic("snrtnews_" + item.getId());
                else
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("snrtnews_" + item.getId());
                SharedPreferences.Editor editor = AppController.getSharedPreferences().edit();
                editor.putBoolean("notif_" + item.getId(), isChecked);
                editor.commit();
            }
        });
        if(item.getColor()!=null && item.getColor().length()==7)
         mHolder.categoryBg.getBackground().setColorFilter(Color.parseColor(item.getColor()), PorterDuff.Mode.SRC_IN);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup,
                                                      int viewType) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.alerte_item_layout, viewGroup, false);
            return new ViewHolder(v);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextViewExtraBold title;
        SwitchButton switchButton;
        View divider, categoryBg;

        public ViewHolder(View convertView) {
            super(convertView);
            title = convertView.findViewById(R.id.alerte_title);
            switchButton = convertView.findViewById(R.id.alerte_switch);
            divider = convertView.findViewById(R.id.alerte_divider);
            categoryBg = convertView.findViewById(R.id.category_bg);
        }
    }
}