package com.zu.customview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.mm.opensdk.diffdev.a.d;
import com.zu.customview.R;

import java.io.DataInput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by rikson on 2018/7/31.
 */

public class TabBtnIndicator extends ViewGroup implements View.OnClickListener {

    private int selectIndex = 0;

    private float posOffset = 0;

    private int selectedTextColor = 0x000000, unselectedTextColor = 0x555555;
    private float textSize = 5.0f;
    private int iconSize = 20;
    private ArrayList<DataEntity> data = null;

    private Drawable indicatorDrawable = null;
    private ImageView indicatorImageView = null;

    private int contentMarginInterval = 5, contentMarginTop = 0, contentMarginBottom = 0;

    private int indicatorMaxWidth = -1;

    private HashMap<String, LayerDrawable> layerDrawableHashMap = new HashMap<>();
    private HashMap<String, ImageView> imageViewHashMap = new HashMap<>();
    private HashMap<String, TextView> textViewHashMap = new HashMap<>();

    private OnIndicatorClickListener mOnClickListener = null;

    public TabBtnIndicator(Context context) {
        this(context, null);
    }

    public TabBtnIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabBtnIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TabBtnIndicator(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.TabBtnIndicator);
        textSize = array.getDimension(R.styleable.TabBtnIndicator_textSize, textSize);
        iconSize = array.getDimensionPixelSize(R.styleable.TabBtnIndicator_iconSize, iconSize);

        selectedTextColor = array.getColor(R.styleable.TabBtnIndicator_selectedTextColor, selectedTextColor);
        unselectedTextColor = array.getColor(R.styleable.TabBtnIndicator_unselectedTextColor, unselectedTextColor);

        indicatorMaxWidth = array.getDimensionPixelSize(R.styleable.TabBtnIndicator_indicatorMaxWidth, indicatorMaxWidth);
        indicatorDrawable = array.getDrawable(R.styleable.TabBtnIndicator_indicatorDrawable);
        contentMarginInterval = array.getDimensionPixelSize(R.styleable.TabBtnIndicator_contentMarginInterval, contentMarginInterval);
        contentMarginTop = array.getDimensionPixelSize(R.styleable.TabBtnIndicator_contentMarginTop, contentMarginTop);
        contentMarginBottom = array.getDimensionPixelSize(R.styleable.TabBtnIndicator_contentMarginBottom, contentMarginBottom);
        array.recycle();

        setClickable(true);
        setLongClickable(true);

