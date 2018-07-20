package cc.hurrypeng.www.fragmem;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import cc.hurrypeng.www.fragmem.Util.*;

/**
 * Created by haora on 2018.07.14.
 */

public class FragAdapter extends RecyclerView.Adapter<FragAdapter.ViewHolder> {

    private Context context;
    private List<Frag> fragList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        TextView textViewContent;

        public ViewHolder(View view) {
            super(view);
            textViewTitle = view.findViewById(R.id.fragItemTitle);
            textViewContent = view.findViewById(R.id.fragItemContent);
        }
    }

    public FragAdapter(Context context, List<Frag> _fragList) {
        this.context = context;
        fragList = _fragList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_frag, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                Intent intent = new Intent(context, FragDetailActivity.class);
                intent.putExtra("position", position);
                context.startActivity(intent);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Frag frag = fragList.get(position);
        holder.textViewTitle.setText(frag.getTitle());
        holder.textViewContent.setText(frag.getContent());
    }

    @Override
    public int getItemCount() {
        return fragList.size();
    }
}
