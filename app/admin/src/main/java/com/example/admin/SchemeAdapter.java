package com.example.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SchemeAdapter extends RecyclerView.Adapter<SchemeAdapter.ViewHolder> {

    private List<Scheme> schemeList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onClick(Scheme scheme);
    }

    public SchemeAdapter(List<Scheme> schemeList, OnItemClickListener listener) {
        this.schemeList = schemeList;
        this.listener = listener;
    }

    public void updateList(List<Scheme> list) {
        this.schemeList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_scheme, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Scheme scheme = schemeList.get(position);
        holder.tvSchemeName.setText(scheme.getScheme_name());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(scheme);
            }
        });
    }

    @Override
    public int getItemCount() {
        return schemeList == null ? 0 : schemeList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSchemeName;

        ViewHolder(View itemView) {
            super(itemView);
            tvSchemeName = itemView.findViewById(R.id.tvSchemeName);
        }
    }
}