package com.zu.customview;

import android.content.res.Configuration;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.zu.customview.view.AlbumListView.AlbumListViewAdapter;
import com.zu.customview.view.AlbumListView.AlbumListViewTwo;
import com.zu.customview.view.AlbumListView.CheckableItem;
import com.zu.customview.view.CheckableView;

public class AlbumListTest extends AppCompatActivity implements View.OnClickListener{


    private AlbumListViewTwo albumListView;
    private Button increaseZoomLevelButton;
    private Button decreaseZoomLevelButton;
    private Button dataChangedButton;
    private AlbumListViewAdapter adapter = null;



    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what)
            {
                case 0:
                    Toast.makeText(AlbumListTest.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;

                default:
                    break;
            }
            return true;
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_list_test);
        Log.v("AlbumListTest", "onCreate");
        albumListView = (AlbumListViewTwo) findViewById(R.id.AlbumListView_albumList);
        increaseZoomLevelButton = (Button)findViewById(R.id.AlbumListView_button_increaseZoomLevel);
        increaseZoomLevelButton.setOnClickListener(this);
        decreaseZoomLevelButton = (Button)findViewById(R.id.AlbumListView_button_decreaseZoomLevel);
        decreaseZoomLevelButton.setOnClickListener(this);
        dataChangedButton = (Button)findViewById(R.id.AlbumListView_button_dataSetChanged);
        dataChangedButton.setOnClickListener(this);
        createDebugData();
        albumListView.addOnItemLongClickListener(new AlbumListViewTwo.OnItemLongClickListener() {
            @Override
            public void onLongClick(View view, final int position, long id) {
                Message message = Message.obtain(mHandler, 0);
                message.obj = position + "long clicked";
                message.sendToTarget();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        albumListView.setMultiCheckMode(true);
                        albumListView.setCheckedItems(new int[]{position});
                    }
                });

            }
        });
        albumListView.addOnItemClickListener(new AlbumListViewTwo.OnItemClickListener() {
            @Override
            public void onClick(View view, int position, long id) {
                Message message = Message.obtain(mHandler, 0);
                message.obj = position + "clicked";
                message.sendToTarget();
            }
        });

    }


    private void createNoGroupedData()
    {

    }

    private void createDebugData()
    {
//        adapter = noGroupAdapter;
        albumListView.setAdapter(adapter);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.AlbumListView_button_increaseZoomLevel:
                albumListView.setZoomLevel((int)albumListView.getZoomLevel() + 1);
                break;
            case R.id.AlbumListView_button_decreaseZoomLevel:
                albumListView.setZoomLevel((int)albumListView.getZoomLevel() - 1);
                break;
            case R.id.AlbumListView_button_dataSetChanged:
                adapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
    }

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
//        {
////            Toast.makeText(this, "屏幕竖向", Toast.LENGTH_SHORT).show();
//            Log.v("AlbumListTest", "屏幕竖向");
//        }else if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
//        {
////            Toast.makeText(this, "屏幕横向", Toast.LENGTH_SHORT).show();
//            Log.v("AlbumListTest", "屏幕横向");
//        }else
//        {
//            Log.v("AlbumListTest", "onConfigurationChanged");
//        }
//    }
//
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        int first = albumListView.getOnScreenFirstItemPosition();
//        int maxWidth = albumListView.getItemMaxWidth();
//        int minWidth = albumListView.getItemMinWidth();
//        float zoomLevel = albumListView.getZoomLevel();
//        outState.putInt("first", first);
//        outState.putInt("maxWidth", maxWidth);
//        outState.putInt("minWidth", minWidth);
//        outState.putFloat("zoomLevel", zoomLevel);
//        Log.v("AlbumListTest", " onSaveInstanceState");
//    }
//
//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        int first = savedInstanceState.getInt("first");
//        int maxWidth = savedInstanceState.getInt("maxWidth");
//        int minWidth = savedInstanceState.getInt("minWidth");
//        float zoomLevel = savedInstanceState.getFloat("zoomLevel");
//        if(first != -1)
//        {
//            albumListView.setPositionShow(first);
//
//        }
////        albumListView.setItemMaxWidth(maxWidth);
////        albumListView.setItemMinWidth(minWidth);
//        int orientation = getResources().getConfiguration().orientation;
//        if(orientation == Configuration.ORIENTATION_LANDSCAPE)
//        {
//            albumListView.setMaxZoomLevel(albumListView.getMaxZoomLevel() + 1);
//            albumListView.setZoomLevel((int)zoomLevel + 1);
//        }else
//        {
//            albumListView.setZoomLevel((int)zoomLevel - 1);
//        }
//
//        Log.v("AlbumListTest", " onRestoreInstanceState");
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v("AlbumListTest", "onDestroy");
    }

