package com.zu.customview.view.AlbumListView;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.zu.customview.MyLog;
import com.zu.customview.R;
import com.zu.customview.module.ImageModule;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by zu on 17-7-17.
 */

public class ImageNoGroupedAdapter extends ImageAdapter{
    private MyLog log = new MyLog("ImageNoGroupedAdapter", true);
    private LinkedList<ImageModule> data = new LinkedList<>();
    private ArrayList<String> sortedKeys = new ArrayList<>();
    private Context context;

    public ImageNoGroupedAdapter(@NonNull Context context, LinkedList<ImageModule> data) {
        this.context = context;
        if(data != null)
        {
            this.data = data;
        }
        setHasStableIds(true);

    }

    @Override
    public boolean isGrouped() {
        return false;
    }

    @Override
    public int getGroupIndex(long id) {
        return (int)((id & 0xffffffff00000000l) >> 32);
    }

    @Override
    public int getChildIndex(long id) {
        return (int)(id & 0x00000000ffffffffl);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List payloads) {
        super.onBindViewHolder(holder, position, payloads);
//        log.d("onBindViewHolder, position = " + position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getChildCount(int groupIndex) {
        return data.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder result = null;
        View view = LayoutInflater.from(context).inflate(R.layout.image_item, null);
        result = new ImageHolder(view);
        return result;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(position < 0 || position >= getItemCount())
        {
            return;
        }
        log.d("onBindViewHolder, position = " + position);
        ImageHolder imageHolder = (ImageHolder)holder;
        imageHolder.textView.setText(position + "");
        ImageModule imageModule = data.get(position);
        Glide.clear(imageHolder.imageView);
        Glide.with(context)
                .load(new File(imageModule.path))
                .override(200, 200)
                .into(imageHolder.imageView);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private class ImageHolder extends RecyclerView.ViewHolder
    {
        public ImageView imageView;
        public CheckBox checkBox;
        public TextView textView;

        public ImageHolder(View itemView) {
            super(itemView);
            imageView = (ImageView)itemView.findViewById(R.id.ImageItem_imageView);
            checkBox = (CheckBox)itemView.findViewById(R.id.ImageItem_checkBox);
            textView = (TextView)itemView.findViewById(R.id.ImageItem_textView);
        }
    }
}
