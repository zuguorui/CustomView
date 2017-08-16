package com.zu.customview.view.AlbumListView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.util.LruCache;
import com.zu.customview.MyLog;
import com.zu.customview.R;
import com.zu.customview.module.ImageModule;
import com.zu.customview.utils.CommonUtil;
import com.zu.customview.view.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_DRAGGING;
import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static android.support.v7.widget.RecyclerView.SCROLL_STATE_SETTLING;

/**
 * Created by zu on 17-7-6.
 */

public class ImageGroupedAdapter extends ImageAdapter {


    private MyLog log = new MyLog("ImageGroupedAdapter", true);
    private HashMap<String, LinkedList<ImageModule>> data = new HashMap<>();
    private ArrayList<String> sortedKeys = new ArrayList<>();
    private Context context;
    int itemCount = 0;
    private Drawable drawable = null;


    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            switch (newState)
            {
                case SCROLL_STATE_IDLE:
                case SCROLL_STATE_DRAGGING:
                    Glide.with(context).resumeRequests();
                    break;
                case SCROLL_STATE_SETTLING:
                    Glide.with(context).pauseRequests();
                    break;
                default:
                    break;
            }
        }
    };



    public RecyclerView.OnScrollListener getOnScrollListener()
    {
        return onScrollListener;
    }
    public ImageGroupedAdapter(@NonNull Context context, HashMap<String, LinkedList<ImageModule>> data) {

        this.context = context;
        if(data != null)
        {
            this.data = data;
            init();
        }
        setHasStableIds(false);
        drawable = context.getDrawable(R.drawable.chuyin);

    }

    private void init()
    {
        itemCount = 0;
        LinkedList<String> temp = CommonUtil.sortKey(data.keySet());
        if(temp == null || temp.size() == 0)
        {
            return;
        }
        sortedKeys = new ArrayList<>(temp);
        for(String s : sortedKeys)
        {
            itemCount += data.get(s).size();
        }
        itemCount += sortedKeys.size();
    }




    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        log.d("onCreateViewHolder, viewType = " + viewType);
        RecyclerView.ViewHolder result = null;
        if(viewType == VIEW_TYPE_UNZOOM)
        {
            View view = LayoutInflater.from(context).inflate(R.layout.title_item, null);
            result = new TitleHolder(view);
        }else if(viewType == VIEW_TYPE_ZOOM)
        {
            CheckableView checkableView = new CheckableView(context);
            ImageView imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            checkableView.setContentView(imageView);

//            View view = LayoutInflater.from(context).inflate(R.layout.image_item, null);
            result = new ImageHolder(checkableView);
        }
        return result;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        log.d("onBindViewHolder, position = " + position);
        long id = getItemId(position);
        int groupIndex = getGroupIndex(id);
        int childIndex = getChildIndex(id);
        if(childIndex == 0)
        {
            TitleHolder titleHolder = (TitleHolder)holder;
            String text = sortedKeys.get(groupIndex);
            titleHolder.textView.setText(position + text);
        }else
        {
            final ImageHolder imageHolder = (ImageHolder)holder;
            LinkedList<ImageModule> list = data.get(sortedKeys.get(groupIndex));
//            imageHolder.textView.setText(position + "");
            ImageModule imageModule = list.get(childIndex - 1);
//            imageHolder.imageView.setImageDrawable(drawable);
//            Object o = bitmapLruCache.get(imageModule.path);
//
//            if(o != null)
//            {
//                Bitmap bitmap = (Bitmap)o;
//                imageHolder.imageView.setImageBitmap(bitmap);
//            }else
//            {
//
//            }

            imageHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int mPosition = position;
                    layoutManager.onItemClicked(mPosition);
                }
            });

            imageHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    final int mPosition = position;
                    layoutManager.onItemLongClicked(mPosition);
                    return true;
                }
            });

            Glide.clear(imageHolder.imageView);
            Glide.with(context)
                    .load(imageModule.path)
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .override(200, 200)
//                    .thumbnail(0.1f)
                    .into(imageHolder.imageView);
        }
//        checkAndPreload(position);
    }



    @Override
    public int getItemCount() {
        return itemCount;
    }

    @Override
    public int getItemViewType(int position) {
        long id = getItemId(position);
        int childIndex = getChildIndex(id);
        if(childIndex == 0)
        {
            return VIEW_TYPE_UNZOOM;
        }else
        {
            return VIEW_TYPE_ZOOM;
        }
    }

    @Override
    public long getItemId(int position) {
        int mPosition = position;
        long group = 0;
        for(String key : sortedKeys)
        {
            if(mPosition >= data.get(key).size() + 1)
            {
                group += 1;
                mPosition -= data.get(key).size() + 1;
            }else
            {
                break;
            }
        }
        long id = (group << 32) | mPosition;
        return id;
    }

    @Override
    public int getGroupIndex(long id)
    {
        return (int)((id & 0xffffffff00000000l) >> 32);
    }

    @Override
    public int getChildIndex(long id)
    {

        return (int)(id & 0x00000000ffffffffl);
    }

    @Override
    public boolean isGrouped() {
        return true;
    }

    @Override
    public int getChildCount(int groupIndex) {
        String key = sortedKeys.get(groupIndex);
        return data.get(key).size();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }



    private class TitleHolder extends RecyclerView.ViewHolder
    {
        public TextView textView;
        public TitleHolder(View itemView) {
            super(itemView);
            textView = (TextView)itemView.findViewById(R.id.TitleItem_textView);
        }
    }

    private class ImageHolder extends RecyclerView.ViewHolder
    {
        public ImageView imageView;


        public ImageHolder(com.zu.customview.view.AlbumListView.CheckableView itemView) {
            super(itemView);
            imageView = (ImageView) itemView.getContentView();
        }
    }

}