//    AlbumListViewAdapter groupedAdapter = new AlbumListViewAdapter() {
//        final int childCountInGroup = 23;
//        final int groupCount = 60;
//        @Override
//        public int getGroupCount() {
//            return groupCount;
//        }
//
//        @Override
//        public int getChildrenCount(long groupId) {
//            return childCountInGroup;
//        }
//
//        @Override
//        public long getGroupId(long childId) {
//            return (childId & 0xffffffff00000000l) >> 32;
//        }
//
//        @Override
//        public int getChildPositionInGroup(long childId) {
//            return (int)(childId & 0x00000000ffffffffl);
//
//        }
//
//        @Override
//        public int getPosition(long id) {
//            int groupPosition = (int)(getGroupId(id));
//            int childPosition = (int)(getChildPositionInGroup(id));
//
//            return groupPosition * childCountInGroup + childPosition;
//        }
//
//        @Override
//        public long getChildIdInGroup(long groupId, int childPositionInGroup) {
//            return (groupId << 32) | childPositionInGroup;
//        }
//
//        @Override
//        public AlbumListViewTwo.VIEW_TYPE getViewType(long id) {
//            int childId = getChildPositionInGroup(id);
//            if(childId == 0)
//            {
//                return AlbumListViewTwo.VIEW_TYPE.UNZOOM;
//            }else
//            {
//                return AlbumListViewTwo.VIEW_TYPE.ZOOM;
//            }
//        }
//
//        @Override
//        public int getCount() {
//            return groupCount * childCountInGroup;
//        }
//
//        @Override
//        public Object getItem(int position) {
//            return null;
//        }
//
//        @Override
//        public long getItemId(int position) {
//
//            long group = position / childCountInGroup;
//            long child = position % childCountInGroup;
//            return (group << 32) | child;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            int group = position / childCountInGroup;
//            int child = position % childCountInGroup;
//            if(position > childCountInGroup * groupCount)
//            {
//                return null;
//            }
//            if(child == 0)
//            {
//                if(convertView != null && convertView.getClass().equals(TextView.class))
//                {
//                    ((TextView) convertView).setText("position: " + position);
//                    return convertView;
//                }else
//                {
//                    TextView t = new TextView(AlbumListTest.this);
//                    t.setText("position: " + position);
//                    convertView = t;
//                }
//            }else
//            {
//                if(convertView != null && convertView instanceof CheckableItem)
//                {
//                    return convertView;
//                }else
//                {
////                        ImageView i = new ImageView(AlbumListTest.this);
////                        i.setBackgroundColor(Color.parseColor("#aaaaaa"));
//                    ImageView i = new ImageView(AlbumListTest.this);
//                    i.setImageResource(R.drawable.chuyin);
//                    i.setScaleType(ImageView.ScaleType.CENTER_CROP);
//                    CheckableView c = new CheckableView(AlbumListTest.this);
//                    c.setContentView(i);
//                    convertView = c;
//
//                }
//            }
//            return convertView;
//        }
//    };
//
//    AlbumListViewAdapter noGroupAdapter = new AlbumListViewAdapter() {
//        int count = 300;
//        @Override
//        public int getGroupCount() {
//            return 1;
//        }
//
//        @Override
//        public int getChildrenCount(long groupId) {
//            return count;
//        }
//
//        @Override
//        public long getGroupId(long childId) {
//            return 0;
//        }
//
//        @Override
//        public int getChildPositionInGroup(long childId) {
//            return (int)childId;
//        }
//
//        @Override
//        public int getPosition(long id) {
//            return (int)id;
//        }
//
//        @Override
//        public long getChildIdInGroup(long groupId, int childPositionInGroup) {
//            return childPositionInGroup;
//        }
//
//        @Override
//        public AlbumListViewTwo.VIEW_TYPE getViewType(long id) {
//            return AlbumListViewTwo.VIEW_TYPE.ZOOM;
//        }
//
//        @Override
//        public int getCount() {
//            return count;
//        }
//
//        @Override
//        public Object getItem(int position) {
//            return null;
//        }
//
//        @Override
//        public long getItemId(int position) {
//            return position;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            if(position >= count)
//            {
//                return null;
//            }
//            if(convertView == null)
//            {
////                Button b = new Button(AlbumListTest.this);
////                TextView t = new TextView(AlbumListTest.this);
////                t.setText(position + "");
////                t.setGravity(Gravity.CENTER);
//                ImageView i = new ImageView(AlbumListTest.this);
//                i.setImageResource(R.drawable.chuyin);
//                i.setScaleType(ImageView.ScaleType.CENTER_CROP);
//                i.setDrawingCacheEnabled(true);
//                CheckableView c = new CheckableView(AlbumListTest.this);
//                c.setContentView(i);
//                c.setDrawingCacheEnabled(true);
//                convertView = c;
//            }
////            CheckableView c = (CheckableView)convertView;
////            ((TextView)(c.getContentView())).setText(position + "");
////            ((TextView)convertView).setText(position + "");
//            return convertView;
//        }
//    };
}
