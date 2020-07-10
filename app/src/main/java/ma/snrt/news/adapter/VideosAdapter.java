package ma.snrt.news.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import ma.snrt.news.AppController;
import ma.snrt.news.DailymotionActivity;
import ma.snrt.news.PopupPlayerActivity;
import ma.snrt.news.R;
import ma.snrt.news.YouTubePlayerActivity;
import ma.snrt.news.model.Post;
import ma.snrt.news.ui.TextViewBold;
import ma.snrt.news.ui.TextViewEBItalic;
import ma.snrt.news.ui.TextViewRegular;
import ma.snrt.news.util.Cache;
import ma.snrt.news.util.Utils;

public class VideosAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<Post> items;
    Context context;
    private int visibleThreshold = 1;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;
    private int lastPosition = -1;
    View lastFabMenuVisible;
    private static int TYPE_NORMAL = 1;
    private static int TYPE_BIG = 2;

    public VideosAdapter(Context context, ArrayList<Post> items, RecyclerView recyclerView) {
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
        } catch (Exception ex) {

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
                if (item.getType().equals("youtube")) {
                    if (item.getLink() != null && !item.getLink().isEmpty()) {
                        String ytId = item.getLink();
                        //Utils.extractYoutubeVideoId(item.getLink());
                       // Intent intent = YouTubeStandalonePlayer.createVideoIntent((Activity) context, AppController.API_KEY, ytId, 0, true, true);
                       // context.startActivity(intent);

                        Intent intent = new Intent(context, YouTubePlayerActivity.class);
                        intent.putExtra("url", ytId);
                        context.startActivity(intent);
                    } else
                        Toast.makeText(context, context.getString(R.string.error_play_video), Toast.LENGTH_SHORT).show();
                } else if (item.getType().equals("dailymotion")) {
                    Intent intent = new Intent(context, DailymotionActivity.class);
                    intent.putExtra("url", item.getLink());
                    context.startActivity(intent);
                } else if (item.getType().equals("url")) {
                    Intent intent = new Intent(context, PopupPlayerActivity.class);
                    intent.putExtra("video_uri", item.getLink());
                    intent.putExtra("video_image", item.getImage());
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
            }
        });
        mHolder.title.setText(Html.fromHtml(item.getTitle()));
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

        if (item.getDatePublication() != null) {
            mHolder.date.setVisibility(View.VISIBLE);
            mHolder.date.setText(Utils.getPostRelativeDate(context, item.getDatePublication()));
        } else
            mHolder.date.setVisibility(View.GONE);

        if (!Cache.existsInVidFav(item.getId() + "")) {
            if (!AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
                mHolder.favBtn.setImageResource(R.drawable.bookmarks_empty);
            else
                mHolder.favBtn.setImageResource(R.drawable.bookmarks_dark);
        } else
            mHolder.favBtn.setImageResource(R.drawable.bookmarks);

        View.OnClickListener onClickListener = v -> {
            switch (v.getId()) {
                case R.id.fav_btn:
                    if (!Cache.existsInVidFav(item.getId() + "")) {
                        Cache.putVidToFav(item.getId() + "", item);
                        mHolder.favBtn.setImageResource(R.drawable.bookmarks);
                    } else {
                        Cache.removeVidFromFav(item.getId() + "");
                        if (AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
                            mHolder.favBtn.setImageResource(R.drawable.bookmarks_dark);
                        else
                            mHolder.favBtn.setImageResource(R.drawable.bookmarks_empty);
                    }
                    break;
                case R.id.share_btn:
                    try {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name));
                        intent.putExtra(Intent.EXTRA_TEXT, item.getTitle() + "\n" + item.getUrl());
                        intent.setType("text/plain");
                        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)));
                    } catch (Exception e) {
                        Toast.makeText(context, context.getString(R.string.api_error), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        };
        mHolder.favBtn.setOnClickListener(onClickListener);
        mHolder.shareBtn.setOnClickListener(onClickListener);
        if (AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
            mHolder.container.setBackgroundColor(ContextCompat.getColor(context, R.color.bgGrey2Dark));
        else
            mHolder.container.setBackgroundColor(ContextCompat.getColor(context, R.color.app_white));
        if (context.getResources().getBoolean(R.bool.is_tablet)) {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mHolder.imageLayout.getLayoutParams();
            lp.setMargins(0, 0, 0, Utils.dpToPx(context.getResources(), 10));
            mHolder.imageLayout.setLayoutParams(lp);
            mHolder.title.setMaxLines(2);
            mHolder.title.setMinLines(2);
            mHolder.title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
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
        if (position == 0)
            return TYPE_BIG;
        return TYPE_NORMAL;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup,
                                                      int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.video_item_layout, viewGroup, false);
        if (viewType == TYPE_NORMAL && !context.getResources().getBoolean(R.bool.is_tablet))
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.video_item_small_layout, viewGroup, false);
        return new ViewHolder(v);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextViewBold title;
        TextViewRegular date;
        TextViewEBItalic category;
        ImageView imageView, playBtn, shareBtn, favBtn;
        LinearLayout container;
        RelativeLayout imageLayout;
        CardView parent;

        public ViewHolder(View convertView) {
            super(convertView);
            title = convertView.findViewById(R.id.post_title);
            container = convertView.findViewById(R.id.post_container);
            imageView = convertView.findViewById(R.id.post_image);
            date = convertView.findViewById(R.id.post_date);
            playBtn = convertView.findViewById(R.id.play_btn);
            shareBtn = convertView.findViewById(R.id.share_btn);
            favBtn = convertView.findViewById(R.id.fav_btn);
            category = convertView.findViewById(R.id.post_category);
            imageLayout = convertView.findViewById(R.id.post_image_layout);
            parent = convertView.findViewById(R.id.post_item_parent);
        }
    }


}