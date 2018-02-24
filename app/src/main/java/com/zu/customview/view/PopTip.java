package com.zu.customview.view;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;
import android.os.IBinder;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import io.reactivex.annotations.NonNull;

/**
 * Created by rickson on 2018/2/24.
 */

public class PopTip extends PopupWindow {

    private Param param = new Param();
    private String message = null;


    private OnDismissListener dismissListenerContainer = null;

    private OnDismissListener recycleBitmapListener = null;

    private List<OnDismissListener> listeners = new LinkedList<>();

    private Bitmap resultBitmap = null;

    public enum ALIGN{START, CENTER, END};
    public enum EDGE{LEFT, TOP, RIGHT, BOTTOM};


    public void setParam(Param param)
    {
        this.param = param;
    }

    public Param getParam()
    {
        return param;
    }

    private void notifyListeners(){
        for(OnDismissListener listener : listeners)
        {
            listener.onDismiss();
        }
    }

    public PopTip()
    {
        super();
        dismissListenerContainer = new OnDismissListener() {
            @Override
            public void onDismiss() {
                notifyListeners();
            }
        };
        setOnDismissListener(dismissListenerContainer);
        recycleBitmapListener = new OnDismissListener() {
            @Override
            public void onDismiss() {
                if(resultBitmap != null && !resultBitmap.isRecycled())
                {
                    resultBitmap.recycle();
                    resultBitmap = null;
                }

            }
        };
        listeners.add(recycleBitmapListener);
    }

    public PopTip(Param param)
    {
        this();
        this.param = param;

    }


    public void setMessage(String message)
    {
        this.message = message;

    }



    public String getMessage()
    {
        return message;
    }

    private void updataBitmap(@NonNull EDGE edge, @NonNull ALIGN align, @NonNull RectF contentPos, @NonNull RectF triPos)
    {
        setContentView(null);
        if(resultBitmap != null && !resultBitmap.isRecycled())
        {
            resultBitmap.recycle();
            resultBitmap = null;
        }
        int height = 0, width = 0;
        if(edge == EDGE.LEFT || edge == EDGE.RIGHT)
        {
            height = (int)contentPos.height();
            width = (int)(contentPos.width() + triPos.width());
        }else
        {
            width = (int)contentPos.width();
            height = (int)(contentPos.height() + triPos.height());
        }

        resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resultBitmap);

        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(param.backgroundColor);
        backgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawRoundRect(contentPos.left, contentPos.top, contentPos.right, contentPos.bottom, param.roundX, param.roundY, backgroundPaint);

        if(edge == EDGE.LEFT)
        {
            Path path = new Path();
            path.moveTo(triPos.left, triPos.centerY());
            path.lineTo(triPos.right, triPos.bottom);
            path.lineTo(triPos.right, triPos.top);
            path.close();
            canvas.drawPath(path, backgroundPaint);
        }else if(edge == EDGE.RIGHT)
        {
            Path path = new Path();
            path.moveTo(triPos.right, triPos.centerY());
            path.lineTo(triPos.left, triPos.bottom);
            path.lineTo(triPos.left, triPos.top);
            path.close();
            canvas.drawPath(path, backgroundPaint);
        }else if(edge == EDGE.TOP)
        {
            Path path = new Path();
            path.moveTo(triPos.centerX(), triPos.top);
            path.lineTo(triPos.left, triPos.bottom);
            path.lineTo(triPos.right, triPos.bottom);
            path.close();
            canvas.drawPath(path, backgroundPaint);
        }else{
            Path path = new Path();
            path.moveTo(triPos.centerX(), triPos.bottom);
            path.lineTo(triPos.left, triPos.top);
            path.lineTo(triPos.right, triPos.top);
            path.close();
            canvas.drawPath(path, backgroundPaint);
        }

