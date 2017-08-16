package com.zu.customview.view.AlbumListView;

import android.support.v7.widget.RecyclerView;

/**
 * Created by zu on 17-7-10.
 */

public abstract class ImageAdapter extends RecyclerView.Adapter {
    protected ZoomLayoutManager layoutManager = null;

    public void setZoomLayoutManager(ZoomLayoutManager layoutManager)
    {
        this.layoutManager = layoutManager;
    }

    public ZoomLayoutManager getLayoutManager()
    {
        return layoutManager;
    }
    public static final int VIEW_TYPE_UNZOOM = 1;
    public static final int VIEW_TYPE_ZOOM = 2;
    public abstract boolean isGrouped();
    public abstract int getGroupIndex(long id);
    public abstract int getChildIndex(long id);
    public abstract int getChildCount(int groupIndex);
}
