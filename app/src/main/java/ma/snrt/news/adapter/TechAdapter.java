package ma.snrt.news.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import ma.snrt.news.AppController;
import ma.snrt.news.PostDetailActivity;
import ma.snrt.news.R;
import ma.snrt.news.model.Post;
import ma.snrt.news.ui.TextViewBold;
import ma.snrt.news.ui.TextViewEBItalic;
import ma.snrt.news.ui.TextViewRegular;
import ma.snrt.news.util.Cache;
import ma.snrt.news.util.Utils;

public class TechAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<Post> items;
    Context context;
    private int visibleThreshold = 1;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;
    private int lastPosition = -1;
    private static int TYPE_BIG = 1;
    private static int TYPE_NORMAL = 2;
    private static int TYPE_SMALL = 3;

    public TechAdapter(Context context, ArrayList<Post> items, RecyclerView recyclerView) {
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
        if(holder instanceof ViewHolder) {
            final ViewHolder mHolder = (ViewHolder) holder;
            mHolder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra("post", item);
                    Bundle bundle = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        bundle = ActivityOptions.makeSceneTransitionAnimation(
                                (Activity) context,
                                mHolder.imageView,
                                mHolder.imageView.getTransitionName())
                                .toBundle();
                    }
                    context.startActivity(intent, bundle);
                }
            });

            mHolder.title.setText(Html.fromHtml(item.getTitle()));

            if (item.getDatePublication() != null) {
                mHolder.date.setText(Utils.getPostRelativeDate(context, item.getDatePublication()));
            }
            mHolder.category.setText(Html.fromHtml(item.getCategory()));
            if (item.getColor() != null && item.getColor().length() == 7)
                mHolder.category.setTextColor(Color.parseColor(item.getColor()));
            else
                mHolder.category.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));

            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.placeholder);
            requestOptions.error(R.drawable.placeholder);
            requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL);

            Glide.with(context)
                    .load(item.getImage())
                    .apply(requestOptions)
                    .into(mHolder.imageView);

            mHolder.title.setClickable(false);

            if (!Cache.existsInFavoris(item.getId() + "")) {
                if (!AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
                    mHolder.favBtn.setImageResource(R.drawable.bookmarks_empty);
                else
                    mHolder.favBtn.setImageResource(R.drawable.bookmarks_dark);
            } else
                mHolder.favBtn.setImageResource(R.drawable.bookmarks);

            mHolder.shareBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name));
                        intent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_post) + " " + item.getUrl());
                        intent.setType("text/plain");
                        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)));
                    } catch (Exception e) {
                        Toast.makeText(context, context.getString(R.string.api_error), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            mHolder.favBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!Cache.existsInFavoris(item.getId() + "")) {
                        Cache.putPost(item.getId() + "", item);
                        mHolder.favBtn.setImageResource(R.drawable.bookmarks);
                    } else {
                        Cache.removePost(item.getId() + "");
                        if (AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
                            mHolder.favBtn.setImageResource(R.drawable.bookmarks_dark);
                        else
                            mHolder.favBtn.setImageResource(R.drawable.bookmarks_empty);
                    }
                }
            });
            if (getItemViewType(position) == TYPE_SMALL) {
                RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) mHolder.container.getLayoutParams();
                if (position == 1) {
                    lp.setMarginStart(Utils.dpToPx(context.getResources(), 10));
                    lp.setMarginEnd(Utils.dpToPx(context.getResources(), 5));
                } else {
                    lp.setMarginStart(Utils.dpToPx(context.getResources(), 5));
                    lp.setMarginEnd(Utils.dpToPx(context.getResources(), 10));
                }
                mHolder.container.setLayoutParams(lp);
            }

            if (position > 2 && position % 2 != 0) {
                if (!AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
                    mHolder.container.setBackgroundColor(ContextCompat.getColor(context, R.color.app_white));
                else
                    mHolder.container.setBackgroundColor(ContextCompat.getColor(context, R.color.app_black));
            } else {
                if (!AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
                    mHolder.container.setBackgroundColor(ContextCompat.getColor(context, R.color.bgGrey));
                else
                    mHolder.container.setBackgroundColor(ContextCompat.getColor(context, R.color.bgGreyDark));
            }
            mHolder.divider.setVisibility(View.GONE);
        }
        else{
            final BigViewHolder mHolder = (BigViewHolder) holder;
            mHolder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra("post", item);
                    Bundle bundle = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        bundle = ActivityOptions.makeSceneTransitionAnimation(
                                (Activity) context,
                                mHolder.imageView,
                                mHolder.imageView.getTransitionName())
                                .toBundle();
                    }
                    context.startActivity(intent, bundle);
                }
            });

            mHolder.title.setText(Html.fromHtml(item.getTitle()));

            if (item.getDatePublication() != null) {
                mHolder.date.setText(Utils.getPostRelativeDate(context, item.getDatePublication()));
            }
            if(item.getColor() != null && item.getColor().length() == 7)
                mHolder.date.setTextColor(Color.parseColor(item.getColor()));

            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.placeholder);
            requestOptions.error(R.drawable.placeholder);
            requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL);

            Glide.with(context)
                    .load(item.getImage())
                    .apply(requestOptions)
                    .into(mHolder.imageView);

            mHolder.title.setClickable(false);
        }
        //setAnimation(mHolder.itemView, position);
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
    public int getItemViewType(int position) {
        if(position==0)
            return TYPE_BIG;
        else if(position == 1 || position == 2)
            return TYPE_SMALL;
        /*if(context.getResources().getBoolean(R.bool.is_tablet) && (position==0 || position%5==0))
            return TYPE_BIG;*/
        return TYPE_NORMAL;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup,
                                                      int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.news_item_layout, viewGroup, false);
            if(viewType == TYPE_BIG) {
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.tech_big_layout, viewGroup, false);
                return new BigViewHolder(v);
            }
            else if(viewType == TYPE_SMALL)
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.tech_small_layout, viewGroup, false);
        return new ViewHolder(v);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextViewBold title;
        TextViewEBItalic category;
        TextViewRegular date;
        ImageView imageView, shareBtn, favBtn;
        View container, divider;

        public ViewHolder(View convertView) {
            super(convertView);
            title = convertView.findViewById(R.id.post_title);
            container = convertView.findViewById(R.id.post_container);
            date = convertView.findViewById(R.id.post_date);
            category = convertView.findViewById(R.id.post_category);
            imageView = convertView.findViewById(R.id.post_image);
            shareBtn = convertView.findViewById(R.id.share_btn);
            favBtn = convertView.findViewById(R.id.fav_btn);
            divider = convertView.findViewById(R.id.news_item_divider);
        }
    }

    class BigViewHolder extends RecyclerView.ViewHolder {
        TextViewBold title, date;
        ImageView imageView;
        View container;

        public BigViewHolder(View convertView) {
            super(convertView);
            title = convertView.findViewById(R.id.post_title);
            container = convertView.findViewById(R.id.post_container);
            date = convertView.findViewById(R.id.post_date);
            imageView = convertView.findViewById(R.id.post_image);
        }
    }
}