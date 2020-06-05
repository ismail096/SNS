package ma.snrt.news.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ma.snrt.news.AgendaDetailActivity;
import ma.snrt.news.R;
import ma.snrt.news.model.Post;
import ma.snrt.news.ui.TextViewRegular;
import ma.snrt.news.ui.TextViewExtraBold;
import ma.snrt.news.util.Cache;
import ma.snrt.news.util.Utils;

public class AgendaAdapter2 extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<Post> items;
    Context context;
    private int visibleThreshold = 1;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;

    public AgendaAdapter2(Context context, ArrayList<Post> items, RecyclerView recyclerView) {
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
                Intent intent = new Intent(context, AgendaDetailActivity.class);
                intent.putExtra("post", item);
                context.startActivity(intent);
            }
        });

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.placeholder);
        requestOptions.error(R.drawable.placeholder);
        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL);

        Glide.with(context)
                .load(item.getImage())
                .apply(requestOptions)
                .into(mHolder.imageView);

        mHolder.title.setText(Html.fromHtml(item.getTitle()));
        if(item.getDatePublication()!=null) {
                mHolder.date.setText(Utils.getPostRelativeDate(context, item.getDatePublication()));
        }
        if(item.getPosition()!=null && !item.getPosition().isEmpty())
            mHolder.position.setText(item.getPosition());
        mHolder.title.setClickable(false);

        if(!Cache.existsInAgendaFav(item.getId()+"")) {
            /*if(!AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
                mHolder.favBtn.setImageResource(R.drawable.bookmarks_empty);
            else*/
                mHolder.favBtn.setImageResource(R.drawable.bookmarks_dark);
        }
        else {
            mHolder.favBtn.setImageResource(R.drawable.bookmarks);
        }

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
                if(!Cache.existsInAgendaFav(item.getId()+"")){
                    Cache.putAgendaToFav(item.getId()+"", item);
                    mHolder.favBtn.setImageResource(R.drawable.bookmarks);
                }
                else
                {
                    Cache.removeAgendaFromFav(item.getId()+"");
                    //if(AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
                        mHolder.favBtn.setImageResource(R.drawable.bookmarks_dark);
                    /*else
                        mHolder.favBtn.setImageResource(R.drawable.bookmarks_empty);*/
                }
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup,
                                                      int viewType) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.agenda2_item_layout, viewGroup, false);
            return new ViewHolder(v);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextViewExtraBold title;
        TextViewRegular date, position;
        LinearLayout container;
        ImageView imageView, shareBtn, favBtn;

        public ViewHolder(View convertView) {
            super(convertView);
            title = convertView.findViewById(R.id.post_title);
            container = convertView.findViewById(R.id.agenda_item_container);
            date = convertView.findViewById(R.id.post_date);
            imageView = convertView.findViewById(R.id.post_img);
            position = convertView.findViewById(R.id.post_position);
            shareBtn = convertView.findViewById(R.id.share_btn);
            favBtn = convertView.findViewById(R.id.fav_btn);
        }
    }
}