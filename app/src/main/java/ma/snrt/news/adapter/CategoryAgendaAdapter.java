package ma.snrt.news.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ma.snrt.news.R;
import ma.snrt.news.model.CategoryAgenda;
import ma.snrt.news.ui.TextViewExtraBold;
import ma.snrt.news.util.Utils;

public class CategoryAgendaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    ArrayList<CategoryAgenda> items;
    Context context;
    private final OnItemClickListener listener;


    public CategoryAgendaAdapter(Context context, ArrayList<CategoryAgenda> items, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.items = items;
        this.listener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int id);
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
        final CategoryAgenda item = items.get(position);
        final ViewHolder mHolder = (ViewHolder) holder;
        mHolder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(item.getId());
            }
        });

        mHolder.title.setText(Html.fromHtml(item.getTitle()));

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.placeholder);
        requestOptions.error(R.drawable.placeholder);
        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL);

        Glide.with(context)
                .load(item.getImage())
                .apply(requestOptions)
                .into(mHolder.image);

        if(item.isSelected()) {
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) mHolder.container.getLayoutParams();
            layoutParams.width = Utils.dpToPx(context.getResources(), 160);
            layoutParams.height = Utils.dpToPx(context.getResources(), 160);
            mHolder.container.setLayoutParams(layoutParams);
        }
        else{
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) mHolder.container.getLayoutParams();
            layoutParams.width = Utils.dpToPx(context.getResources(), 120);
            layoutParams.height = Utils.dpToPx(context.getResources(), 120);
            mHolder.container.setLayoutParams(layoutParams);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup,
                                                      int viewType) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.category_a_item_layout, viewGroup, false);
            return new ViewHolder(v);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextViewExtraBold title;
        CardView container;

        public ViewHolder(View convertView) {
            super(convertView);
            title = convertView.findViewById(R.id.category_title);
            container = convertView.findViewById(R.id.category_container);
            image = convertView.findViewById(R.id.category_image);
        }
    }
}