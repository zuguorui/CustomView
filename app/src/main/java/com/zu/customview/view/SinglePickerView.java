package com.zu.customview.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by rikson on 2018/10/12.
 */

public class SinglePickerView extends View {

    public static final int ORI_HOR = 0;
    public static final int ORI_VER = 1;

    private String[] data = null;
    private int drawOffset = 0;
    private int textColor = 0xaaaaaa;
    private int selectedTextColor = 0x000000;
    private int textSize = 20;
    private int selectedTextSize = 30;
    private int orientation = ORI_HOR;
    private int totalTextWidth = 0;
    private int[] textWidth = null;
    private int textHeight = 0;
    private int totalTextHeight = 0;
    private int space = 0;
    private int currentIndex = 0;
    private LinearGradient gradient;

    private Paint textPaint = new Paint();
    public SinglePickerView(Context context) {
        this(context, null);
    }

    public SinglePickerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SinglePickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SinglePickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void layout(int l, int t, int r, int b) {
        super.layout(l, t, r, b);
    }

    public void setOrientation(int orientation)
    {
        this.orientation = orientation;
    }



    public void setData(String[] data)
    {
        this.data = data;
        initData();
    }

    public String[] getData()
    {
        return data;
    }

    private int getCenterX()
    {
        int leftEdge = getPaddingLeft();
        int rightEdge = getWidth() - getPaddingRight();
        return (leftEdge + rightEdge) / 2;
    }

    private int getCenterY()
    {
        int topEdge = getPaddingTop();
        int bottomEdge = getHeight() - getPaddingBottom();
        return (topEdge + bottomEdge) / 2;
    }

    private void initData()
    {
        totalTextWidth = 0;
        if(data == null || data.length == 0)
        {
            return;
        }
        textPaint.setTextSize(selectedTextSize);
        textWidth = new int[data.length];
        for(int i = 0; i < data.length; i++)
        {
            int len = (int)textPaint.measureText(data[i]);
            textWidth[i] = len;
            totalTextWidth += len;
        }
        totalTextWidth += (data.length - 1) * space;

        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        textHeight = (int)(metrics.bottom - metrics.top);
        totalTextHeight = textHeight * data.length + (data.length - 1) * space;

    }

    private void computeOffsetByIndex()
    {
        if(data == null || currentIndex >= data.length || currentIndex < 0)
        {
            return;
        }
        if(orientation == ORI_HOR)
        {
            int center = getCenterX();
            int len = 0;
            for(int i = 0; i < currentIndex; i++)
            {
                len += textWidth[i] + space;
            }
            len += textWidth[currentIndex] / 2;
            drawOffset = center - len;
        }else{
            int center = getCenterY();
            int len = 0;
            if(currentIndex > 0)
            {
                len += (currentIndex - 1) * (textHeight + space);
            }
            len += textHeight / 2;
            drawOffset = center - len;
        }
    }

    private void drawContent(Canvas canvas)
    {
//        canvas.drawBitmap();
    }




}
