package ma.snrt.news.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ma.snrt.news.AgendaDetailActivity;
import ma.snrt.news.R;
import ma.snrt.news.model.Post;
import ma.snrt.news.ui.TextViewRegular;
import ma.snrt.news.ui.TextViewExtraBold;
import ma.snrt.news.util.Utils;

public class TopAgendaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<Post> items;
    Context context;

    public TopAgendaAdapter(Context context, ArrayList<Post> items) {
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

        /*RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mHolder.container.getLayoutParams();
        if(position == 0) {
            lp.height = (int) (lp.height * 1.2);
            lp.width = (int) (lp.width * 1.2);
        }
        else{
            lp.width = Utils.dpToPx(context.getResources(), 180);
            lp.height = Utils.dpToPx(context.getResources(), 260);
            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        }
        mHolder.container.setLayoutParams(lp);*/
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup,
                                                      int viewType) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.agenda_top_item_layout, viewGroup, false);
            return new ViewHolder(v);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextViewExtraBold title;
        TextViewRegular date, position;
        RelativeLayout container;
        ImageView imageView;

        public ViewHolder(View convertView) {
            super(convertView);
            title = convertView.findViewById(R.id.post_title);
            container = convertView.findViewById(R.id.agenda_item_container);
            date = convertView.findViewById(R.id.post_date);
            imageView = convertView.findViewById(R.id.post_img);
            position = convertView.findViewById(R.id.post_position);
        }
    }
}