        initIndicatorDrawable();
        if(isInEditMode())
        {

            ArrayList<DataEntity> mData = new ArrayList<>();
            mData.add(new DataEntity(0, "play", getContext().getDrawable(R.mipmap.bg_pause), getContext().getDrawable(R.mipmap.bg_play)));
            mData.add(new DataEntity(1, "next", getContext().getDrawable(R.mipmap.bg_play_next), getContext().getDrawable(R.mipmap.bg_play_previous)));
            setData(mData);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        if(data != null && data.size() != 0)
        {
            int resultHeight = 0;
            int resultWidth = 0;

            int totalTextWidth = 0;

            int textViewSpec = getChildMeasureSpec(MeasureSpec.UNSPECIFIED, 0, LayoutParams.WRAP_CONTENT);
            int imageViewSpec = MeasureSpec.makeMeasureSpec(iconSize, MeasureSpec.EXACTLY);
            for(DataEntity d : data)
            {
                TextView textView = textViewHashMap.get(d.tag);

                textView.measure(textViewSpec, textViewSpec);
                totalTextWidth += textView.getMeasuredWidth();

                ImageView imageView = imageViewHashMap.get(d.tag);
                imageView.measure(imageViewSpec, imageViewSpec);
            }
            int textViewHeight = textViewHashMap.get(data.get(0).tag).getMeasuredHeight();

            int tempMaxWidth = Math.max(iconSize * data.size(), totalTextWidth) + getPaddingLeft() + getPaddingRight();

            resultHeight = getPaddingBottom() + getPaddingTop() + textViewHeight + contentMarginInterval
                    + iconSize + contentMarginTop + contentMarginBottom;

            if(widthMode == MeasureSpec.UNSPECIFIED)
            {
                resultWidth = tempMaxWidth;
            }else if(widthMode == MeasureSpec.AT_MOST)
            {
                resultWidth = Math.min(tempMaxWidth, widthSize);
            }else{
                resultWidth = widthSize;
            }

            if(heightMode == MeasureSpec.AT_MOST)
            {
                resultHeight = Math.min(heightSize, resultHeight);
            }else if(heightMode == MeasureSpec.EXACTLY)
            {
                resultHeight = heightSize;
            }

            if(indicatorImageView != null)
            {
                int indicatorWidth = (resultWidth - getPaddingLeft() - getPaddingRight()) / data.size();
                int indicatorHeight = resultHeight - getPaddingTop() - getPaddingBottom();

                if(indicatorMaxWidth > 0)
                {
                    indicatorWidth = Math.min(indicatorMaxWidth, indicatorWidth);
                }

                int indicatorWidthSpec = MeasureSpec.makeMeasureSpec(indicatorWidth, MeasureSpec.EXACTLY);
                int indicatorHeightSpec = MeasureSpec.makeMeasureSpec(indicatorHeight, MeasureSpec.EXACTLY);
                indicatorImageView.measure(indicatorWidthSpec, indicatorHeightSpec);
            }

            setMeasuredDimension(resultWidth, resultHeight);


        }else{
            int resultHeight = 0;
            int resultWidth = 0;
            if(heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED)
            {
                resultHeight = 0;
            }else{
                resultHeight = heightSize;
            }

            if(widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED)
            {
                resultWidth = 0;
            }else{
                resultWidth = widthSize;
            }
            setMeasuredDimension(resultWidth, resultHeight);
        }
    }



    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if(changed)
        {
            if(data == null || data.size() == 0)
            {
                return;
            }
            int width = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
            int height = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
            int blockWidth = width / data.size();
            int itemWidth = 0, itemHeight = 0;
            int left, top, bottom, right;
            for(int i = 0; i < data.size(); i++)
            {
                int centerX = (int)(i * blockWidth + blockWidth * 0.5 + getPaddingLeft());
                DataEntity d = data.get(i);
                ImageView imageView = imageViewHashMap.get(d.tag);
                itemWidth = imageView.getMeasuredWidth();
                itemHeight = imageView.getMeasuredHeight();
                left = centerX - (itemWidth / 2);
                right = left + itemWidth;
                top = getPaddingTop() + contentMarginTop;
                bottom = top + itemHeight;
                imageView.layout(left, top, right, bottom);

                TextView textView = textViewHashMap.get(d.tag);
                itemWidth = textView.getMeasuredWidth();
                itemHeight = textView.getMeasuredHeight();
                left = centerX - (itemWidth / 2);
                right = left + itemWidth;
                top = bottom + contentMarginInterval;
                bottom = top + itemHeight;
                textView.layout(left, top, right, bottom);

            }

            if(indicatorImageView != null)
            {
                int indicatorWidth = indicatorImageView.getMeasuredWidth();
                int indicatorHeight = indicatorImageView.getMeasuredHeight();

                top = getPaddingTop();
                bottom = top + indicatorHeight;

                int centerX = (int)(getPaddingLeft() + (0.5f + selectIndex + posOffset) * blockWidth);
                left = (int)(centerX - 0.5f * indicatorWidth);
                right = left + indicatorWidth;

                indicatorImageView.layout(left, top, right, bottom);

            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_UP:
                int x = (int)event.getX();
                int blockWidth = (getWidth() - getPaddingRight() - getPaddingLeft()) / data.size();
                int index = x / blockWidth;
                notifyClicked(data.get(index).id);
        }
        return super.onTouchEvent(event);
    }

    /**使用在ViewPager.OnPageChangeListener.onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
     * 方法中，将三个参数原样传到该函数即可。
     * @param position 当前可见的第一个页面的序号，如果positionOffset不为0,那么position + 1页面也是可见的。
     * @param positionOffset 取值范围[0, 1)，表示当前position页面的偏离范围。
     * @param positionOffsetPixels 当前position页面的偏离值。
     * */
    public void listen(int position, float positionOffset, int positionOffsetPixels){
        DataEntity d = data.get(selectIndex);
        d.unselectedDrawable.setAlpha(0xff);
        d.selectedDrawable.setAlpha(0);

        TextView textView = textViewHashMap.get(d.tag);
        textView.setTextColor(unselectedTextColor);


        selectIndex = position;
        posOffset = positionOffset;
        setUpTag(selectIndex, posOffset);
    }

    private void setUpTag(int position, float positionOffset)
    {
        if(position == data.size() - 1)
        {
            DataEntity dNow = data.get(position);
            dNow.selectedDrawable.setAlpha(0xff);
            dNow.unselectedDrawable.setAlpha(0);
            TextView tvNow = textViewHashMap.get(dNow.tag);
            tvNow.setTextColor(selectedTextColor);

        }else{
            DataEntity dNow = data.get(position);
            DataEntity dNext = data.get(position + 1);

            int alphaNow = (int)(0xff * (1 - positionOffset));
            int alphaNext = (int)(0xff * positionOffset);

            dNow.selectedDrawable.setAlpha(alphaNow);
            dNow.unselectedDrawable.setAlpha(alphaNext);

            dNext.selectedDrawable.setAlpha(alphaNext);
            dNext.unselectedDrawable.setAlpha(alphaNow);

            int unselectedColor = getAnimatedColor(selectedTextColor, unselectedTextColor, positionOffset);
            int selectedColor = getAnimatedColor(unselectedTextColor, selectedTextColor, positionOffset);

            TextView tvNow = textViewHashMap.get(dNow.tag);
            TextView tvNext = textViewHashMap.get(dNext.tag);

            tvNow.setTextColor(unselectedColor);
            tvNext.setTextColor(selectedColor);
        }


        if(indicatorImageView != null)
        {
            int width = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();

            int blockWidth = width / data.size();
            int left, top, bottom, right;

            int indicatorWidth = indicatorImageView.getMeasuredWidth();
            int indicatorHeight = indicatorImageView.getMeasuredHeight();

            top = getPaddingTop();
            bottom = top + indicatorHeight;

            int centerX = (int)(getPaddingLeft() + (0.5f + selectIndex + posOffset) * blockWidth);
            left = centerX - indicatorWidth / 2;
            right = left + indicatorWidth;

            indicatorImageView.layout(left, top, right, bottom);

        }
    }

