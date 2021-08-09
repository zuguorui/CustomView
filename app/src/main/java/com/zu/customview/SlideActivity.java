package com.zu.customview;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.zu.customview.view.SlideLayout;


public class SlideActivity extends AppCompatActivity {
    private SlideLayout slideLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide);
        slideLayout = (SlideLayout)findViewById(R.id.activity_main);
    }
}
