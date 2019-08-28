package com.vtech.contactsearch;

import androidx.recyclerview.widget.RecyclerView;
import android.view.ViewGroup;


public interface StickyHeaderAdapter<T extends RecyclerView.ViewHolder> {
    long getHeaderId(int position);

    T onCreateHeaderViewHolder(ViewGroup parent);

    void onBindHeaderViewHolder(T viewholder, int position);
}