    private int getAnimatedColor(int startColor, int endColor, float progress)
    {
        int startAlpha = (startColor >> 24) & 0xff;
        int startR = (startColor >> 16) & 0xff;
        int startG = (startColor >> 8) & 0xff;
        int startB = startColor & 0xff;

        int endAlpha = (endColor >> 24) & 0xff;
        int endR = (endColor >> 16) & 0xff;
        int endG = (endColor >> 8) & 0xff;
        int endB = endColor & 0xff;

        int resultAlpha = (int)((endAlpha - startAlpha) * progress + startAlpha);
        int resultR = (int)((endR - startR) * progress + startR);
        int resultG = (int)((endG - startG) * progress + startG);
        int resultB = (int)((endB - startB) * progress + startB);

        int resultColor = (resultAlpha << 24) | (resultR << 16) | (resultG << 8) | resultB;

        return resultColor;
    }

    @Override
    public void onClick(View v) {
        int id = (int)v.getTag();
        notifyClicked(id);
    }

    public void setIndicatorDrawable(Drawable drawable)
    {
        indicatorDrawable = drawable;
        initIndicatorDrawable();
    }

    public void setData(@NonNull ArrayList<DataEntity> pData)
    {

        data = pData;
        initData();
        invalidate();
    }

    private void initData()
    {
        layerDrawableHashMap.clear();
        imageViewHashMap.clear();
        textViewHashMap.clear();
        removeAllViews();


        selectIndex = 0;
        posOffset = 0f;


        for(DataEntity d : data)
        {
            Drawable[] drawables = new Drawable[2];
            drawables[0] = d.selectedDrawable;
            drawables[1] = d.unselectedDrawable;
            drawables[0].setAlpha(0);
            LayerDrawable layerDrawable = new LayerDrawable(drawables);
            ImageView imageView = new ImageView(getContext());
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setImageDrawable(layerDrawable);
            imageView.setTag(d.id);
            /*imageView.setClickable(true);
            imageView.setOnClickListener(this);*/
            addView(imageView);

            TextView textView = new TextView(getContext());
            textView.setTextSize(textSize);
            textView.setText(d.tag);
            textView.setMaxLines(1);
            /*textView.setClickable(true);
            textView.setOnClickListener(this);*/
            textView.setTag(d.id);
            textView.setTextColor(unselectedTextColor);

            addView(textView);

            layerDrawableHashMap.put(d.tag, layerDrawable);
            imageViewHashMap.put(d.tag, imageView);
            textViewHashMap.put(d.tag, textView);
        }
        if(indicatorImageView != null)
        {
            addView(indicatorImageView, 0);
        }


        setUpTag(selectIndex, posOffset);
    }

    private void initIndicatorDrawable()
    {
        if(indicatorDrawable == null)
        {
            indicatorImageView = null;
        }else{
            if(indicatorImageView == null)
            {
                indicatorImageView = new ImageView(getContext());
                indicatorImageView.setScaleType(ImageView.ScaleType.FIT_XY);
            }
            indicatorImageView.setImageDrawable(indicatorDrawable);
            addView(indicatorImageView, 0);
        }
    }

    public void setOnIndicatorClickListener(OnIndicatorClickListener listener)
    {
        mOnClickListener = listener;
    }

    public void removeOnIndicatorClickListener()
    {
        mOnClickListener = null;
    }

    private void notifyClicked(int id)
    {
        if(mOnClickListener != null)
        {
            mOnClickListener.onClick(id);
        }
    }

    public static class DataEntity{
        public String tag = null;
        public Drawable unselectedDrawable = null, selectedDrawable = null;
        public int id;

        public DataEntity(int id, String tag, Drawable unselectedDrawable, Drawable selectedDrawable) {
            this.tag = tag;
            this.unselectedDrawable = unselectedDrawable;
            this.selectedDrawable = selectedDrawable;
            this.id = id;
        }
    }

    public interface OnIndicatorClickListener{
        void onClick(int id);
    }
}
