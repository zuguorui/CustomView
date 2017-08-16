package com.zu.customview.view;

import android.widget.BaseAdapter;

/**
 * Created by zu on 17-5-2.
 */

public abstract class ItemAdapter extends BaseAdapter{
    public abstract Object getItem(int position, ZoomView.ItemManager.Callback callback);
}
