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
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ma.snrt.news.AppController;
import ma.snrt.news.PopupPlayerActivity;
import ma.snrt.news.R;
import ma.snrt.news.model.Post;
import ma.snrt.news.ui.TextViewBold;
import ma.snrt.news.ui.TextViewExtraBold;
import ma.snrt.news.ui.TextViewEBItalic;
import ma.snrt.news.util.Cache;
import ma.snrt.news.util.Utils;

public class Videos2Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<Post> items;
    Context context;
    private int visibleThreshold = 1;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;
    private int lastPosition = -1;

    public Videos2Adapter(Context context, ArrayList<Post> items, RecyclerView recyclerView) {
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

    public interface OnItemClickListener {
        void onItemClick(Post post);
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
              if(item.getType().equals("youtube")){
                  if(item.getLink()!=null && !item.getLink().isEmpty()) {
                      String ytId = Utils.extractYoutubeVideoId(item.getLink());
                      Intent intent = YouTubeStandalonePlayer.createVideoIntent((Activity) context, AppController.API_KEY, ytId, 0, true, false);
                      context.startActivity(intent);
                  }
                  else
                      Toast.makeText(context, context.getString(R.string.error_play_video), Toast.LENGTH_SHORT).show();
              }
              else if(item.getType().equals("dailymotion")){

              }
              else if(item.getType().equals("url")){
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
        if(item.getColor()!=null && item.getColor().length()==7)
            mHolder.category.setTextColor(Color.parseColor(item.getColor()));
        else
            mHolder.category.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
        try {
            Picasso.with(context)
                    .load(item.getImage())
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .error(R.drawable.placeholder)
                    .into(mHolder.imageView, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(context)
                                    .load(item.getImage())
                                    .error(R.drawable.placeholder)
                                    .into(mHolder.imageView);
                        }
                    });
        } catch (Exception ex) {
            mHolder.imageView.setImageResource(R.drawable.placeholder);
        }

        if(!Cache.existsInVidFav(item.getId()+"")) {
            if(!AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
                mHolder.favBtn.setImageResource(R.drawable.bookmarks_empty);
            else
                mHolder.favBtn.setImageResource(R.drawable.bookmarks_dark);
        }
        else
            mHolder.favBtn.setImageResource(R.drawable.bookmarks);

        View.OnClickListener onClickListener = v -> {
            switch (v.getId()){
                case R.id.fav_btn:
                    if(!Cache.existsInVidFav(item.getId()+"")){
                        Cache.putVidToFav(item.getId()+"", item);
                        mHolder.favBtn.setImageResource(R.drawable.bookmarks);
                    }
                    else
                    {
                        Cache.removeVidFromFav(item.getId()+"");
                        if(AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
                            mHolder.favBtn.setImageResource(R.drawable.bookmarks_dark);
                        else
                            mHolder.favBtn.setImageResource(R.drawable.bookmarks_empty);
                    }
                    break;
                case R.id.share_btn:
                    try {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name));
                        intent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_post)+" "+item.getUrl());
                        intent.setType("text/plain");
                        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)));
                    } catch(Exception e) {
                        Toast.makeText(context, context.getString(R.string.api_error), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        };
        mHolder.favBtn.setOnClickListener(onClickListener);
        mHolder.shareBtn.setOnClickListener(onClickListener);

        if(AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
            mHolder.container.setBackgroundColor(ContextCompat.getColor(context, R.color.bgGrey2Dark));
        else
            mHolder.container.setBackgroundColor(ContextCompat.getColor(context, R.color.app_white));

        if(item.getDatePublication()!=null) {
            mHolder.date.setText(Utils.getPostRelativeDate(context, item.getDatePublication()));
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
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.video_item_layout2, viewGroup, false);
            return new ViewHolder(v);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextViewBold title;
        TextViewExtraBold date;
        TextViewEBItalic category;
        ImageView imageView, playBtn, shareBtn, favBtn;
        LinearLayout container;

        public ViewHolder(View convertView) {
            super(convertView);
            title = convertView.findViewById(R.id.post_title);
            container = convertView.findViewById(R.id.post_container);
            imageView = convertView.findViewById(R.id.post_image);
            playBtn = convertView.findViewById(R.id.play_btn);
            shareBtn = convertView.findViewById(R.id.share_btn);
            favBtn = convertView.findViewById(R.id.fav_btn);
            category = convertView.findViewById(R.id.post_category);
            date = convertView.findViewById(R.id.post_date);
        }
    }
}