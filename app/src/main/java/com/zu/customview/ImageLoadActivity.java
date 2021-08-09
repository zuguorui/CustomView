package com.zu.customview;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zu.customview.module.ImageModule;


import com.zu.customview.temp.AlbumListView;
import com.zu.customview.view.AlbumListView.AlbumListViewAdapter;
import com.zu.customview.view.AlbumListView.AlbumListViewTwo;
import com.zu.customview.view.AlbumListView.ImageGroupedAdapter;
import com.zu.customview.view.AlbumListView.ImageNoGroupedAdapter;
import com.zu.customview.view.AlbumListView.ZoomLayoutManager;
import com.zu.customview.view.CheckableView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ImageLoadActivity extends AppCompatActivity {

    private MyLog log = new MyLog("ImageLoadActivity", true);
    private AlbumListViewTwo albumListView;
    private GridView gridView;
    private RecyclerView recyclerView;
    private ImageGroupedAdapter imageGroupedAdapter;
    private ImageNoGroupedAdapter imageNoGroupedAdapter;
    private ZoomLayoutManager zoomLayoutManager;
    private HashMap<String, LinkedList<ImageModule>> imgMap = new HashMap<>();
    private LinkedList<ImageModule> imgList = new LinkedList<>();
    private Drawable errorDrawable;
    private Drawable readDrawble;
    private String TAG = "ImageLoadActivity";
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what)
            {
                case 0:
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
        setContentView(R.layout.activity_image_load);

//        albumListView = (AlbumListViewTwo) findViewById(R.id.ImageLoadActivity_albumListView);
//        gridView = (GridView)findViewById(R.id.ImageLoadActivity_gridView);

        errorDrawable = new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.chuyin));
        readDrawble = new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.heiyi));
        readImg();
        readImgNoGrouped();
//        albumListView.setAdapter(groupedAdapter);
//        albumListView.setZoomLevel(4);
//        albumListView.setAdapter(groupedAdapter);
//        gridView.setAdapter(noGroupedAdapter);


        recyclerView = (RecyclerView)findViewById(R.id.ImageLoadActivity_recyclerView);
        imageGroupedAdapter = new ImageGroupedAdapter(this, imgMap);
        zoomLayoutManager = new ZoomLayoutManager(this, 4, 6, 2, imageGroupedAdapter);
        recyclerView.setOnTouchListener(zoomLayoutManager.getZoomOnTouchListener());
        recyclerView.setLayoutManager(zoomLayoutManager);
        recyclerView.setAdapter(imageGroupedAdapter);
        zoomLayoutManager.setOnItemCheckedListener(new ZoomLayoutManager.OnItemCheckedListener() {
            @Override
            public void onItemChecked(int[] checkedItemPosition) {
                Toast.makeText(ImageLoadActivity.this, "checked count = " + checkedItemPosition.length, Toast.LENGTH_SHORT).show();
            }
        });
        zoomLayoutManager.setOnItemClickListener(new ZoomLayoutManager.OnItemClickListener() {
            @Override
            public void onItemClicked(int position) {
                Toast.makeText(ImageLoadActivity.this, "clicked position = " + position, Toast.LENGTH_SHORT).show();
            }
        });

        zoomLayoutManager.setOnItemLongClickListener(new ZoomLayoutManager.OnItemLongClickListener() {
            @Override
            public void onItemLongClicked(int position) {
                Toast.makeText(ImageLoadActivity.this, "long clicked position = " + position, Toast.LENGTH_SHORT).show();
                zoomLayoutManager.setMultiCheckMode(true);
                zoomLayoutManager.addCheckedItems(new int[]{position});
            }
        });

//        imageNoGroupedAdapter = new ImageNoGroupedAdapter(this, imgList);
//        recyclerView.setLayoutManager(new GridLayoutManager(this, 6));
//        recyclerView.setAdapter(imageNoGroupedAdapter);
    }

    private void readImg()
    {
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        while(cursor.moveToNext())
        {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
            String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
            String parent = null;
            if(name != null && !"".equals(name))
            {
                parent = new File(path).getParent();
            }
            if(parent != null)
            {
                LinkedList<ImageModule> list = imgMap.get(parent);
                if(list == null)
                {
                    list = new LinkedList<>();
                    imgMap.put(parent, list);
                }
                ImageModule imageModule = new ImageModule(path, 0, 0, parent, false);
                list.add(imageModule);
            }
        }
    }

    private void readImgNoGrouped()
    {
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        while(cursor.moveToNext())
        {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
            String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
            ImageModule imageModule = new ImageModule(path, 0, 0, null, false);
            imgList.add(imageModule);
        }
    }

