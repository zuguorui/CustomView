package com.zu.customview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.zu.customview.R;


/**
 * Created by rikson on 2018/8/30.
 */

public class FreqSurfaceView extends SurfaceView implements SurfaceHolder.Callback{

    private static final String TAG = FreqSurfaceView.class.getSimpleName();

    private SurfaceHolder mSurfaceHolder;
    private Canvas mCanvas;
    private boolean drawContent = false;

    private long delayTime = 50;

    private int baseSpeed = 20;
    private int speedStep = 40;

    private int mainLineWidth = 5;
    private int shadowLineWidth = 2;

    private int mainLineColor = 0xffffff00;
    private int shadowLineColor = 0xff00ffff;

    private float shadowToMainScale = -0.5f;

    private Paint paint = new Paint();

    private Path mainPath = new Path(), shadowPath = new Path();


    private static final int SIN_LENGTH = 1000;

    public static short[] sinTable = new short[SIN_LENGTH];
    private static float[] ampFactor = new float[SIN_LENGTH];

    private int sinOffset = 0;

    private int period = 3;

    private float data = 0.0f;

    private long dataSetTime = -1;

    private float slop = 0.0f;

    private float currentAmp = 0.0f;


    static{
        for(int i = 0; i < SIN_LENGTH; i++)
        {
            float temp = (float)Math.cos(i * 1.0f / SIN_LENGTH * 2 * Math.PI);
            sinTable[i] = (short) (Short.MAX_VALUE * temp);
            ampFactor[i] = (1 - temp) / 2;
        }
    }


   // private int shadowCount = 3;

    //private LinkedList<Float> shadowScale = new LinkedList<>();
    //private ArrayList<Path> shadowPaths = new ArrayList<>();

    private DrawThread drawThread = null;

    public FreqSurfaceView(Context context) {
        this(context, null);
    }

    public FreqSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FreqSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public FreqSurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.FreqSurfaceView);
        baseSpeed = array.getInteger(R.styleable.FreqSurfaceView_baseSpeed, baseSpeed);
        speedStep = array.getInteger(R.styleable.FreqSurfaceView_speedStep, speedStep);
        mainLineWidth = array.getDimensionPixelSize(R.styleable.FreqSurfaceView_mainLineWidth, mainLineWidth);
        shadowLineWidth = array.getDimensionPixelSize(R.styleable.FreqSurfaceView_shadowLineWidth, shadowLineWidth);
        mainLineColor = array.getDimensionPixelSize(R.styleable.FreqSurfaceView_mainLineColor, mainLineColor);
        shadowLineColor = array.getDimensionPixelSize(R.styleable.FreqSurfaceView_shadowLineColor, shadowLineColor);
        period = array.getInteger(R.styleable.FreqSurfaceView_period, period);
        shadowToMainScale = array.getFloat(R.styleable.FreqSurfaceView_shadowToMainScale, shadowToMainScale);
        array.recycle();

