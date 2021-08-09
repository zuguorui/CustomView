package com.zu.customview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.zu.customview.view.ImageCheckView;

public class ImageCheckActivity extends AppCompatActivity {

    private ImageCheckView imageCheckView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_check);

        imageCheckView = (ImageCheckView)findViewById(R.id.ImageCheck_imageView);
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.chuyin);
        imageCheckView.setImageResource(R.drawable.chuyin);
    }
}
