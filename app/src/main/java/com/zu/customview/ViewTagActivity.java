package com.zu.customview;

import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;


import java.io.File;

public class ViewTagActivity extends AppCompatActivity {

    View item;
    private ImageView imageView;
    private Handler mHandler = new Handler();
    private String path = "sdcard/Pictures/Screenshots/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_tag);
        imageView = (ImageView)findViewById(R.id.glide_load_test);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try{
                    Thread.sleep(500);
                    Glide.with(ViewTagActivity.this).load(R.drawable.heiyi).into(imageView);
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

//        item = findViewById(R.id.my_list_item);
//        ViewHandler handler = new ViewHandler();
//        handler.fileIcon = (ImageView)item.findViewById(R.id.list_item_simple_file_icon);
//        handler.fileName = (TextView)item.findViewById(R.id.list_item_simple_file_name);
//        handler.fileIcon.setImageResource(R.drawable.folder_61d0ff_256);
//
//        handler.fileName.setText("fuck");
//        item.setTag(handler);
    }
}

class ViewHandler
{
    public ImageView fileIcon;
    public TextView fileName;
}
