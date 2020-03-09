package ma.snrt.news.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ma.snrt.news.AgendaDetailActivity;
import ma.snrt.news.AppController;
import ma.snrt.news.R;
import ma.snrt.news.model.Post;
import ma.snrt.news.ui.TextViewBold;
import ma.snrt.news.ui.TextViewRegular;
import ma.snrt.news.ui.TextViewExtraBold;
import ma.snrt.news.ui.RoundRectCornerImageView;
import ma.snrt.news.util.Utils;

public class AgendaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<Post> items;
    Context context;
    private int visibleThreshold = 1;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;

    public AgendaAdapter(Context context, ArrayList<Post> items, RecyclerView recyclerView) {
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
        mHolder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AgendaDetailActivity.class);
                intent.putExtra("post", item);
                context.startActivity(intent);
            }
        });

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

        mHolder.title.setText(Html.fromHtml(item.getTitle()));
        if(item.getDate()!=null) {
                mHolder.date.setText(Utils.getPostRelativeDate(context, item.getDate()));
        }
        if(item.getPosition()!=null && !item.getPosition().isEmpty())
            mHolder.position.setText(item.getPosition());
        mHolder.title.setClickable(false);

        if(AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
            mHolder.container.setCardBackgroundColor(ContextCompat.getColor(context, R.color.bgGreyDark));
        else
            mHolder.container.setCardBackgroundColor(ContextCompat.getColor(context, R.color.app_white));

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup,
                                                      int viewType) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.agenda_item_layout, viewGroup, false);
            return new ViewHolder(v);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextViewBold title;
        TextViewExtraBold moreBtn;
        TextViewRegular date, position;
        CardView container;
        RoundRectCornerImageView imageView;

        public ViewHolder(View convertView) {
            super(convertView);
            title = convertView.findViewById(R.id.post_title);
            container = convertView.findViewById(R.id.agenda_item_container);
            date = convertView.findViewById(R.id.post_date);
            imageView = convertView.findViewById(R.id.post_img);
            position = convertView.findViewById(R.id.post_position);
            moreBtn = convertView.findViewById(R.id.more_btn);
        }
    }
}