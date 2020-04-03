package ma.snrt.news.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import ma.snrt.news.R;
import ma.snrt.news.StatusStoriesActivity;
import ma.snrt.news.StoryActivity;
import ma.snrt.news.model.User;
import ma.snrt.news.util.Utils;

public class StoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<User> items;
    Context context;
    private int lastPosition = -1;

    public StoryAdapter(Context context, ArrayList<User> items) {
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
        final User item = items.get(position);
        final ViewHolder mHolder = (ViewHolder) holder;

        mHolder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent intent = new Intent(view.getContext(), StatusStoriesActivity.class);
                intent.putExtra(StatusStoriesActivity.STATUS_RESOURCES_KEY, item);
                Bundle bundle = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    Pair<View, String> p1 = Pair.create(mHolder.storyImage, mHolder.storyImage.getTransitionName());
                    Pair<View, String> p2 = Pair.create(mHolder.imageView, mHolder.imageView.getTransitionName());
                    bundle = ActivityOptions.makeSceneTransitionAnimation(
                            (Activity) context,
                            p1, p2)
                            .toBundle();
                }
                context.startActivity(intent, bundle);*/
                Intent intent = new Intent(context, StoryActivity.class);
                intent.putExtra("users", items);
                intent.putExtra("user_index", position);
                context.startActivity(intent);
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setImage(mHolder.imageView, item.getImage());
                setImage(mHolder.storyImage, item.getStories().get(0).getImage());
                //setAnimation(mHolder.itemView, position);
            }
        }, 500);

    }

    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.anim_zoom);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    private void setImage(ImageView imageView, String imageUrl){
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.placeholder);
        requestOptions.error(R.drawable.placeholder);
        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL);

        Glide.with(context)
                .load(imageUrl)
                .apply(requestOptions)
                .into(imageView);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup,
                                                      int viewType) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.story_item_layout, viewGroup, false);
            return new ViewHolder(v);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imageView;
        ImageView storyImage;
        RelativeLayout container;
        View border;

        public ViewHolder(View convertView) {
            super(convertView);
            container = convertView.findViewById(R.id.story_container);
            imageView = convertView.findViewById(R.id.user_image);
            border = convertView.findViewById(R.id.user_border);
            storyImage = convertView.findViewById(R.id.story_image);
        }
    }
}