//        for(int i = 0; i < shadowCount; i++)
//        {
//            shadowScale.addFirst(0f);
//            shadowPaths.add(new Path());
//        }
        initViews();

    }

    public void setDrawContent(boolean b)
    {
        drawContent = b;
    }




    private void initViews()
    {

        mSurfaceHolder = getHolder();
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        mSurfaceHolder.addCallback(this);

        setFocusable(true);
        setFocusableInTouchMode(true);

        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);

        //setZOrderMediaOverlay(true);
        //setZOrderOnTop(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");
        drawContent = true;
        drawThread = new DrawThread();
        drawThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed");
        drawContent = false;
        if(drawThread != null)
        {
            drawThread.interrupt();
            drawThread = null;
        }
    }

    public void setData(float d)
    {
        data = d;
        dataSetTime = System.currentTimeMillis();
        slop = (data - currentAmp) * 1.0f / delayTime;
    }

    private class DrawThread extends Thread{
        @Override
        public void run() {
            while(drawContent)
            {
                Log.d(TAG, "draw run");

                //float tempAmp = (random.nextInt(Short.MAX_VALUE * 2) - Short.MAX_VALUE) * 1.0f / Short.MAX_VALUE;
                long currentTime = System.currentTimeMillis();
                if(currentTime < dataSetTime + delayTime)
                {
                    currentAmp = data + (currentTime - dataSetTime - delayTime) * slop;
                }


                int left = getPaddingLeft();
                int right = getWidth() - getPaddingRight();
                int top = getPaddingTop();
                int bottom = getHeight() - getPaddingBottom();
                int center = (top + bottom) / 2;
                int a = (bottom - top) / 2;

                float widthScale = (right - left) * 1.0f / SIN_LENGTH;
                float heightScale = a * 1.0f / Short.MAX_VALUE;


                mainPath.reset();
                shadowPath.reset();

//                for(int i = 0; i < shadowCount; i++)
//                {
//                    shadowPaths.get(i).reset();
//                }

                int dataPos, ampPos;
                short point = 0, shadowPoint = 0;


                for(int i = 0; i < right - left; i++)
                {
                    dataPos = (int)((sinOffset + i * period / widthScale) % SIN_LENGTH);
                    ampPos = (int)(i / widthScale) % SIN_LENGTH;
                    shadowPoint = (short) (sinTable[dataPos] * heightScale * ampFactor[ampPos]);
                    point = (short) (sinTable[dataPos] * currentAmp * heightScale * ampFactor[ampPos]);
                    if(i == 0)
                    {
                        mainPath.moveTo(i + left, point + center);
//                        for(int j = 0; j < shadowCount; j++)
//                        {
//                            shadowPaths.get(j).moveTo(i + left, shadowPoint * shadowScale.get(j) + center);
//                        }
                        shadowPath.moveTo(i + left, point * shadowToMainScale + center);
                    }else{
                        mainPath.lineTo(i + left, point + center);
//                        for(int j = 0; j < shadowCount; j++)
//                        {
//                            shadowPaths.get(j).lineTo(i + left, shadowPoint * shadowScale.get(j) + center);
//                        }
                        shadowPath.lineTo(i + left, point * shadowToMainScale + center);

                    }

                }


                if(!drawContent)
                {
                    return;
                }
                mCanvas = mSurfaceHolder.lockCanvas();
                if(mCanvas == null)
                {
                    Log.e(TAG, "canvas is null");
                    continue;
                }
                mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);



                paint.setColor(shadowLineColor);
                paint.setStrokeWidth(shadowLineWidth);

//                for(int i = 0; i < shadowCount; i++)
//                {
//                    mCanvas.drawPath(shadowPaths.get(i), paint);
//                }
                mCanvas.drawPath(shadowPath, paint);

                paint.setColor(mainLineColor);
                paint.setStrokeWidth(mainLineWidth);
                mCanvas.drawPath(mainPath, paint);

//                shadowScale.addFirst(tempAmp);
//                shadowScale.removeLast();


                sinOffset += (int)(baseSpeed + speedStep * currentAmp);
                if(sinOffset >= SIN_LENGTH){
                    sinOffset = sinOffset % SIN_LENGTH;
                }
                mSurfaceHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public int getBaseSpeed() {
        return baseSpeed;
    }

    public void setBaseSpeed(int baseSpeed) {
        this.baseSpeed = baseSpeed;
    }

    public int getSpeedStep() {
        return speedStep;
    }

    public void setSpeedStep(int speedStep) {
        this.speedStep = speedStep;
    }

    public int getMainLineWidth() {
        return mainLineWidth;
    }

    public void setMainLineWidth(int mainLineWidth) {
        this.mainLineWidth = mainLineWidth;
    }

    public int getShadowLineWidth() {
        return shadowLineWidth;
    }

    public void setShadowLineWidth(int shadowLineWidth) {
        this.shadowLineWidth = shadowLineWidth;
    }

    public int getMainLineColor() {
        return mainLineColor;
    }

    public void setMainLineColor(int mainLineColor) {
        this.mainLineColor = mainLineColor;
    }

    public int getShadowLineColor() {
        return shadowLineColor;
    }

    public void setShadowLineColor(int shadowLineColor) {
        this.shadowLineColor = shadowLineColor;
    }

    public float getShadowToMainScale() {
        return shadowToMainScale;
    }

    public void setShadowToMainScale(float shadowToMainScale) {
        this.shadowToMainScale = shadowToMainScale;
    }
}
