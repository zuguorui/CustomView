package com.zu.customview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;

public class LayoutParamTest extends AppCompatActivity implements View.OnClickListener{

    private Button biggerButton;
    private Button smallerButton;
    private ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_param_test);
        biggerButton = (Button)findViewById(R.id.LayoutParamTest_bigger);
        biggerButton.setOnClickListener(this);
        smallerButton = (Button)findViewById(R.id.LayoutParamTest_smaller);
        smallerButton.setOnClickListener(this);
        imageView = (ImageView)findViewById(R.id.LayoutParamTest_img);
        ViewGroup.LayoutParams layoutParams =imageView.getLayoutParams();
        Log.v("ImageView onCreate", "LayoutParams.width=" + layoutParams.width + ", LayoutParams.height=" + layoutParams.height);
        ViewTreeObserver viewTreeObserver = imageView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewGroup.LayoutParams layoutParams =imageView.getLayoutParams();
                Log.v("ImageView", "LayoutParams.width=" + layoutParams.width + ", LayoutParams.height=" + layoutParams.height);
                Log.v("ImageView", "getWidth()=" + imageView.getWidth() + ", getHeight()=" + imageView.getHeight());
            }
        });
//        imageView.post(new Runnable() {
//            @Override
//            public void run() {
//                ViewGroup.LayoutParams layoutParams =imageView.getLayoutParams();
//                Log.v("ImageView", "width=" + layoutParams.width + ", height=" + layoutParams.height);
//            }
//        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.LayoutParamTest_bigger:
                ViewGroup.LayoutParams layoutParams =imageView.getLayoutParams();
                layoutParams.width += 10;
                layoutParams.height += 10;
                imageView.setLayoutParams(layoutParams);
                break;
            case R.id.LayoutParamTest_smaller:
                ViewGroup.LayoutParams layoutParams1 =imageView.getLayoutParams();
                layoutParams1.width -= 10;
                layoutParams1.height -= 10;
                imageView.setLayoutParams(layoutParams1);
                break;
            default:
                break;
        }
    }
}
