package com.sandeshkoli.yttrendy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.sandeshkoli.yttrendy.R;
import com.sandeshkoli.yttrendy.models.VideoItem;
import java.util.List;

public class ShortsAdapter extends RecyclerView.Adapter<ShortsAdapter.ShortsViewHolder> {

    private Context context;
    private List<VideoItem> shortsList;
    private OnShortsClickListener listener;

    public interface OnShortsClickListener {
        void onShortClick(VideoItem item);
    }

    public ShortsAdapter(Context context, List<VideoItem> shortsList, OnShortsClickListener listener) {
        this.context = context;
        this.shortsList = shortsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ShortsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_shorts_card, parent, false);
        return new ShortsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ShortsViewHolder holder, int position) {
        VideoItem item = shortsList.get(position);
        holder.title.setText(item.getSnippet().getTitle());

        Glide.with(context)
                .load(item.getSnippet().getThumbnails().getHigh().getUrl())
                .into(holder.thumbnail);

        holder.itemView.setOnClickListener(v -> listener.onShortClick(item));
    }

    @Override
    public int getItemCount() { return shortsList.size(); }

    public static class ShortsViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView title;
        public ShortsViewHolder(View v) {
            super(v);
            thumbnail = v.findViewById(R.id.shorts_thumbnail);
            title = v.findViewById(R.id.shorts_title);
        }
    }
}