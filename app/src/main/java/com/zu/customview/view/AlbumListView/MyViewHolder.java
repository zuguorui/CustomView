package com.zu.customview.view.AlbumListView;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by zu on 17-7-24.
 */

public abstract class MyViewHolder extends RecyclerView.ViewHolder{
    public MyViewHolder(View itemView) {
        super(itemView);
    }

    abstract boolean isCheckable();

    abstract void setChecked(boolean checked);

    abstract boolean isChecked();
}
