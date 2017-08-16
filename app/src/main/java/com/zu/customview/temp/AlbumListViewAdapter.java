package com.zu.customview.temp;

import android.widget.BaseAdapter;

/**
 * Created by zu on 17-5-17.
 */

public abstract class AlbumListViewAdapter extends BaseAdapter {
    public abstract int getGroupCount();
    public abstract int getChildrenCount(long groupId);
    public abstract long getGroupId(long childId);
    public abstract int getChildPositionInGroup(long childId);
    public abstract int getPosition(long id);
    public abstract long getChildIdInGroup(long groupId, int childPositionInGroup);
    public abstract AlbumListView.VIEW_TYPE getViewType(long id);
}