//    private AlbumListViewAdapter noGroupedAdapter = new AlbumListViewAdapter() {
//        @Override
//        public int getGroupCount() {
//            return 0;
//        }
//
//        @Override
//        public int getChildrenCount(long groupId) {
//            return imgList.size();
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
//        public AlbumListView.VIEW_TYPE getViewType(long id) {
//            return AlbumListView.VIEW_TYPE.ZOOM;
//        }
//
//        @Override
//        public int getCount() {
//            return imgList.size();
//        }
//
//        @Override
//        public Object getItem(int position) {
//            if(position < 0 || position >= imgList.size())
//            {
//                return null;
//            }else
//            {
//                return imgList.get(position);
//            }
//
//        }
//
//        @Override
//        public long getItemId(int position) {
//            return position;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            if(position < 0 || position >= imgList.size())
//            {
//                return null;
//            }
//            if(convertView == null)
//            {
//                CheckableView checkableView = new CheckableView(ImageLoadActivity.this);
//                ImageView imageView = new ImageView(ImageLoadActivity.this);
//                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//                checkableView.setContentView(imageView);
//                convertView = checkableView;
//            }
//            ImageView imageView = (ImageView) ((CheckableView)convertView).getContentView();
//            String path = imgList.get(position);
//            Glide.with(ImageLoadActivity.this)
//                    .load(new File(path))
//                    .asBitmap()
//                    .override(500, 500)
//                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
//                    .into(imageView);
//            return convertView;
//
//        }
//    };

    private AlbumListViewAdapter groupedAdapter = new AlbumListViewAdapter() {
//        @Override
//        public int getGroupCount() {
//            return imgMap.size();
//        }
//
//        @Override
//        public int getChildrenCount(long groupId) {
//            int mGroupId = (int)groupId;
//            String folder = null;
//            Set<String> keys = imgMap.keySet();
//            for(String key : keys)
//            {
//                if(groupId == 0)
//                {
//                    folder = key;
//                    break;
//                }
//                groupId--;
//
//            }
//            return imgMap.get(folder).size() + 1;
//        }

        @Override
        public int getGroupIndex(long childId) {
            return (int)((childId & 0xffffffff00000000l) >> 32);
        }

        @Override
        public int getChildIndex(long childId) {
            return (int)(childId & 0x00000000ffffffffl);
        }

//        @Override
//        public int getPosition(long id) {
//            int group = (int)getGroupId(id);
//            int child = getChildPositionInGroup(id);
//            int count = 0;
//            Set<String> keys = imgMap.keySet();
//            int i = 0;
//            for(String key : keys)
//            {
//                if(i == group)
//                {
//                    break;
//                }
//                count += imgMap.get(key).size() + 1;
//                i++;
//
//            }
//            if(i != group)
//            {
//                return -1;
//            }
//            return count + child;
//        }

//        @Override
//        public long getChildIdInGroup(long groupId, int childPositionInGroup) {
//            return (groupId << 32) | childPositionInGroup;
//        }

//        @Override
//        public int getViewType(long id) {
//            int childPos = getChildPositionInGroup(id);
//            if(childPos == 0)
//            {
//                return AlbumListView.VIEW_TYPE.UNZOOM;
//            }else
//            {
//                return AlbumListView.VIEW_TYPE.ZOOM;
//            }
//        }


        @Override
        public int getItemViewType(int position) {
            int childPos = getChildIndex(getItemId(position));
            if(childPos == 0)
            {
                return VIEW_TYPE_UNZOOM;
            }else
            {
                return VIEW_TYPE_ZOOM;
            }
        }

        @Override
        public int getCount() {
            int count = 0;
            Set<String> keys = imgMap.keySet();
            for(String key : keys)
            {
                count += imgMap.get(key).size() + 1;
            }
            return count;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            int mPosition = position;
            long id = 0;
            long group = 0;
            Set<String> keys = imgMap.keySet();
            for(String key : keys)
            {
                List<ImageModule> value = imgMap.get(key);
                if(mPosition >= value.size() + 1)
                {
                    mPosition -= value.size() + 1;
                    group++;
                }else
                {
                    break;
                }
            }
            return (group << 32) | mPosition;
        }

        @Override
        public boolean isGrouped() {
            return true;
        }

        @Override
        public int getChildCount(int groupIndex) {

            String folder = null;
            Set<String> keys = imgMap.keySet();
            for(String key : keys)
            {
                if(groupIndex == 0)
                {
                    folder = key;
                    break;
                }
                groupIndex--;

            }
            return imgMap.get(folder).size() + 1;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(position > getCount())
            {
                return null;
            }
            long id = getItemId(position);
            if(getItemViewType(position) == VIEW_TYPE_UNZOOM)
            {
                if(convertView == null || !convertView.getClass().equals(TextView.class))
                {

                    TextView textView = new TextView(ImageLoadActivity.this);
                    textView.setTextColor(Color.BLACK);
                    textView.setPadding(0, 3, 0, 3);
                    ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    textView.setLayoutParams(layoutParams);
                    convertView = textView;
                    log.d("convert view is null");
                }else
                {
                    log.d("convert view is available");
                }
                Set<String> keys = imgMap.keySet();
                int groupId = getGroupIndex(id);
                String folder = null;
                if(keys.size() < groupId + 1)
                {
                    return null;
                }

                for(String key : keys)
                {
                    if(groupId == 0)
                    {
                        folder = key;
                        break;
                    }
                    groupId--;

                }
                ((TextView)convertView).setText(folder);
            }else
            {
                if(convertView == null || !convertView.getClass().equals(ImageView.class))
                {
                    ImageView imageView = new ImageView(ImageLoadActivity.this);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    convertView = imageView;
                    log.d("convert view is null");
                }else
                {
                    log.d("convert view is available");
                }
                Set<String> keys = imgMap.keySet();
                int groupId = (int)getGroupIndex(id);
                if(keys.size() < groupId + 1)
                {
                    return null;
                }
                int childPos = (int)getChildIndex(id);
                String folder = null;
                for(String key : keys)
                {
                    if(groupId == 0)
                    {
                        folder = key;
                        break;
                    }
                    groupId--;

                }

                LinkedList<ImageModule> imgs = imgMap.get(folder);
                final String path = imgs.get(childPos - 1).path;
                final ImageView recycleView = (ImageView) convertView;
                Glide.with(ImageLoadActivity.this)
                        .load(new File(path))
                        .override(200, 200)
                        .into(recycleView);

            }
            return convertView;
        }
    };


}
