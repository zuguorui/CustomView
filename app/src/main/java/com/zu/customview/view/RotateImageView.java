package com.zu.customview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.zu.customview.R;

/**
 * Created by rickson on 2018/3/13.
 */

public class RotateImageView extends View{
    private Drawable src = null;
    private int backgroundColor = 0xff000000;
    private int backgroundStrokeWidth = 10;
    private int degree = 0;
    private int rotateSpeed = 10;
    private long lastDrawTime = -1;
    private boolean shouldRotate = true;


    public RotateImageView(Context context) {
        this(context, null);
    }

    public RotateImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RotateImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RotateImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.RotateImageView);
        src = array.getDrawable(R.styleable.RotateImageView_drawableSrc);
        backgroundColor = array.getColor(R.styleable.RotateImageView_backgroundColor, backgroundColor);
        backgroundStrokeWidth = (int)array.getDimension(R.styleable.RotateImageView_backgroundStrokeWidth, backgroundStrokeWidth);
        rotateSpeed = array.getInt(R.styleable.RotateImageView_rotateSpeed, rotateSpeed);
        array.recycle();
        setWillNotDraw(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawContent(canvas);
    }

    private void drawContent(Canvas canvas)
    {
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        int radius = width > height ? height / 2 : width / 2;
        int centerX = width / 2;
        int centerY = height / 2;
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        canvas.save();
        Path path = new Path();
        path.addCircle(centerX, centerY, radius, Path.Direction.CCW);

        canvas.clipPath(path);
        path.reset();
        path.addCircle(centerX, centerY, radius - backgroundStrokeWidth, Path.Direction.CCW);
        canvas.clipPath(path, Region.Op.DIFFERENCE);
        canvas.drawColor(backgroundColor);
        canvas.restore();

        int left = centerX - (radius - backgroundStrokeWidth);
        int right = centerX + (radius - backgroundStrokeWidth);
        int top = centerY - (radius - backgroundStrokeWidth);
        int bottom = centerY + (radius - backgroundStrokeWidth);
        src.setBounds(left, top, right, bottom);

        canvas.save();
        path.reset();
        path.addCircle(centerX, centerY, radius - backgroundStrokeWidth, Path.Direction.CCW);
        canvas.clipPath(path);
        path.reset();
        path.addCircle(centerX, centerY, (int)(radius * 0.1f), Path.Direction.CCW);
        canvas.clipPath(path, Region.Op.DIFFERENCE);

        long time = System.currentTimeMillis();
        if(!shouldRotate)
        {
            degree = 0;
        }else{
            if(lastDrawTime != -1)
            {
                degree += (int)((time - lastDrawTime) * 1.0f / 100 * rotateSpeed);
            }
        }
        degree = degree % 360;
        canvas.rotate(degree, centerX, centerY);
        src.draw(canvas);
        canvas.restore();

        if(shouldRotate)
        {
            lastDrawTime = time;
            postInvalidateDelayed(10);
        }
    }

    public void startRotate()
    {
        shouldRotate = true;
        lastDrawTime = -1;
        invalidate();
    }

    public void resetRotate()
    {
        degree = 0;
        invalidate();
    }

    public void stopRotate()
    {
        shouldRotate = false;
        lastDrawTime = -1;
    }
}
