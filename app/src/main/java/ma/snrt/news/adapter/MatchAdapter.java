package ma.snrt.news.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import ma.snrt.news.R;
import ma.snrt.news.model.Match;
import ma.snrt.news.ui.TextViewBold;

public class MatchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<Match> items;
    Context context;

    public MatchAdapter(Context context, ArrayList<Match> items) {
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
        final Match item = items.get(position);
        final ViewHolder mHolder = (ViewHolder) holder;

        mHolder.teamA.setText(item.getDom());
        mHolder.teamB.setText(item.getExtr());
        mHolder.teamAResult.setText(item.getS_dom()+"");
        mHolder.teamBResult.setText(item.getS_extr()+"");
        //mHolder.matchStatus.setText(item.getStatus());
        mHolder.matchTime.setText(item.getTime());

        loadImage(mHolder.teamAImage, item.getDomImage());
        loadImage(mHolder.teamBImage, item.getExtrImage());

        mHolder.matchStatusLayout.post(new Runnable() {
            @Override
            public void run() {
                mHolder.matchTime.setWidth(mHolder.matchStatusLayout.getWidth());
            }
        });
    }

    private void loadImage(ImageView imageView, String url){
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.placeholder);
        requestOptions.error(R.drawable.placeholder);
        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL);

        Glide.with(context)
                .load(url)
                .apply(requestOptions)
                .into(imageView);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup,
                                                      int viewType) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.match_item_layout, viewGroup, false);
            return new ViewHolder(v);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextViewBold teamA, teamB, teamAResult, teamBResult, matchStatus, matchTime;
        ImageView teamAImage, teamBImage;
        LinearLayout matchStatusLayout;

        public ViewHolder(View convertView) {
            super(convertView);
            teamA = convertView.findViewById(R.id.team_a_name);
            teamB = convertView.findViewById(R.id.team_b_name);
            teamAResult = convertView.findViewById(R.id.team_a_result);
            teamBResult = convertView.findViewById(R.id.team_b_result);
            teamAImage = convertView.findViewById(R.id.team_a_img);
            teamBImage = convertView.findViewById(R.id.team_b_img);
            matchStatus = convertView.findViewById(R.id.match_status);
            matchTime = convertView.findViewById(R.id.match_time);
            matchStatusLayout = convertView.findViewById(R.id.match_status_layout);
        }
    }
}