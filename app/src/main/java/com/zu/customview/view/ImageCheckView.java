package com.zu.customview.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v7.view.menu.MenuAdapter;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.Scroller;


/**
 * Created by zu on 17-6-2.
 */

public class ImageCheckView extends ImageView{

    private final String TAG = "ImageCheckView";

    private float[] matrixValues = new float[9];
    private Matrix mMatrix = new Matrix();
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private Scroller mScroller;


    private int downX, downY, newX, newY, oldX, oldY, dx, dy;
    private int newCenterX, newCenterY, oldCenterX, oldCenterY;
    private AnimateZoomRunnable animateZoomRunnable = null;

    private boolean FLING_STOP = false;


    private float MAX_SCALE = 4.0f;
    private float initScale = 1.0f;

    private int touchSlop = 5;
    private float scaleSlop = 0.01f;

    private boolean once = true;
    private boolean firstZoomCenterDetected = false;





    private GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener(){
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.v(TAG, "onDoubleTap");
            boolean consumed = false;
//            Matrix matrix = getImageMatrix();
//            float[] values = new float[9];
            float currentScale = getMatrixValue(Matrix.MSCALE_X);
            float targetScale;
            if(currentScale <= initScale)
            {

                targetScale = MAX_SCALE;
            }else
            {
                targetScale = initScale;
            }
            int x = (int)e.getX();
            int y = (int)e.getY();
            RectF rectF = getMatrixRect();
            if(x > (int)rectF.right)
            {
                x = (int)rectF.right;
            }else if(x < (int)rectF.left)
            {
                x = (int)rectF.left;
            }

            if(y > (int)rectF.bottom)
            {
                y = (int)rectF.bottom;
            }else if(y < (int)rectF.top)
            {
                y = (int)rectF.top;
            }

            if(animateZoomRunnable != null)
            {
                animateZoomRunnable.stop();
                animateZoomRunnable = null;
            }
            animateZoomRunnable = new AnimateZoomRunnable(x, y, currentScale, targetScale, 20);

            post(animateZoomRunnable);
            return consumed;
        }

//        @Override
//        public boolean onDown(MotionEvent e) {
//            Log.v(TAG, "onDown");
//            newX = (int)e.getX();
//            newY = (int)e.getY();
//            downX = newX;
//            downY = newY;
//            if(mScroller != null)
//            {
//                mScroller.forceFinished(true);
//            }
//            zoomed = false;
//            firstZoomCenterDetected = false;
//            return true;
//        }



        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//            Log.v(TAG, "onScroll");
//            oldX = newX;
//            oldY = newY;
//            newX = (int)e2.getX();
//            newY = (int)e2.getY();
//            dx = newX - oldX;
//            dy = newY - oldY;
//            Log.v(TAG, "dx = " + dx + ", dy = " + dy);
////            return scrollPic((int)distanceX, (int)distanceY);
//            return scrollPic(dx, dy);
            return false;

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.v(TAG, "onFling");
            flingPic(velocityX, velocityY);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
        }
    };

    private ScaleGestureDetector.OnScaleGestureListener scaleGestureListener = new ScaleGestureDetector.OnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            Log.v(TAG, "onScale");
            int x = (int)detector.getFocusX();
            int y = (int)detector.getFocusY();
            float scaleFactor = detector.getScaleFactor();
            return zoomPic(x, y, scaleFactor);
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

        }
    };

    private boolean zoomed = false;



    private OnTouchListener onTouchListener = new OnTouchListener() {
        private long lastId = 0;
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            boolean consumed = false;
            consumed |= gestureDetector.onTouchEvent(event);

            if(event.getPointerCount() >= 2)
            {
                consumed |= scaleGestureDetector.onTouchEvent(event);
            }
            switch (event.getActionMasked())
            {
                case MotionEvent.ACTION_DOWN:
                    Log.v(TAG, "ACTION_DOWN");
                    consumed = true;
//                    getParent().requestDisallowInterceptTouchEvent(true);
                    newX = (int)event.getX();
                    newY = (int)event.getY();
                    downX = newX;
                    downY = newY;
                    if(mScroller != null)
                    {
                        mScroller.forceFinished(true);
                    }
                    zoomed = false;
                    firstZoomCenterDetected = false;
                    break;

                case MotionEvent.ACTION_MOVE:
                    long id = event.getPointerId(0);
                    if(id != lastId)
                    {
                        newX = (int)event.getX();
                        newY = (int)event.getY();
                        lastId = id;
                    }
                    oldX = newX;
                    oldY = newY;
                    newX = (int)event.getX();
                    newY = (int)event.getY();
                    dx = newX - oldX;
                    dy = newY - oldY;
                    int tempX = newX - downX;
                    int tempY = newY - downY;

                    if(Math.abs(tempX) < touchSlop && Math.abs(tempY) < touchSlop)
                    {
                        //even a single tap will contains some ACTION_MOVE, this step is to avoid that
                        consumed = true;
                    }else if(Math.abs(dx) >= touchSlop || Math.abs(dy) >= touchSlop)
                    {
                        consumed |= scrollPic(dx, dy);
                        if(!consumed)
                        {
                            int left = getPaddingLeft();
                            int right = getWidth() - getPaddingRight();
                            RectF rectF = getMatrixRect(mMatrix);
                            if((dx > 0 && (int)rectF.left >= left) || (dx < 0 && (int)rectF.right <= right))
                            {

                            }else
                            {
                                consumed = true;
                            }
                        }
                    }else
                    {
                        //if neither dx nor dy is bigger than touchSlop, we may scroll picture later. So we
                        //still consume this event.
                        consumed = true;
                    }
                    break;

                default:
                    break;
            }
            /*
             * If all gesture detectors return false, we still can not return false easily. Because user may
             * do that careless. For example, user may want to check the right of image and and scroll image to left over scroll
             * beyond edge careless, gesture detectors return false. However, user doing this do not mean he want to switch image. He may check other
             * parts and scroll image to right later. If we just return false, we can not receive later events of this event queue
             * so that user's scrolling back will switch to other image instead scrolling this image.
             * There are two cases when we should return false.
             * 1. Image is in initScale now. It means image now can not be scrolled.
             * 2. User scroll over half of our width. He may really want to switch image.
             * Attention that when consumed is true, we do not deal with that, cause gesture detectors consume this event.
             * If not, it will cause gesture detectors lose efficacy.
            * */
            if(!consumed)
            {
                float scale = getMatrixValue(Matrix.MSCALE_X);
                int tempX = (int)event.getX();
                int tempY = (int)event.getY();
                if(scale == initScale || (Math.abs(tempX - downX) > getWidth() / 3 && Math.abs(tempY - downY) < Math.abs(tempX - downX) / 2))
                {
                    consumed = false;
                }else
                {
                    consumed = true;
                }
            }



            return consumed;
        }
    };



    private ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if(once)
            {
                if(getDrawable() == null)
                {
                    return;
                }

                int imgWidth = getDrawable().getIntrinsicWidth();
                int imgHeight = getDrawable().getIntrinsicHeight();

                float scale = 1.0f;
                if(imgWidth > getWidth() && imgHeight <= getHeight())
                {
                    scale = getWidth() * 1.0f/ imgWidth;
                }else if(imgWidth <= getWidth() && imgHeight > getHeight())
                {
                    scale = getHeight() * 1.0f/ imgHeight;
                }else if(imgWidth > getWidth() && imgHeight > getHeight())
                {
                    float temp1 = getHeight() * 1.0f / imgHeight;
                    float temp2 = getWidth() * 1.0f / imgWidth;
                    scale = Math.min(temp1, temp2);
                }

                initScale = scale;
                mMatrix.postTranslate((getWidth() - imgWidth) / 2, (getHeight() - imgHeight) / 2);
                mMatrix.postScale(scale, scale, getWidth() / 2, getHeight() / 2);
                setImageMatrix(mMatrix);
                once = false;

            }
        }
    };

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
    }

    public ImageCheckView(Context context) {
        this(context, null);
    }

    public ImageCheckView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageCheckView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ImageCheckView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        super.setScaleType(ScaleType.MATRIX);
        scaleGestureDetector = new ScaleGestureDetector(context, scaleGestureListener);
        gestureDetector = new GestureDetector(context, gestureListener);

        setOnTouchListener(onTouchListener);
    }

    public void resetScaleWithAnim()
    {
        int centerX = (getWidth() - getPaddingLeft() - getPaddingRight()) / 2;
        int centerY = (getHeight() - getPaddingTop() - getPaddingBottom()) / 2;
        float currentScale = getMatrixValue(Matrix.MSCALE_X);
        float targetScale = initScale;
        if(animateZoomRunnable != null)
        {
            animateZoomRunnable.stop();
            animateZoomRunnable = null;
        }
        animateZoomRunnable = new AnimateZoomRunnable(centerX, centerY, currentScale, targetScale, 20);

        post(animateZoomRunnable);
    }

    public void resetScale()
    {
        int centerX = (getWidth() - getPaddingLeft() - getPaddingRight()) / 2;
        int centerY = (getHeight() - getPaddingTop() - getPaddingBottom()) / 2;
        float scale = (initScale / getMatrixValue(Matrix.MSCALE_X));
        zoomPic(centerX, centerY, scale);
    }

    private boolean scrollPic(int dx, int dy)
    {
        boolean consumed = false;

        RectF rectF1 = getMatrixRect(mMatrix);
//        int dx1 = Math.abs(dx) >= touchSlop ? dx : 0;
//        int dy1 = Math.abs(dy) >= touchSlop ? dy : 0;
        int dx1 = dx;
        int dy1 = dy;
        if(dx1 != 0)
        {
            if((int)rectF1.width() > getWidth())
            {
                if(dx1 + (int)rectF1.left > 0)
                {
                    dx1 = -(int)rectF1.left;
                }else if(dx1 + (int)rectF1.right < getWidth())
                {
                    dx1 = getWidth() - (int)rectF1.right;
                }
            }else
            {
                dx1 = 0;
            }
        }

        if(dy1 != 0)
        {
            if((int)rectF1.height() > getHeight())
            {
                if(dy1 + (int)rectF1.top > 0)
                {
                    dy1 = -(int)rectF1.top;
                }else if(dy1 + (int)rectF1.bottom < getHeight())
                {
                    dy1 = getHeight() - (int)rectF1.bottom;
                }
            }else
            {
                dy1 = 0;
            }
        }

        if(dx1 != 0 || dy1 != 0)
        {
            mMatrix.postTranslate(dx1, dy1);
            setImageMatrix(mMatrix);
            consumed = true;
        }else
        {
            consumed = false;
        }



        return consumed;
    }

    private boolean zoomPic(int centerX, int centerY, float scaleFactor)
    {
        boolean consumed = false;
//        Matrix matrix = getImageMatrix();

        RectF rectF1 = getMatrixRect(mMatrix);

        float scale = getMatrixValue(Matrix.MSCALE_X);
        float mScaleFactor = scaleFactor;
        if((mScaleFactor > 1.0f && scale < MAX_SCALE) || (mScaleFactor < 1.0f && scale > initScale))
        {
            if(scale * mScaleFactor > MAX_SCALE)
            {
                mScaleFactor = MAX_SCALE / scale;
            }else if(scale * mScaleFactor < initScale)
            {
                mScaleFactor = initScale /scale;
            }

            mMatrix.postScale(mScaleFactor, mScaleFactor, centerX, centerY);
            checkScaleEdge(mMatrix);

            setImageMatrix(mMatrix);
            consumed = true;
        }else if((scale <= initScale && mScaleFactor < 1.0f) || (scale >= MAX_SCALE && mScaleFactor > 1.0f))
        {
            return false;
        }else if(mScaleFactor == 1.0f)
        {
            return true;
        }
        return consumed;
    }

    private void flingPic(float velocityX, float velocityY)
    {
        if(mScroller == null)
        {
            mScroller = new Scroller(getContext());
        }
        RectF rectF = getMatrixRect();
        FLING_STOP = false;
        newX = (int)rectF.left;
        newY = (int)rectF.top;
        mScroller.fling(newX, newY, (int)velocityX, (int)velocityY, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    private void checkScaleEdge(Matrix matrix)
    {

        RectF rectF = getMatrixRect(matrix);
        int offsetX = 0;
        int offsetY = 0;
        if(rectF.height() >= getHeight())
        {
            if(rectF.top > 0)
            {
                offsetY = -(int)rectF.top;
            }else if(rectF.bottom < getHeight())
            {
                offsetY = getHeight() - (int)rectF.bottom;
            }
        }else
        {
            int centerY = getHeight() / 2;
            int imgCenterY = (int)rectF.centerY();
            offsetY = centerY - imgCenterY;
        }

        if(rectF.width() >= getWidth())
        {
            if(rectF.left > 0)
            {
                offsetX = -(int)rectF.left;
            }else if(rectF.right < getWidth())
            {
                offsetX = getWidth() - (int)rectF.right;
            }
        }else
        {
            int centerX = getWidth() / 2;
            int imgCenterX = (int)rectF.centerX();
            offsetX = centerX - imgCenterX;
        }

        if(offsetX != 0 || offsetY != 0)
        {
            matrix.postTranslate(offsetX, offsetY);
        }

    }



    private RectF getMatrixRect()
    {
        return getMatrixRect(mMatrix);
    }

    private RectF getMatrixRect(Matrix matrix)
    {
        RectF rectF = new RectF(0, 0,getDrawable().getIntrinsicWidth(), getDrawable().getIntrinsicHeight());
        matrix.mapRect(rectF);
        return rectF;
    }

    @Override
    public void computeScroll() {
        if(mScroller == null || FLING_STOP)
        {
            return;
        }
        if(mScroller.computeScrollOffset())
        {
            oldX = newX;
            oldY = newY;
            newX = mScroller.getCurrX();
            newY = mScroller.getCurrY();
            boolean scrolled = scrollPic(newX - oldX, newY - oldY);
            if(!scrolled)
            {
                FLING_STOP = true;
            }
            invalidate();
        }

    }

    private float getMatrixValue(int index)
    {
        float[] values = getMatrixValues();
        return values[index];

    }

    private float[] getMatrixValues()
    {
        float[] values = new float[9];
        mMatrix.getValues(values);
        return values;
    }

    private class AnimateZoomRunnable implements Runnable
    {
        private int centerX, centerY;
        private float srcScale, destScale;
        private int duration = 1000;

        private boolean stop = false;

        private boolean init = false;

        private float dScale = 0f;

        private long startTime = 0l;

        public AnimateZoomRunnable(int centerX, int centerY, float srcScale, float destScale, int speed) {
            this.centerX = centerX;
            this.centerY = centerY;
            this.srcScale = srcScale;
            this.destScale = destScale;

            float temp = (destScale - srcScale) / speed;
            dScale = 1.0f + temp;
        }

        public void stop()
        {
            stop = true;
        }

        @Override
        public void run() {
//            if(!init)
//            {
//                startTime = System.currentTimeMillis();
//            }
//            long currentTime = System.currentTimeMillis();
//            int dTime = (int)(currentTime - startTime);
//            if(dTime > duration || stop)
//            {
//                return;
//            }
            if(zoomPic(centerX, centerY, dScale))
            {
                post(this);
            }
        }
    }
}
