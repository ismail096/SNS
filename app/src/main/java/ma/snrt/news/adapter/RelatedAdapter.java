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
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ma.snrt.news.AppController;
import ma.snrt.news.PostDetailActivity;
import ma.snrt.news.R;
import ma.snrt.news.model.Post;
import ma.snrt.news.ui.TextViewBold;
import ma.snrt.news.ui.TextViewEBItalic;
import ma.snrt.news.ui.TextViewRegular;
import ma.snrt.news.ui.TextViewExtraBold;
import ma.snrt.news.util.Cache;
import ma.snrt.news.util.Utils;

public class RelatedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<Post> items;
    Context context;

    public RelatedAdapter(Context context, ArrayList<Post> items) {
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
        final Post item = items.get(position);
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

        if(item.getDatePublication()!=null) {
            mHolder.date.setText(Utils.getPostRelativeDate(context, item.getDatePublication()));
        }
        if(item.getCategory()!=null)
        mHolder.category.setText(Html.fromHtml(item.getCategory()));

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.placeholder);
        requestOptions.error(R.drawable.placeholder);
        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL);

        Glide.with(context)
                .load(item.getImage())
                .apply(requestOptions)
                .into(mHolder.imageView);

        if(item.getColor()!=null && item.getColor().length()==7)
            mHolder.category.setTextColor(Color.parseColor(item.getColor()));
        else
            mHolder.category.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));

        mHolder.title.setClickable(false);

        if(!Cache.existsInFavoris(item.getId()+"")) {
            if(!AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
                mHolder.favBtn.setImageResource(R.drawable.bookmarks_empty);
            else
                mHolder.favBtn.setImageResource(R.drawable.bookmarks_dark);
        }
        else
            mHolder.favBtn.setImageResource(R.drawable.bookmarks);

        mHolder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name));
                    intent.putExtra(Intent.EXTRA_TEXT, item.getTitle()+"\n"+item.getUrl());
                    intent.setType("text/plain");
                    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)));
                } catch(Exception e) {
                    Toast.makeText(context, context.getString(R.string.api_error), Toast.LENGTH_SHORT).show();
                }
            }
        });
        mHolder.favBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Cache.existsInFavoris(item.getId()+"")){
                    Cache.putPost(item.getId()+"", item);
                    mHolder.favBtn.setImageResource(R.drawable.bookmarks);
                }
                else
                {
                    Cache.removePost(item.getId()+"");
                    if(AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
                        mHolder.favBtn.setImageResource(R.drawable.bookmarks_dark);
                    else
                        mHolder.favBtn.setImageResource(R.drawable.bookmarks_empty);
                }
            }
        });
        /*if(position<items.size()-1)
            mHolder.divider.setVisibility(View.VISIBLE);
        else*/
            mHolder.divider.setVisibility(View.GONE);

        if(position%2==0) {
            if(!AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
                mHolder.container.setBackgroundColor(ContextCompat.getColor(context, R.color.app_white));
            else
                mHolder.container.setBackgroundColor(ContextCompat.getColor(context, R.color.app_black));
        }
        else {
            if(!AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
                mHolder.container.setBackgroundColor(ContextCompat.getColor(context, R.color.bgGrey));
            else
                mHolder.container.setBackgroundColor(ContextCompat.getColor(context, R.color.bgGreyDark));
        }

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup,
                                                      int viewType) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.news_item_layout, viewGroup, false);
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
}