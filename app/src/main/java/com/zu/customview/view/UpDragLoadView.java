package com.zu.customview.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zu.customview.MyLog;
import com.zu.customview.R;


/**
 * Created by zu on 17-9-25.
 */

public class UpDragLoadView extends DragLoadView {
    private ImageView imageView;
    private TextView textView;
    private ValueAnimator animator = null;
    MyLog log = new MyLog("UpDragLoadView", true);
    public UpDragLoadView(@NonNull Context context) {
        this(context, null);


    }

    public UpDragLoadView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UpDragLoadView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public UpDragLoadView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setLayoutParams(layoutParams);
        View view = LayoutInflater.from(context).inflate(R.layout.drag_load_view, null);
        this.addView(view);
        imageView = (ImageView)view.findViewById(R.id.DragLoad_imageView);
        textView = (TextView)view.findViewById(R.id.DragLoad_textView);
        textView.setText("下拉刷新");
    }

    @Override
    public void onDrag(float process) {
        imageView.setRotation(process * 360);
        log.d("onDrag, process = " + process);
        super.onDrag(process);
    }

    @Override
    public void onDragRelease(float process) {
        super.onDragRelease(process);
    }

    @Override
    public void onDragStart() {
        super.onDragStart();
    }


    @Override
    public void onLoadComplete(boolean success) {
        super.onLoadComplete(success);
        animator.resume();
        if(success)
        {
            textView.setText("刷新成功");
        }else
        {
            textView.setText("刷新失败");
        }
    }

    @Override
    public void onLoadStart() {

        super.onLoadStart();
        if(animator == null)
        {
            animator = ValueAnimator.ofFloat(0f, 360f);
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setRepeatMode(ValueAnimator.RESTART);
            animator.setDuration(1000);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float degree = (float)animation.getAnimatedValue();
                    imageView.setRotation(degree);
                }
            });

        }
        animator.start();
        textView.setText("正在刷新");


    }
}
