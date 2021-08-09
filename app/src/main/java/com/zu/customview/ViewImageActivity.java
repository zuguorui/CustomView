package com.zu.customview;

import android.graphics.Color;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

import com.zu.customview.view.ImageCheckView;
import com.zu.customview.view.ImageSwitchView;

public class ViewImageActivity extends AppCompatActivity {

    private ImageSwitchView imageSwitchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);
        imageSwitchView = (ImageSwitchView)findViewById(R.id.ViewImage_imageSwitchView);
        createDebugView();
    }
    private void createDebugView()
    {
        BaseAdapter adapter = new BaseAdapter() {
            int count = 10;
            @Override
            public int getCount() {
                return count;
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(position < 0 || position >= count)
                {
                    return null;
                }
                if(convertView == null)
                {
                    ImageCheckView i = new ImageCheckView(ViewImageActivity.this);
                    i.setImageResource(R.drawable.chuyin);
                    convertView = i;
                }
                return convertView;

            }
        };
        imageSwitchView.setAdapter(adapter);
    }
}
