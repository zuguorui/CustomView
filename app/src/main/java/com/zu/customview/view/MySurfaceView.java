package com.zu.customview.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by rikson on 2018/8/30.
 */

public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback{

    private SurfaceHolder mSurfaceHolder;
    private Canvas mCanvas;
    private boolean drawContent = false;

    private Boolean isDrawing = true;
    PorterDuffXfermode xfermode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

    private long start = 0;
    private float w = 0.05f;
    private DrawThread drawThread = null;

    public MySurfaceView(Context context) {
        this(context, null);
    }

    public MySurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MySurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MySurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initViews();

    }

    private void initViews()
    {

        mSurfaceHolder = getHolder();
        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        mSurfaceHolder.addCallback(this);
        setZOrderOnTop(true);
        setZOrderMediaOverlay(true);
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawContent = true;
        drawThread = new DrawThread();
        drawThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        drawContent = false;
        if(drawThread != null)
        {
            drawThread.interrupt();
            drawThread = null;
        }
    }

    private class DrawThread extends Thread{
        @Override
        public void run() {
            while(drawContent)
            {
                mCanvas = mSurfaceHolder.lockCanvas();
                if(mCanvas == null)
                {
                    Log.e("MySurfaceView", "canvas is null");
                    return;
                }
                mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                int left = getPaddingLeft();
                int right = getWidth() - getPaddingRight();
                int top = getPaddingTop();
                int bottom = getHeight() - getPaddingBottom();
                int center = (top + bottom) / 2;
                int a = (bottom - top) / 2;
                Paint paint = new Paint();
                paint.setColor(0xffff0000);
                paint.setStrokeWidth(5);

                for(int i = left; i < right; i++)
                {
                    int y = (int)(a * Math.sin(w * (start + i))) + a;
                    mCanvas.drawPoint(i, y, paint);
                }
                start++;
                mSurfaceHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }


}
