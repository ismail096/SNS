package ma.snrt.news.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ma.snrt.news.NewsActivity;
import ma.snrt.news.R;
import ma.snrt.news.model.Tag;
import ma.snrt.news.ui.TextViewExtraBold;

public class TagAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<Tag> items;
    Context context;

    public TagAdapter(Context context, ArrayList<Tag> items) {
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
        final Tag item = items.get(position);
        final ViewHolder mHolder = (ViewHolder) holder;
        mHolder.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, NewsActivity.class);
                //intent.putExtra("is_cat", false);
                intent.putExtra("tag", item.getName());
                context.startActivity(intent);
            }
        });

        mHolder.name.setText("#"+item.getName());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup,
                                                      int viewType) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.tag_item_layout, viewGroup, false);
            return new ViewHolder(v);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextViewExtraBold name;
        RelativeLayout container;

        public ViewHolder(View convertView) {
            super(convertView);
            name = convertView.findViewById(R.id.post_tag);
            container = convertView.findViewById(R.id.tag_container);
        }
    }
}