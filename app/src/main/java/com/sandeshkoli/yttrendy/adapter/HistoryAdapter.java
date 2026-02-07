package com.sandeshkoli.yttrendy.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.sandeshkoli.yttrendy.R;
import java.util.List;

// HistoryAdapter.java ko replace karein:

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private List<String> historyList;
    private OnHistoryClickListener listener;

    public interface OnHistoryClickListener {
        void onHistoryClick(String query);
        void onDeleteClick(String query); // Naya callback
    }

    public HistoryAdapter(List<String> historyList, OnHistoryClickListener listener) {
        this.historyList = historyList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_history, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String query = historyList.get(position);
        holder.text.setText(query);

        // Item click (Search perform karne ke liye)
        holder.itemView.setOnClickListener(v -> listener.onHistoryClick(query));

        // Delete click (X button)
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(query));
    }

    @Override
    public int getItemCount() { return historyList.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView text;
        ImageView btnDelete;
        public ViewHolder(View v) {
            super(v);
            text = v.findViewById(R.id.history_text);
            btnDelete = v.findViewById(R.id.btn_delete_history);
        }
    }
}