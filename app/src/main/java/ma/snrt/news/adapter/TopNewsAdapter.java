package ma.snrt.news.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import ma.snrt.news.AgendaActivity;
import ma.snrt.news.AppController;
import ma.snrt.news.MainActivity;
import ma.snrt.news.PostDetailActivity;
import ma.snrt.news.R;
import ma.snrt.news.fragment.HomeFragment;
import ma.snrt.news.model.Post;
import ma.snrt.news.model.User;
import ma.snrt.news.network.ApiCall;
import ma.snrt.news.network.GsonHelper;
import ma.snrt.news.ui.TextViewBold;
import ma.snrt.news.ui.TextViewEBItalic;
import ma.snrt.news.ui.TextViewRegular;
import ma.snrt.news.ui.TextViewExtraBold;
import ma.snrt.news.util.Cache;
import ma.snrt.news.util.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TopNewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<Post> items;
    Context context;

    private int lastVisibleItem, totalItemCount;
    private int lastPosition = -1;
    private static int TYPE_NORMAL = 1;
    private static int TYPE_BIG = 2;
    private static int TYPE_STORIES = 3;
    private static int TYPE_AGENDA = 4;
    private static int TYPE_VIDEOS = 5;
    private ArrayList<User> users;
    private ArrayList<Post> videos;
    private ArrayList<Post> agendas;

    public TopNewsAdapter(Context context, ArrayList<Post> items, ArrayList<User> users, ArrayList<Post> videos, ArrayList<Post> agendas) {
        this.context = context;
        this.items = items;
        this.users = users;
        this.videos = videos;
        this.agendas = agendas;
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
            if(getItemViewType(position) == TYPE_NORMAL && mHolder.divider != null) {
                if (position == 3 || position == 9 || position == items.size()-1)
                    mHolder.divider.setVisibility(View.GONE);
                else
                    mHolder.divider.setVisibility(View.VISIBLE);
            }
            if(context.getResources().getBoolean(R.bool.is_tablet)){
                if(getItemViewType(position) == TYPE_NORMAL){
                    int diff = 0;
                    if(position>10)
                        diff = 11;
                    RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) mHolder.itemView.getLayoutParams();

                    if(position != 7 || position != 8 || position != 9) {
                        int width = Utils.getScreenWidth((Activity) context) / 3 - Utils.dpToPx(context.getResources(), 20);
                        lp.width = width;
                        if (((position + 1) - diff) % 4 == 0) {
                            lp.setMarginStart(Utils.dpToPx(context.getResources(), 0));
                        } else if (((position + 2) - diff) % 4 == 0) {
                            lp.setMarginStart(Utils.dpToPx(context.getResources(), 10));
                        } else if (((position + 3) - diff) % 4 == 0) {
                            lp.setMarginStart(Utils.dpToPx(context.getResources(), 20));
                        }
                    }

                    if(position==7){
                        int width = Utils.getScreenWidth((Activity) context) / 3 - Utils.dpToPx(context.getResources(), 20);
                        lp.width = width;
                        lp.setMarginStart(Utils.dpToPx(context.getResources(), 20));
                    }
                    if(position==8){
                        int width = Utils.getScreenWidth((Activity) context) / 3 - Utils.dpToPx(context.getResources(), 20);
                        lp.width = width;
                        lp.setMarginStart(Utils.dpToPx(context.getResources(), 10));
                    }
                    if(position==9){
                        int width = Utils.getScreenWidth((Activity) context) / 3 - Utils.dpToPx(context.getResources(), 20);
                        lp.width = width;
                        lp.setMarginStart(Utils.dpToPx(context.getResources(), 0));
                    }
                    mHolder.itemView.setLayoutParams(lp);
                    holder.setIsRecyclable(false);
                }
                else if(getItemViewType(position) == TYPE_BIG){
                    mHolder.postDataLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                    int p = Utils.dpToPx(context.getResources(), 10);
                    mHolder.postDataLayout.setPadding(p, 0, p, 0);
                }
            }
            //setAnimation(mHolder.itemView, position);
        }
        else if(holder instanceof StoriesHolder){
            StoriesHolder mHolder = (StoriesHolder) holder;
            if(AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
                mHolder.title.setTextColor(ContextCompat.getColor(context, R.color.text_grey3));
            if(users.size()>0)
                setStoriesAdapter(mHolder.recyclerView);
            else
                mHolder.itemView.setVisibility(View.GONE);
        }
        else if(holder instanceof AgendaHolder){
            AgendaHolder mHolder = (AgendaHolder) holder;
            if(AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
                mHolder.title.setTextColor(ContextCompat.getColor(context, R.color.text_grey3));
            mHolder.recyclerView.setNestedScrollingEnabled(false);
            if(agendas.size()>0) {
                setAgendaAdapter(mHolder.recyclerView);
                mHolder.seeMore.setOnClickListener(view -> {
                    //context.startActivity(new Intent(context, AgendaActivity.class));
                    if(context != null){
                        ViewPager viewPager = ((MainActivity) context).viewPager;
                        if(viewPager!=null) {
                            HomeFragment frag1 = (HomeFragment) viewPager
                                    .getAdapter()
                                    .instantiateItem(viewPager, viewPager.getCurrentItem());
                            frag1.selectAgenda();
                        }
                    }
                });
            }
            else
                mHolder.itemView.setVisibility(View.GONE);
        }
        else if(holder instanceof VideosHolder){
            VideosHolder mHolder = (VideosHolder) holder;
            mHolder.recyclerView.setNestedScrollingEnabled(false);
            mHolder.seeMore.setOnClickListener(view ->{
                ((MainActivity) context).viewPager.setCurrentItem(2);
            });
            if(videos.size()>0)
                setVideosAdapter(mHolder.recyclerView);
            else
                mHolder.itemView.setVisibility(View.GONE);
        }

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
        if(items.get(position).getCustomCategory() == 1)
            return TYPE_STORIES;
        if(items.get(position).getCustomCategory() == 2)
            return TYPE_AGENDA;
        if(items.get(position).getCustomCategory() == 3)
            return TYPE_VIDEOS;
        if((position==0 || position==6 || position==11 || (position>11 && (position-11)% 4 == 0)))
            return TYPE_BIG;
        /*if(context.getResources().getBoolean(R.bool.is_tablet) && (position==0 || position== 6 || position == 11 || (position>11 && (position - 11) % 4 == 0)))
            return TYPE_BIG;*/
        return TYPE_NORMAL;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup,
                                                      int viewType) {
        RecyclerView.ViewHolder mViewHolder;
        View v;
        switch (viewType){
            case 1:
                if(!context.getResources().getBoolean(R.bool.is_tablet))
                    v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.news_item_layout, viewGroup, false);
                else
                    v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.news_tab_item_layout, viewGroup, false);
                mViewHolder =  new ViewHolder(v);
                break;
            case 2:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.news_item_big_layout, viewGroup, false);
                mViewHolder =  new ViewHolder(v);
                break;
            case 3:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.top_stories_item, viewGroup, false);
                mViewHolder =  new StoriesHolder(v);
                break;
            case 4:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.top_agenda_item, viewGroup, false);
                mViewHolder =  new AgendaHolder(v);
                break;
            case 5:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.top_videos_item, viewGroup, false);
                mViewHolder =  new VideosHolder(v);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + viewType);
        }
        return mViewHolder;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextViewBold title;
        TextViewEBItalic category;
        TextViewRegular date;
        ImageView imageView, shareBtn, favBtn;
        View container, divider;
        LinearLayout postDataLayout;

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
            postDataLayout = convertView.findViewById(R.id.post_data_layout);
        }
    }

    class StoriesHolder extends RecyclerView.ViewHolder {
        RecyclerView recyclerView;
        LinearLayout container;
        TextViewExtraBold title;

        public StoriesHolder(View convertView) {
            super(convertView);
            recyclerView = convertView.findViewById(R.id.stories_recycler);
            container = convertView.findViewById(R.id.stories_container);
            title = convertView.findViewById(R.id.top_title);
        }
    }

    class AgendaHolder extends RecyclerView.ViewHolder {
        RecyclerView recyclerView;
        LinearLayout container;
        TextViewExtraBold seeMore, title;

        public AgendaHolder(View convertView) {
            super(convertView);
            recyclerView = convertView.findViewById(R.id.agenda_recycler);
            seeMore = convertView.findViewById(R.id.see_more);
            container = convertView.findViewById(R.id.agendas_container);
            title = convertView.findViewById(R.id.top_title);
        }
    }

    class VideosHolder extends RecyclerView.ViewHolder {
        RecyclerView recyclerView;
        LinearLayout container;
        TextViewExtraBold seeMore;

        public VideosHolder(View convertView) {
            super(convertView);
            recyclerView = convertView.findViewById(R.id.videos_recycler);
            container = convertView.findViewById(R.id.videos_container);
            seeMore = convertView.findViewById(R.id.see_more);
        }
    }

    private void setStoriesAdapter(RecyclerView rv){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    LinearLayoutManager llm = new LinearLayoutManager(context);
                    llm.setOrientation(LinearLayoutManager.HORIZONTAL);
                    rv.setHasFixedSize(true);
                    rv.setLayoutManager(llm);
                    StoryAdapter storyAdapter = new StoryAdapter(context, users);
                    rv.setAdapter(storyAdapter);
                }
            }, 200);

    }

    private void setAgendaAdapter(RecyclerView rv){
            LinearLayoutManager llm = new LinearLayoutManager(context);
            if(!context.getResources().getBoolean(R.bool.is_tablet))
                llm.setOrientation(LinearLayoutManager.VERTICAL);
            else
                llm.setOrientation(LinearLayoutManager.HORIZONTAL);
            rv.setHasFixedSize(true);
            rv.setLayoutManager(llm);
            AgendaAdapter storyAdapter = new AgendaAdapter(context, agendas, rv);
            rv.setAdapter(storyAdapter);
    }

    private void setVideosAdapter(RecyclerView rv){
            if(!context.getResources().getBoolean(R.bool.is_tablet)) {
                LinearLayoutManager llm = new LinearLayoutManager(context);
                llm.setOrientation(LinearLayoutManager.VERTICAL);
                rv.setHasFixedSize(true);
                rv.setLayoutManager(llm);
            }
            else{
                GridLayoutManager glm = new GridLayoutManager(context, 3);
                rv.setHasFixedSize(true);
                rv.setLayoutManager(glm);
            }
            VideosAdapter adapter = new VideosAdapter(context, videos, rv);
            rv.setAdapter(adapter);
        if(context.getResources().getBoolean(R.bool.is_tablet)){
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) rv.getLayoutParams();
            int margin = Utils.dpToPx(context.getResources(), 7);
            lp.setMargins(margin, lp.topMargin, margin, lp.bottomMargin );
            rv.setLayoutParams(lp);
        }
    }
}