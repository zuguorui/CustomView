package com.zu.customview.view;

import android.graphics.Color;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.zu.customview.R;

import org.w3c.dom.Text;

public class ZoomActivity extends AppCompatActivity {
    ZoomLayout zoomLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom);
        zoomLayout = (ZoomLayout) findViewById(R.id.ZoomActivity_zoomLayout);
        createDebugData();
    }

    private void createDebugData()
    {

        BaseAdapter baseAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return 1000;
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
                if(position >= 1000 || position < 0)
                {
                    return null;
                }else
                {
                    if(convertView == null)
                    {
                        convertView = new TextView(ZoomActivity.this);
                        ((TextView)convertView).setText("fuck you gallery");
//                        ((ImageView)convertView).setImageResource(R.drawable.chuyin);
//                        ((ImageView)convertView).setScaleType(ImageView.ScaleType.CENTER_CROP);
                    }
//                    ((TextView)convertView).setText(position + "");
                    return convertView;
                }
            }


        };
        zoomLayout.setAdapter(baseAdapter);
    }
}
