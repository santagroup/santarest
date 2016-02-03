package com.santarest.sample.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public abstract class RecyclerViewAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    private OnItemClickListener listener;

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        public void onItemClicked(View view, int position);
    }

    @Override
    public void onViewAttachedToWindow(final VH holder) {
        if (listener == null) {
            return;
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener == null) {
                    return;
                }
                listener.onItemClicked(v, holder.getPosition());
            }
        });
    }
}