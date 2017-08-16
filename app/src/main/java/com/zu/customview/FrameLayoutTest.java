package com.zu.customview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class FrameLayoutTest extends AppCompatActivity implements View.OnClickListener{

    FrameLayout root;
    Button press;
    Button check;
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frame_layout_test);
        root = (FrameLayout)findViewById(R.id.Frame_root);
        press = (Button)findViewById(R.id.Frame_button_press);
        check = (Button)findViewById(R.id.Frame_button_check);
        press.setOnClickListener(this);
        check.setOnClickListener(this);
        imageView = (ImageView)findViewById(R.id.Frame_imageView);
        imageView.setClickable(true);
        root.setClickable(true);
//        button.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                Log.v("FrameLayoutTest", "button onTouchListener");
//                return false;
//            }
//        });
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.v("FrameLayoutTest", "button onClickListener");
//            }
//        });

//        root.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                Log.v("FrameLayoutTest", "root onTouchListener");
//                if (v.getId() == R.id.Frame_button)
//                {
//                    Log.v("FrameLayoutTest", "root onTouchListener, target is button");
//                }else if(v.getId() == R.id.Frame_root)
//                {
//                    Log.v("FrameLayoutTest", "root onTouchListener, target is root");
//                }else
//                {
//                    Log.v("FrameLayoutTest", "root onTouchListener");
//                }
//                return false;
//            }
//        });

        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("FrameLayoutTest", "root onClickListener");
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.Frame_button_check:
                break;
            case R.id.Frame_button_press:
//                imageView.setPressed(true);
//                root.setPressed(!root.isPressed());
                check.setPressed(!check.isPressed());
                Log.v("FrameLayoutTest", "root is pressed = " + check.isPressed());
                break;
            default:
                break;
        }
    }
}
