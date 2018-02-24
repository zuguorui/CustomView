package com.zu.customview;

import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

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
//        root.setClickable(true);
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

        root.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.v("FrameLayoutTest", "root onTouchListener");
                Log.v("FrameLayoutTest", "action = " + event.getActionMasked());
                if (v.getId() == R.id.Frame_button_press)
                {
                    Log.v("FrameLayoutTest", "root onTouchListener, target is button");
                }else if(v.getId() == R.id.Frame_root)
                {
                    Log.v("FrameLayoutTest", "root onTouchListener, target is root");
                }else
                {
                    Log.v("FrameLayoutTest", "root onTouchListener");
                }
                return false;
            }
        });

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
                showPop();
                //check.setPressed(!check.isPressed());
                Log.v("FrameLayoutTest", "root is pressed = " + check.isPressed());
                break;
            default:
                break;
        }
    }

    private void showPop()
    {
        PopupWindow popupWindow = new PopupWindow(100,100);
        TextView textView = new TextView(this);
        textView.setText("測試");

        popupWindow.setContentView(textView);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0xffffeecc));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setAnimationStyle(R.style.PopVerAnim);
        //popupWindow.showAsDropDown(press,0,-press.getHeight(), Gravity.BOTTOM);
        popupWindow.showAtLocation(press, Gravity.TOP, 0, 0);


    }
}
