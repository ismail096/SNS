package ma.snrt.news.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.JsonObject;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ma.snrt.news.R;
import ma.snrt.news.model.Post;
import ma.snrt.news.network.ApiCall;
import ma.snrt.news.ui.TextViewRegular;
import ma.snrt.news.ui.TextViewExtraBold;
import ma.snrt.news.util.Cache;
import ma.snrt.news.util.DateTimeUtils;
import ma.snrt.news.util.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AgendaDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    ArrayList<Post> items;
    Context context;
    private final OnItemClickListener listener;


    public AgendaDetailAdapter(Context context, ArrayList<Post> items, OnItemClickListener onItemClickListener) {
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
        final Post item = items.get(position);
        final ViewHolder mHolder = (ViewHolder) holder;
        mHolder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(item.getId());
            }
        });
        if(item.getPosition()!=null)
            mHolder.position.setText(item.getPosition());
        if(item.getDate()!=null && !item.getDate().isEmpty()){
            mHolder.dateLayout.setVisibility(View.VISIBLE);
            mHolder.day.setText(DateTimeUtils.getDayFromDate(item.getDate()));
            mHolder.month.setText(DateTimeUtils.getMonthFromDate(item.getDate()));
        }
        else
            mHolder.dateLayout.setVisibility(View.GONE);

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
            layoutParams.width = Utils.dpToPx(context.getResources(), 180);
            layoutParams.height = Utils.dpToPx(context.getResources(), 220);
            mHolder.container.setLayoutParams(layoutParams);
        }
        else{
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) mHolder.container.getLayoutParams();
            layoutParams.width = Utils.dpToPx(context.getResources(), 140);
            layoutParams.height = Utils.dpToPx(context.getResources(), 180);
            mHolder.container.setLayoutParams(layoutParams);
        }

        if(Cache.existsInLikes(item.getId()+""))
            mHolder.likeBtn.setImageResource(R.drawable.like_full);
        else
            mHolder.likeBtn.setImageResource(R.drawable.story_like);

        mHolder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postLike(mHolder.likeBtn, !Cache.existsInLikes(item.getId()+""), item.getId());
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup,
                                                      int viewType) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.agenda_d_item_layout, viewGroup, false);
            return new ViewHolder(v);
    }

    private void postLike(final ImageView imageView, final boolean like, final int postId){
        ApiCall.likePostAgenda(like, postId, new Callback<JsonObject>(){
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if(response.isSuccessful() && response.body()!=null){
                    if(response.body().has("status") && response.body().get("status").getAsBoolean()){
                        if(like){
                            Cache.likePost(postId+"");
                            imageView.setImageResource(R.drawable.like_full);
                        }
                        else{
                            Cache.unLikePost(postId+"");
                            imageView.setImageResource(R.drawable.story_like);
                        }
                        return;
                    }
                }
                Toast.makeText(context, context.getString(R.string.api_error), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(context, context.getString(R.string.api_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image, likeBtn;
        TextViewRegular position;
        TextViewExtraBold day, month;
        CardView container;
        LinearLayout dateLayout;

        public ViewHolder(View convertView) {
            super(convertView);
            position = convertView.findViewById(R.id.post_position);
            container = convertView.findViewById(R.id.post_container);
            image = convertView.findViewById(R.id.post_image);
            likeBtn = convertView.findViewById(R.id.like_btn);
            day = convertView.findViewById(R.id.post_day);
            month = convertView.findViewById(R.id.post_month);
            dateLayout = convertView.findViewById(R.id.post_date_layout);
        }
    }
}