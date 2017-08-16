package com.zu.customview.GestureDetector;

import android.view.MotionEvent;

/**
 * Created by zu on 17-6-7.
 */

public class CustomGestureDetector {
    private OnCustomGestureListener mListener;

    public static abstract class OnCustomGestureListener
    {
        public boolean onClick(MotionEvent event){
            return false;
        }

        boolean onDoubleTap(MotionEvent event)
        {
            return false;
        }

        boolean onScroll(int dx, int dy)
        {
            return false;
        }

        boolean onFling(float velocityX, float velocityY)
        {
            return false;
        }

        boolean onEventReceived(MotionEvent event)
        {
            return false;
        }
    }
}
