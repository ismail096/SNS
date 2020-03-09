package ma.snrt.news.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ma.snrt.news.R;
import ma.snrt.news.model.CategoryAgenda;
import ma.snrt.news.ui.TextViewExtraBold;
import ma.snrt.news.util.Utils;

public class FiltreAgendaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    ArrayList<CategoryAgenda> items;
    Context context;
    private final OnItemClickListener listener;


    public FiltreAgendaAdapter(Context context, ArrayList<CategoryAgenda> items, OnItemClickListener onItemClickListener) {
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
        try {
            Picasso.with(context)
                    .load(item.getImage())
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .error(R.drawable.placeholder)
                    .into(mHolder.image, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(context)
                                    .load(item.getImage())
                                    .error(R.drawable.placeholder)
                                    .into(mHolder.image);
                        }
                    });
        } catch (Exception ex) {
            mHolder.image.setImageResource(R.drawable.placeholder);
        }
        if(item.isSelected()) {
            mHolder.checkLayout.setVisibility(View.VISIBLE);
        }
        else{
            mHolder.checkLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup,
                                                      int viewType) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.filtre_a_item_layout, viewGroup, false);
            v.getLayoutParams().width = (viewGroup.getMeasuredWidth() / 3) - Utils.dpToPx(context.getResources(), 10);
            v.getLayoutParams().height = (viewGroup.getMeasuredWidth() / 3);
            return new ViewHolder(v);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextViewExtraBold title;
        CardView container;
        RelativeLayout checkLayout;

        public ViewHolder(View convertView) {
            super(convertView);
            title = convertView.findViewById(R.id.category_title);
            container = convertView.findViewById(R.id.category_container);
            image = convertView.findViewById(R.id.category_image);
            checkLayout = convertView.findViewById(R.id.check_layout);
        }
    }
}