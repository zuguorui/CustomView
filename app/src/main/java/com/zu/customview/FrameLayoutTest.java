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

import com.zu.customview.view.PopTip;

public class FrameLayoutTest extends AppCompatActivity implements View.OnClickListener{

    FrameLayout root;
    Button press;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frame_layout_test);

        press = (Button)findViewById(R.id.Frame_button_press);

        press.setOnClickListener(this);




    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {

            case R.id.Frame_button_press:
//                imageView.setPressed(true);
//                root.setPressed(!root.isPressed());
                showPop();
                //check.setPressed(!check.isPressed());
                //Log.v("FrameLayoutTest", "root is pressed = " + check.isPressed());
                break;
            default:
                break;
        }
    }

    private void showPop()
    {
//        PopupWindow popupWindow = new PopupWindow(100,100);
//        TextView textView = new TextView(this);
//        textView.setText("測試");
//
//        popupWindow.setContentView(textView);
//        popupWindow.setBackgroundDrawable(new ColorDrawable(0xffffeecc));
//        popupWindow.setOutsideTouchable(true);
//        popupWindow.setAnimationStyle(R.style.PopVerAnim);
//        //popupWindow.showAsDropDown(press,0,-press.getHeight(), Gravity.BOTTOM);
//        popupWindow.showAtLocation(press, Gravity.TOP, 0, 0);

        PopTip popTip = new PopTip();

        popTip.showAt(this, press, "该项为必填项", PopTip.EDGE.BOTTOM, PopTip.ALIGN.START);


    }
}
