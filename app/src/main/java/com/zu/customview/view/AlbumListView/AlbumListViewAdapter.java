package com.zu.customview.view.AlbumListView;

import android.widget.BaseAdapter;

/**
 * Created by zu on 17-5-17.
 */

public abstract class AlbumListViewAdapter extends BaseAdapter {


    public static final int VIEW_TYPE_UNZOOM = 1;
    public static final int VIEW_TYPE_ZOOM = 2;
    public abstract boolean isGrouped();
    public abstract int getGroupIndex(long id);
    public abstract int getChildIndex(long id);
    public abstract int getChildCount(int groupIndex);
}
