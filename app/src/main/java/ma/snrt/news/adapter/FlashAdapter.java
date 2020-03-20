package ma.snrt.news.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ma.snrt.news.AppController;
import ma.snrt.news.PostDetailActivity;
import ma.snrt.news.R;
import ma.snrt.news.model.Post;
import ma.snrt.news.ui.TextViewBold;
import ma.snrt.news.ui.TextViewExtraBold;
import ma.snrt.news.ui.TextViewEBItalic;
import ma.snrt.news.ui.TextViewRegular;
import ma.snrt.news.util.DateTimeUtils;

public class FlashAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<Post> items;
    Context context;
    private int visibleThreshold = 1;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;
    private int lastPosition = -1;

    public FlashAdapter(Context context, ArrayList<Post> items, RecyclerView recyclerView) {
        this.context = context;
        this.items = items;
        try {
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    totalItemCount = linearLayoutManager.getItemCount();
                    lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                    if (!loading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        if (onLoadMoreListener != null) {
                            onLoadMoreListener.onLoadMore();
                        }
                        loading = true;
                    }
                }
            });
        }catch(Exception ex){

        }
    }

    public void setLoaded() {
        notifyDataSetChanged();
        loading = false;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
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
        final Post item = items.get(position);
        final ViewHolder mHolder = (ViewHolder) holder;
        mHolder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("post", item);
                context.startActivity(intent);
            }
        });

        mHolder.title.setText(Html.fromHtml(item.getTitle()));
        if(item.getDatePublication()!=null) {
            mHolder.date.setText(DateTimeUtils.getHourFromDate(item.getDatePublication()));
        }
        mHolder.category.setText(Html.fromHtml(item.getCategory()));
        if(item.getColor()!=null && item.getColor().length()==7)
        mHolder.category.setTextColor(Color.parseColor(item.getColor()));
        if(item.getLive()==1) {
            mHolder.liveImg.setVisibility(View.VISIBLE);
            mHolder.title.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
        }
        else {
            mHolder.liveImg.setVisibility(View.GONE);
            if(AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
                mHolder.title.setTextColor(ContextCompat.getColor(context, R.color.text_color_dark));
            else
                mHolder.title.setTextColor(ContextCompat.getColor(context, R.color.text_color_primary));
        }
        setAnimation(mHolder.itemView, position);
    }

    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.anim_zoom);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup,
                                                      int viewType) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.flash_item_layout, viewGroup, false);
            return new ViewHolder(v);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextViewRegular title;
        TextViewExtraBold date;
        TextViewEBItalic category;
        LinearLayout container;
        ImageView liveImg;

        public ViewHolder(View convertView) {
            super(convertView);
            title = convertView.findViewById(R.id.post_title);
            container = convertView.findViewById(R.id.post_container);
            date = convertView.findViewById(R.id.post_date);
            category = convertView.findViewById(R.id.post_category);
            liveImg = convertView.findViewById(R.id.live_badge);
        }
    }
}