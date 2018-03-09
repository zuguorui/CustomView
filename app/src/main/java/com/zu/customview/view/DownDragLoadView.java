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
 * Created by zu on 2017/10/8.
 */

public class DownDragLoadView extends DragLoadView {
    private ImageView imageView;
    private TextView textView;
    private ValueAnimator animator = null;


    private MyLog log = new MyLog("DownDragLoadView", true);
    public DownDragLoadView(@NonNull Context context) {

        this(context, null);



    }

    public DownDragLoadView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DownDragLoadView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public DownDragLoadView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setLayoutParams(layoutParams);
        View view = LayoutInflater.from(context).inflate(R.layout.drag_load_view, null);
        this.addView(view);
        imageView = (ImageView)view.findViewById(R.id.DragLoad_imageView);
        textView = (TextView)view.findViewById(R.id.DragLoad_textView);
        textView.setText("上拉加载更多");
    }

    @Override
    public void drag(float process) {
        log.d("onDrag, process = " + process);
        imageView.setRotation(process * 360);
        super.drag(process);
    }

    @Override
    public void dragRelease(float process) {
        super.dragRelease(process);
    }

    @Override
    public void dragStart() {
        super.dragStart();
    }


    @Override
    public void loadComplete(boolean success) {
        super.loadComplete(success);
        animator.end();
        if(success)
        {
            textView.setText("加载完成");
        }else
        {
            textView.setText("加载失败");
        }
    }

    @Override
    public void loadStart() {

        super.loadStart();
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
        textView.setText("正在加载");
    }

    @Override
    public void viewHidden() {
        if(animator != null)
        {
            animator.end();
        }
        textView.setText("上拉加载更多");
    }
}
