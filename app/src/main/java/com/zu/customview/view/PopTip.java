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
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;
import android.os.IBinder;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.zu.customview.App;
import com.zu.customview.R;

import java.util.LinkedList;
import java.util.List;

import io.reactivex.annotations.NonNull;

/**
 * Created by rickson on 2018/2/24.
 */

public class PopTip extends PopupWindow {

    private Param param = new Param();
    private PxParam pxParam = null;
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
        transToPxParam(param);
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
        setOutsideTouchable(true);
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
        setParam(param);

    }


    public void setMessage(String message)
    {
        this.message = message;

    }



    public String getMessage()
    {
        return message;
    }

    private void updateBitmap(@NonNull EDGE edge, @NonNull ALIGN align, @NonNull RectF contentPos, @NonNull RectF triPos)
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
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setColor(pxParam.backgroundColor);
        backgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawRoundRect(contentPos.left, contentPos.top, contentPos.right, contentPos.bottom, pxParam.roundX, pxParam.roundY, backgroundPaint);

        if(edge == EDGE.LEFT)
        {
            Path path = new Path();
            path.moveTo(triPos.right, triPos.centerY());
            path.lineTo(triPos.left, triPos.bottom);
            path.lineTo(triPos.left, triPos.top);
            path.close();
            canvas.drawPath(path, backgroundPaint);
        }else if(edge == EDGE.RIGHT)
        {
            Path path = new Path();
            path.moveTo(triPos.left, triPos.centerY());
            path.lineTo(triPos.right, triPos.bottom);
            path.lineTo(triPos.right, triPos.top);
            path.close();
            canvas.drawPath(path, backgroundPaint);
        }else if(edge == EDGE.BOTTOM)
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
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(pxParam.textSize);
        textPaint.setColor(pxParam.textColor);
        float textWidth = textPaint.measureText(temp);
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float textHeight = fm.ascent + fm.descent;
        canvas.drawText(temp, contentPos.centerX() - textWidth / 2, contentPos.centerY() - textHeight / 2 , textPaint);


    }

    private void calculateContentRect(@NonNull EDGE edge, @NonNull RectF contentRect)
    {
        if(pxParam == null)
        {
           setParam(new Param());
        }



        Paint textPaint = new Paint();
        textPaint.setTextSize(pxParam.textSize);
        String temp = message == null ? "" : message;
        float textWidth = textPaint.measureText(temp);
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float textHeight = Math.abs(fm.top) + Math.abs(fm.bottom);
        int width = (int)(textWidth + pxParam.paddingLeft + pxParam.paddingRight);
        int height = (int)(textHeight + pxParam.paddingTop + pxParam.paddingBottom);
        if(edge == EDGE.TOP || edge == EDGE.BOTTOM)
        {
            if(width < pxParam.triWidth + pxParam.roundX * 2)
            {
                width = (int)(pxParam.triWidth + pxParam.roundX * 2);
            }
        }else
        {
            if(height < pxParam.triWidth + pxParam.roundY * 2)
            {
                height = (int)(pxParam.triWidth + pxParam.roundY * 2);
            }
        }
        contentRect.left = 0;
        contentRect.right = contentRect.left + width;
        contentRect.top = 0;
        contentRect.bottom = contentRect.top + height;
    }

    private void calculateTriRect(@NonNull EDGE edge, @NonNull RectF triPos)
    {
        float width = 0, height = 0;
        if(edge == EDGE.RIGHT || edge == EDGE.LEFT)
        {
            width = pxParam.triHeight;
            height = pxParam.triWidth;
        }else{
            width = pxParam.triWidth;
            height = pxParam.triHeight;
        }
        triPos.top = 0;
        triPos.bottom = triPos.top + height;
        triPos.left = 0;
        triPos.right = triPos.left + width;
    }

    private void calculatePosition(@NonNull EDGE edge, @NonNull ALIGN align, @NonNull RectF contentPos, @NonNull RectF triPos)
    {
        if(pxParam == null)
        {
            setParam(new Param());
        }
        calculateContentRect(edge, contentPos);
        calculateTriRect(edge, triPos);

        if(edge == EDGE.LEFT)
        {
            triPos.offset(contentPos.width(), 0);


        }else if(edge == EDGE.RIGHT){
            contentPos.offset(triPos.width(), 0);


        }else if(edge == EDGE.TOP)
        {
            triPos.offset(0, contentPos.height());

        }else{
            contentPos.offset(0, triPos.height());

        }

        if(edge == EDGE.LEFT || edge == EDGE.RIGHT)
        {
            if(align == ALIGN.START)
            {
                triPos.offset(0, pxParam.roundY - triPos.top);
            }else if(align == ALIGN.CENTER)
            {
                triPos.offset(0, contentPos.centerY() - triPos.centerY());
            }else
            {
                triPos.offset(0, contentPos.bottom - pxParam.roundY - triPos.bottom);
            }
        }else{
            if(align == ALIGN.START)
            {
                triPos.offset(pxParam.roundX - triPos.left, 0);
            }else if(align == ALIGN.CENTER)
            {
                triPos.offset(contentPos.centerX() - triPos.centerX(), 0);
            }else
            {
                triPos.offset(contentPos.width() - pxParam.roundX - triPos.right,0);
            }
        }



    }

    public void showAt(Context context, View anchorView, String message , EDGE edge, ALIGN align){
        this.message = message;
        RectF contentPos = new RectF();
        RectF triPos = new RectF();

        calculatePosition(edge, align, contentPos, triPos);
        updateBitmap(edge, align, contentPos, triPos);




        Rect rect = calculateShowPosition(context, anchorView, edge, align, contentPos, triPos);

        ImageView imageView = new ImageView(context);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(rect.width(), rect.height());
        imageView.setLayoutParams(layoutParams);
        imageView.setImageBitmap(resultBitmap);
        setContentView(imageView);
        setWidth(rect.width());
        setHeight(rect.height());
        setAnimationStyle(R.style.PopAlphaAnim);
        showAtLocation(anchorView, Gravity.TOP | Gravity.LEFT, rect.left, rect.top);

    }

    public void showAt(Context context, View anchorView, String message)
    {
        this.message = message;


        Pair<EDGE, ALIGN> arg = calculateSuitablePosition(context, anchorView);

        showAt(context, anchorView, message, arg.first, arg.second);

    }

    private Rect calculateShowPosition(Context context, View anchorView, EDGE edge, ALIGN align, RectF contentPos, RectF triPos)
    {

        int[] anchorLocation = new int[2];
        anchorView.getLocationInWindow(anchorLocation);

        Rect rect = new Rect();
        if(edge == EDGE.LEFT)
        {
            rect.right = anchorLocation[0];
            rect.left = rect.right - (int)(contentPos.width() + triPos.width());

        }else if(edge == EDGE.RIGHT)
        {
            rect.left = anchorLocation[0] + anchorView.getWidth();
            rect.right = rect.left + (int)(contentPos.width() + triPos.width());
        }else if(edge == EDGE.TOP)
        {
            rect.bottom = anchorLocation[1];
            rect.top = rect.bottom - (int)(contentPos.height() + triPos.height());

        }else{
            rect.top = anchorLocation[1] + anchorView.getHeight();
            rect.bottom = rect.top + (int)(contentPos.height() + triPos.height());
        }

        if(edge == EDGE.LEFT || edge == EDGE.RIGHT)
        {
            if(align == ALIGN.START)
            {
                rect.top = anchorLocation[1];
                rect.bottom = rect.top + (int)contentPos.height();
            }else if(align == ALIGN.CENTER)
            {
                rect.top = (anchorLocation[1] + anchorView.getHeight() / 2) - (int)contentPos.height() / 2;
                rect.bottom = rect.top + (int)contentPos.height();
            }else{
                rect.bottom = anchorLocation[1] - anchorView.getHeight();
                rect.top = rect.bottom - (int)contentPos.height();
            }
        }else{
            if(align == ALIGN.START)
            {
                rect.left = anchorLocation[0];
                rect.right = rect.left + (int)contentPos.width();
            }else if(align == ALIGN.CENTER)
            {
                rect.left = anchorLocation[0] + anchorView.getWidth() / 2 - (int)contentPos.width() / 2;
                rect.right = rect.left + (int)contentPos.width();
            }else{
                rect.right = anchorLocation[0] + anchorView.getWidth();
                rect.left = rect.right - (int)contentPos.width();
            }
        }

        return rect;
    }

    private Pair<EDGE, ALIGN> calculateSuitablePosition(Context context, View anchorView)
    {
        int screenWidth = getScreenWidth(context), screenHeight = getScreenHeight(context);
        int[] anchorLocation = new int[2];
        anchorView.getLocationInWindow(anchorLocation);

        int topSpace = anchorLocation[1];
        int bottomSpace = screenHeight - topSpace - anchorView.getHeight();
        int leftSpace = anchorLocation[0];
        int rightSpace = screenWidth - leftSpace - anchorView.getWidth();

        EDGE resultEDGE = null;
        ALIGN resultALIGN = null;

        RectF contentPos = new RectF();
        EDGE edge = EDGE.TOP;
        calculateContentRect(edge, contentPos);
        int height = (int)(contentPos.height() + pxParam.triHeight);

        int width = (int)contentPos.width();

        if(width < screenWidth)
        {
            if(height <= topSpace)
            {
                resultEDGE = EDGE.TOP;
            }else if(height <= bottomSpace){
                resultEDGE = EDGE.BOTTOM;
            }

            if(resultEDGE != null)
            {
                if(anchorView.getWidth() + rightSpace >= width)
                {
                    resultALIGN = ALIGN.START;
                }else if(anchorView.getWidth() >= width)
                {
                    resultALIGN = ALIGN.CENTER;
                }else{
                    resultALIGN = ALIGN.END;
                }
            }

            if(resultEDGE != null && resultALIGN != null)
            {
                return new Pair<>(resultEDGE, resultALIGN);
            }
        }

        edge = EDGE.LEFT;
        calculateContentRect(edge, contentPos);
        height = (int)contentPos.height();
        width = (int)(contentPos.width() + pxParam.triHeight);

        if(height < screenHeight)
        {
            if(width <= leftSpace)
            {
                resultEDGE = EDGE.LEFT;
            }else if(width <= rightSpace)
            {
                resultEDGE = EDGE.RIGHT;
            }
            if(resultEDGE != null)
            {
                if(anchorView.getHeight() + bottomSpace >= height)
                {
                    resultALIGN = ALIGN.START;
                }else if(anchorView.getHeight() >= height)
                {
                    resultALIGN = ALIGN.CENTER;
                }else
                {
                    resultALIGN = ALIGN.END;
                }
            }

            if(resultEDGE != null && resultALIGN != null)
            {
                return new Pair<>(resultEDGE, resultALIGN);
            }
        }

        if(bottomSpace > topSpace)
        {
            return new Pair<>(EDGE.BOTTOM, ALIGN.START);
        }else{
            return new Pair<>(EDGE.TOP, ALIGN.START);
        }



    }

    private void transToPxParam(Param param)
    {
        PxParam pxParam = new PxParam();
        pxParam.triHeight = dpToPx(param.triHeight);
        pxParam.triWidth = dpToPx(param.triWidth);
        pxParam.backgroundColor = param.backgroundColor;
        pxParam.textColor = param.textColor;
        pxParam.roundX = dpToPx(param.roundX);
        pxParam.roundY = dpToPx(param.roundY);
        pxParam.textSize = spToPx(param.textSize);
        pxParam.paddingLeft = dpToPx(param.paddingLeft);
        pxParam.paddingTop = dpToPx(param.paddingTop);
        pxParam.paddingRight = dpToPx(param.paddingRight);
        pxParam.paddingBottom = dpToPx(param.paddingBottom);

        this.pxParam = pxParam;
    }

    private int getScreenHeight(Context context)
    {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    private int getScreenWidth(Context context)
    {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    private static float dpToPx(int dp)
    {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, App.getAppContext().getResources().getDisplayMetrics());
    }

    private static float spToPx(int sp)
    {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, App.getAppContext().getResources().getDisplayMetrics());
    }


    public static class Param{
        public int triHeight = 10, triWidth = 15;
        public int roundX = 10, roundY = 10;
        public int backgroundColor = 0xffff0000;

        public int textColor = 0xff000000;
        public int textSize = 15;
        public int paddingLeft = 10, paddingRight = 10, paddingTop = 5, paddingBottom = 5;
    }

    private static class PxParam{
        public float triHeight = 10, triWidth = 15;
        public float roundX = 10, roundY = 10;
        public int backgroundColor = 0xffff0000;

        public int textColor = 0xffffffff;
        public float textSize = 10;
        public float paddingLeft = 20, paddingRight = 20, paddingTop = 20, paddingBottom = 20;
    }
}