        String temp = message == null ? "" : message;
        Paint textPaint = new Paint();
        textPaint.setTextSize(param.textSize);
        textPaint.setColor(param.textColor);
        float textWidth = textPaint.measureText(temp);
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float textHeight = Math.abs(fm.top - fm.bottom);
        canvas.drawText(temp, contentPos.centerX() - textWidth / 2, contentPos.centerY() + textHeight / 2, textPaint);


    }

    private void calculatePosition(@NonNull EDGE edge, @NonNull ALIGN align, @NonNull RectF contentPos, @NonNull RectF triPos)
    {
        if(param == null)
        {
            param = new Param();
        }
        Paint textPaint = new Paint();
        textPaint.setTextSize(param.textSize);
        String temp = message == null ? "" : message;
        float textWidth = textPaint.measureText(temp);
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float textHeight = Math.abs(fm.top - fm.bottom);
        int width = (int)(textWidth + param.paddingLeft + param.paddingRight);
        int height = (int)(textHeight + param.paddingTop + param.paddintBottom);
        if(edge == EDGE.TOP || edge == EDGE.BOTTOM)
        {
            if(width < param.triWidth + param.roundX * 2)
            {
                width = (int)(param.triWidth + param.roundX * 2);
            }
        }else
        {
            if(height < param.triWidth + param.roundY * 2)
            {
                height = (int)(param.triWidth + param.roundY * 2);
            }
        }

        if(edge == EDGE.LEFT)
        {
            contentPos.top = 0;
            contentPos.bottom = contentPos.top + height;
            contentPos.left = param.triHeight;
            contentPos.right = contentPos.left + width;
            triPos.left = 0;
            triPos.right = triPos.left + param.triHeight;

        }else if(edge == EDGE.RIGHT){
            contentPos.top = 0;
            contentPos.bottom = contentPos.top + height;
            contentPos.left = 0;
            contentPos.right = contentPos.left + width;
            triPos.left = contentPos.right;
            triPos.right = triPos.left + param.triHeight;

        }else if(edge == EDGE.TOP)
        {
            contentPos.top = 0;
            contentPos.bottom =  height;
            contentPos.left = 0;
            contentPos.right = contentPos.left + width;
            triPos.top = contentPos.bottom;
            triPos.bottom = triPos.top + param.triHeight;
        }else{
            contentPos.top = param.triHeight;
            contentPos.bottom = contentPos.top + height;
            contentPos.left = 0;
            contentPos.right = contentPos.left + width;
            triPos.top = 0;
            triPos.bottom = triPos.top + param.triHeight;
        }
        if(edge == EDGE.LEFT || edge == EDGE.RIGHT)
        {
            if(align == ALIGN.START)
            {
                triPos.top = param.roundY;
                triPos.bottom = triPos.top + param.triWidth;
            }else if(align == ALIGN.CENTER)
            {
                triPos.top = (contentPos.height() - param.triWidth) / 2;
                triPos.bottom = triPos.top + param.triWidth;
            }else
            {
                triPos.bottom = contentPos.height() - param.roundY;
                triPos.top = triPos.bottom - param.triWidth;
            }
        }else{
            if(align == ALIGN.START)
            {
                triPos.left = param.roundX;
                triPos.right = triPos.left + param.triWidth;
            }else if(align == ALIGN.CENTER)
            {
                triPos.left = (contentPos.width() - param.triWidth) / 2;
                triPos.right = triPos.left + param.triWidth;
            }else
            {
                triPos.right = contentPos.width() - param.roundX;
                triPos.left = triPos.right - param.triWidth;
            }
        }


    }

    public void showAt(Context context, View anchorView, String message , EDGE edge, ALIGN align){
        this.message = message;
        RectF contentPos = new RectF();
        RectF triPos = new RectF();

        calculatePosition(edge, align, contentPos, triPos);
        updataBitmap(edge, align, contentPos, triPos);

        Drawable b = new BitmapDrawable(context.getResources(), resultBitmap);
        setBackgroundDrawable(b);

    }

    private Rect calculateShowPosition(Context context, View anchorView, EDGE edge, ALIGN align, RectF contentPos, RectF triPos)
    {
        int screenWidth = getScreenWidth(context), screenHeight = getScreenHeight(context);
        int[] anchorLocation = new int[2];
        anchorView.getLocationInWindow(anchorLocation);
        int top = 0, left = 0;
        Rect rect = new Rect();
        if(edge == EDGE.LEFT)
        {
            rect.right = anchorLocation[0];

        }
    }

    private int getScreenHeight(Context context)
    {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    private int getScreenWidth(Context context)
    {
        return context.getResources().getDisplayMetrics().widthPixels;
    }



    public static class Param{
        public float triHeight = 10f, triWidth = 15f;
        public float roundX = 0f, roundY = 0;
        public int backgroundColor = 0x00000000;

        public int textColor = 0xff000000;
        public float textSize = 15f;
        public int paddingLeft = 2, paddingRight = 2, paddingTop = 2, paddintBottom = 2;
    }
}
