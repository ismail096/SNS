package ma.snrt.news.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import ma.snrt.news.AgendaActivity;
import ma.snrt.news.AppController;
import ma.snrt.news.R;
import ma.snrt.news.model.CategoryAgenda;
import ma.snrt.news.model.Post;
import ma.snrt.news.network.ApiCall;
import ma.snrt.news.network.GsonHelper;
import ma.snrt.news.ui.TextViewExtraBold;
import ma.snrt.news.ui.TextViewRegular;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllAgendaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<CategoryAgenda> items;
    Context context;

    public AllAgendaAdapter(Context context, ArrayList<CategoryAgenda> items) {
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
        final CategoryAgenda item = items.get(position);
        if(holder instanceof ViewHolder) {
            final ViewHolder mHolder = (ViewHolder) holder;
            mHolder.name.setText(Html.fromHtml(item.getTitle()));
            getAgenda(mHolder.recyclerView, mHolder.emptyTextView, item.getId());
            if(position%2==0){
                if(AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
                    mHolder.container.setBackgroundColor(ContextCompat.getColor(context, R.color.bgGrey2Dark));
                else
                    mHolder.container.setBackgroundColor(ContextCompat.getColor(context, R.color.app_white));
            }
            else
            {
                if(AppController.getSharedPreferences().getBoolean("NIGHT_MODE", false))
                    mHolder.container.setBackgroundColor(ContextCompat.getColor(context, R.color.bgGreyDark));
                else
                    mHolder.container.setBackgroundColor(ContextCompat.getColor(context, R.color.bgGrey));
            }
            mHolder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, AgendaActivity.class);
                    intent.putExtra("category", item);
                    context.startActivity(intent);
                }
            });
            mHolder.moreBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, AgendaActivity.class);
                    intent.putExtra("category", item);
                    context.startActivity(intent);
                }
            });
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup,
                                                      int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.agenda_all_item_layout, viewGroup, false);
        return new ViewHolder(v);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        RecyclerView recyclerView;
        TextViewExtraBold name, moreBtn;
        TextViewRegular emptyTextView;
        LinearLayout container;

        public ViewHolder(View convertView) {
            super(convertView);
            recyclerView = convertView.findViewById(R.id.agenda_recyclerview);
            name = convertView.findViewById(R.id.category_name);
            container = convertView.findViewById(R.id.item_container);
            emptyTextView = convertView.findViewById(R.id.empty_textview);
            moreBtn = convertView.findViewById(R.id.see_more);
        }
    }

    private void getAgenda(RecyclerView rv, TextViewRegular emptyView, int catId){
        ApiCall.getAgendaByCat(catId, "", "", "", 0, new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if(response.body()!=null && response.isSuccessful()){
                    ArrayList<Post> agendas = GsonHelper.getGson().fromJson(response.body(), new TypeToken<List<Post>>(){}.getType());
                    setAgendaAdapter(rv, emptyView, agendas);
                }
                else {
                    rv.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                rv.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setAgendaAdapter(RecyclerView rv, TextViewRegular emptyView, ArrayList<Post> list){
        if(list.size()>0){
            rv.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            ArrayList<Post> agendas = new ArrayList<>();
            GridLayoutManager llm = new GridLayoutManager(context, 2);
            rv.setLayoutManager(llm);
            if(list.size()>2) {
                agendas.addAll(list.subList(0, 2));
            }
            else
                agendas.addAll(list);
            AgendaAdapter2 agendaAdapter = new AgendaAdapter2(context, agendas, rv);
            rv.setAdapter(agendaAdapter);
        }
        else{
            rv.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }
    }
}