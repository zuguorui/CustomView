package com.zu.customview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.zu.customview.MyLog;
import com.zu.customview.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by zu on 17-3-8.
 */

public class TextViewPagerIndicator extends ViewGroup
{
    MyLog log = new MyLog("TextViewPagerIndicator", true);
    /**
     * 布局显示相关的参数
     * */
    private int textColor = Color.parseColor("#000000");
    private int backgroundColor = Color.parseColor("#ffffff");
    private int textSize = 10;
    private int indicatorColor = Color.parseColor("#ffffff");
    private int indicatorHeight = 3;
    /*tag之间的间隔*/
    private int interval = 10;
    private int textPadding = 0;
    private int textPaddingTop = 0;
    private int textPaddingBottom = 0;
    private int textPaddingLeft = 0;
    private int textPaddingRight = 0;
    private int selectedTextColor;
    private boolean balanceLayout = false;


    private enum IndicatorStyle
    {
        line, background
    }

    private IndicatorStyle indicatorStyle = IndicatorStyle.line;
    private int indicatorDrawable = -1;




    private ArrayList<String> tags = new ArrayList<>();
    private HashMap<String, TextView> tagMap = new HashMap<>();
    private ImageView indicatorLine;
    /*目前选中的位置*/
    private int currentPosition = 0;

    private boolean expanded = false;
    /*最前面的TextView的偏离值，为0时代表刚好对齐Layout的左侧*/
    private int textOffset = 0;
    /*tag的点击监听器*/
    private ArrayList<OnTagClickedListener> onTagClickedListeners = new ArrayList<>();


    /**
     * 事件相关参数
     * */

    private float newX, newY, lastX, lastY, dx, dy, downX, downY;
    /*判断是否是滑动*/
    private int touchSlop = 5;

    public TextViewPagerIndicator(Context context) {
        this(context, null);
    }

    public TextViewPagerIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextViewPagerIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TextViewPagerIndicator(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TextViewPagerIndicator);
        textColor = a.getColor(R.styleable.TextViewPagerIndicator_textColor, textColor);
        backgroundColor = a.getColor(R.styleable.TextViewPagerIndicator_backgroundColor, backgroundColor);
        indicatorColor = a.getColor(R.styleable.TextViewPagerIndicator_indicatorColor, indicatorColor);
        textSize = a.getDimensionPixelSize(R.styleable.TextViewPagerIndicator_textSize, textSize);
        indicatorHeight = a.getDimensionPixelSize(R.styleable.TextViewPagerIndicator_indicatorHeight, indicatorHeight);
        interval = a.getDimensionPixelSize(R.styleable.TextViewPagerIndicator_intervalBetweenTags, interval);
        selectedTextColor = a.getColor(R.styleable.TextViewPagerIndicator_selectedTextColor, indicatorColor);
        balanceLayout = a.getBoolean(R.styleable.TextViewPagerIndicator_balanceLayout, balanceLayout);
        a.recycle();
        indicatorLine = new ImageView(context);
        indicatorLine.setBackgroundColor(indicatorColor);
        this.addView(indicatorLine);

        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.setClickable(true);
        textOffset = getPaddingLeft();

        if(isInEditMode())
        {
            for(int i = 0; i < 5; i++)
            {
                addTag("item" + i);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        log.v("onMeasure");
        /*如果这个View不进行任何绘制操作，则设置为true，以便系统进行优化*/
        setWillNotDraw(true);

        /*获取父view传递给我们的宽和高的SpecMode和SpecSize*/
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        /*设置用来测量TextView宽度的MeasureSpec，由于tag是一定要完整的单行显示，因此我们将宽度的SpecMode设置为UNSPECIFIED，即
        * 要多大给多大*/
        int unspecifiedWidthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.UNSPECIFIED);
        /*设置用来测量TextView高度的MeasureSpec，不同于宽度，高度上TextView不能比我们指示器的高度更大，还要减去padding值和预留给横线的空间*/
        int atMostHeightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.AT_MOST);


        /*宽度结果，要考虑paddingLeft和paddingRight*/
        int resultWidthSize = getPaddingLeft() + getPaddingRight();
        /*高度结果，要考虑paddingTop和paddingBottom，还有给横线预留的位置*/
        int resultHeightSize = getPaddingTop() + getPaddingBottom() + indicatorHeight;

//        int childWidthSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
//        int childHeightSpec = 0;
//        if(heightSpecMode == MeasureSpec.UNSPECIFIED)
//        {
//            childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
//        }else
//        {
//            childHeightSpec = MeasureSpec.makeMeasureSpec(heightSize - resultHeightSize, MeasureSpec.EXACTLY);
//        }
        for(String tag : tags)
        {
            /*依次对每一个TextView进行布局，并将宽度累加到宽度结果里*/
            TextView child = tagMap.get(tag);
            ViewGroup.LayoutParams childLayoutParams = child.getLayoutParams();
            int childWidthSpec = getChildMeasureSpec(unspecifiedWidthMeasureSpec, resultWidthSize, childLayoutParams.width);
            int childHeightSpec = getChildMeasureSpec(atMostHeightMeasureSpec, resultHeightSize, childLayoutParams.height);
            child.measure(childWidthSpec, childHeightSpec);
            resultWidthSize += child.getMeasuredWidth();

        }

        /*最终完全确定宽度和高度结果。注意到如果我们不是平衡布局，那么宽度结果还要加上tag之间的距离。对于高度结果，我们只要随便
        * 取一个已经测量过的TextView将其高度加进去即可*/
        if(tags != null && tags.size() != 0)
        {
            resultHeightSize += tagMap.get(tags.get(0)).getMeasuredHeight();
            if(!balanceLayout)
            {
                resultWidthSize += (tags.size() - 1) * interval;
            }
        }

        /*结合父view传给我们的SpecMode来确定我们这个指示器layout的最终大小。如果是AT_MOST，那大小不能超过父View传给我们的SpecSize，
        * 如果是EXACTLY，那就直接将SpecSize作为我们的结果，而不管我们之前测量的宽度和高度结果*/
        if(widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST)
        {
            setMeasuredDimension(resultWidthSize > widthSize ? widthSize : resultWidthSize,
                    resultHeightSize > heightSize ? heightSize : resultHeightSize);
        }else if(widthMeasureSpec == MeasureSpec.AT_MOST)
        {
            setMeasuredDimension(resultWidthSize > widthSize ? widthSize : resultWidthSize, heightSize);
        }else if(heightMeasureSpec == MeasureSpec.AT_MOST)
        {
            setMeasuredDimension(widthSize, resultHeightSize > heightSize ? heightSize : resultHeightSize);
        }else
        {
            setMeasuredDimension(widthSize, heightSize);
        }


    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        if(changed)
        {
            layoutChildren(textOffset, currentPosition);
            tagMap.get(tags.get(currentPosition)).setTextColor(selectedTextColor);
        }


    }

    private void layoutChildren(int textOffset, int indicatorOffset, int indicatorLength)
    {

        log.v("layout children, textOffset = " + textOffset + ", indicatorOffset = " + indicatorOffset + ", indicatorLength = " + indicatorLength);
        if(tags.size() != 0)
        {
            /*计算TextView的顶部到指示器顶部的距离和底部到横线顶部的距离。由于我们的TextView是居中显示的（不），所以如下计算*/
            int padding = (getMeasuredHeight() - getPaddingBottom() - getPaddingTop() - indicatorHeight - tagMap.get(tags.get(0)).getMeasuredHeight()) / 2;
            if(padding < 0)
            {
                padding = 0;
            }
            if(balanceLayout)
            {
                /*如果是平衡布局，我们就要计算横向所剩余的空间，再将这些空间平分，作为tag之间的间距和tag与指示器前端和后端的距离*/
                int availableWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
                int totalItemWidth = 0;
                for(int i = 0; i < tags.size(); i++)
                {
                    totalItemWidth += tagMap.get(tags.get(i)).getMeasuredWidth();
                }
                int space = (availableWidth - totalItemWidth) / (tags.size() + 1);
                space = space < 0 ? 0 : space;
                textOffset = getPaddingLeft() + space;
                /*布局子view*/
                for(int i = 0; i < tags.size(); i++)
                {
                    TextView child = tagMap.get(tags.get(i));
                    int left = textOffset;
                    int right = textOffset + child.getMeasuredWidth();
                    child.layout(left,
                            getPaddingTop() + padding, right,
                            getPaddingTop() + child.getMeasuredHeight() + padding);

                    textOffset += space + child.getMeasuredWidth();
                }
            }else
            {
                for(String s : tags)
                {
                    TextView child = tagMap.get(s);
                    child.layout(textOffset, getPaddingTop() + padding, textOffset + child.getMeasuredWidth(), getPaddingTop() + child.getMeasuredHeight() + padding);
                    textOffset += child.getMeasuredWidth() + interval;
//                log.v(s + ": left=" + child.getLeft() + ", right=" + child.getRight() + ", top=" + child.getTop() + ", bottom=" + child.getBottom());
                }
            }

        }


        /*布局指示器的横线*/
        ViewGroup.LayoutParams layoutParams = indicatorLine.getLayoutParams();
        layoutParams.width = indicatorLength;
        indicatorLine.setLayoutParams(layoutParams);
        indicatorLine.layout(indicatorOffset, getMeasuredHeight() - getPaddingBottom() - indicatorHeight,
                indicatorOffset + indicatorLength, getMeasuredHeight() - getPaddingBottom());

    }

    private void layoutChildren(int textOffset, int index)
    {
        if(tags.size() != 0)
        {
            /*计算TextView的顶部到指示器顶部的距离和底部到横线顶部的距离。由于我们的TextView是居中显示的（不），所以如下计算*/
            int padding = (getMeasuredHeight() - getPaddingBottom() - getPaddingTop() - indicatorHeight - tagMap.get(tags.get(0)).getMeasuredHeight()) / 2;
            if(padding < 0)
            {
                padding = 0;
            }
            if(balanceLayout)
            {
                int availableWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
                /*如果是平衡布局，我们就要计算横向所剩余的空间，再将这些空间平分，作为tag之间的间距和tag与指示器前端和后端的距离*/
                int totalItemWidth = 0;
                for(int i = 0; i < tags.size(); i++)
                {
                    totalItemWidth += tagMap.get(tags.get(i)).getMeasuredWidth();
                }
                int space = (availableWidth - totalItemWidth) / (tags.size() + 1);
                space = space < 0 ? 0 : space;
                /*根据paddingLeft决定第一个TextView的偏离值*/
                textOffset = getPaddingLeft() + space;
                /*对子view进行布局*/
                for(int i = 0; i < tags.size(); i++)
                {
                    TextView child = tagMap.get(tags.get(i));
                    int left = textOffset;
                    int right = textOffset + child.getMeasuredWidth();
                    child.layout(left,
                            getPaddingTop() + padding, right,
                            getPaddingTop() + child.getMeasuredHeight() + padding);
                    /*更新offset*/
                    textOffset += space + child.getMeasuredWidth();
                }
            }else
            {
                /*如果不是平衡布局，直接按照传入的textOffset来布局，此时更新textOffset时使用tag之间的间距，即interval*/
                for(String s : tags)
                {
                    TextView child = tagMap.get(s);
                    child.layout(textOffset, getPaddingTop() + padding, textOffset + child.getMeasuredWidth(), getPaddingTop() + child.getMeasuredHeight() + padding);
                    textOffset += child.getMeasuredWidth() + interval;
//                log.v(s + ": left=" + child.getLeft() + ", right=" + child.getRight() + ", top=" + child.getTop() + ", bottom=" + child.getBottom());
                }
            }

            /*最后对横线进行布局*/
            ViewGroup.LayoutParams layoutParams = indicatorLine.getLayoutParams();
            layoutParams.width = tagMap.get(tags.get(index)).getMeasuredWidth();
            indicatorLine.setLayoutParams(layoutParams);
            int indicatorOffset = tagMap.get(tags.get(index)).getLeft();
            indicatorLine.layout(indicatorOffset, getMeasuredHeight() - getPaddingBottom() - indicatorHeight ,
                    indicatorOffset + layoutParams.width, getMeasuredHeight() - getPaddingBottom());
        }

    }


    @Override
    protected void onDraw(Canvas canvas) {

        log.v("onDraw");
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(backgroundColor);
        Rect rect = new Rect(this.getLeft(),this.getTop(), this.getRight(), this.getBottom());
        canvas.drawRect(rect, paint);

        for(String s : tags)
        {
            TextView child = tagMap.get(s);
            if(child.getLeft() <= getRight() || child.getRight() >= getLeft())
            {
                child.draw(canvas);
            }
        }

        if(indicatorLine.getLeft() <= getRight() || indicatorLine.getRight() >= getLeft())
        {
            indicatorLine.draw(canvas);
        }



    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    /*本次事件流是否已经被判断为滑动事件*/
    private boolean moved = false;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction())
        {
            /*按下的时候，对各个值进行赋值*/
            case MotionEvent.ACTION_DOWN:
                newX = event.getRawX();
                newY = event.getRawY();
                downX = newX;
                downY = newY;
                break;
            /*发生滑动时，先更新值，然后用前一次触摸点的坐标和本次坐标进行计算，如果x方向上的移动距离大于touchSlop，那么
            * 就判断为滑动。*/
            case MotionEvent.ACTION_MOVE:
                lastX = newX;
                lastY = newY;
                newX = event.getRawX();
                newY = event.getRawY();
                dx = newX - lastX;
                dy = newY - lastY;
                if(dx >= touchSlop)
                {
                    moved = true;
                }
                /*判断TextView的布局。tags太多的话，是允许滑动的，但是滑动也有限制，第一个tag的最左边不可以大于指示器的paddingLeft。
                * 最后一个tag的最右边不可以小于（height - paddingRight）。然后以这个条件来计算滑动后的第一个tag最左边的位置。
                * 并进行重新布局，横线的位置及长短也要相应改变。*/
                int left = tagMap.get(tags.get(0)).getLeft();
                int right = tagMap.get(tags.get(tags.size() - 1)).getRight();
                int length = right - left;
                if (length < getMeasuredWidth() - getPaddingLeft() - getPaddingRight())
                {

                }else if(left + dx > getPaddingLeft())
                {
                    left = getPaddingLeft();
                }else if(right + dx < (getMeasuredWidth() - getPaddingRight()))
                {
                    right = getMeasuredWidth() - getPaddingRight();
                    left = right - length;
                }else
                {
                    left += dx;
                }
                textOffset = left;
                layoutChildren(left, indicatorLine.getLeft() + (left - tagMap.get(tags.get(0)).getLeft()), indicatorLine.getWidth());

                break;
            /*抬起时，判断从起点到落点的距离是否超过了touchSlop，如果不是，我们就判断它是点击事件，执行点击回调函数。否则就什么也不做。另外
            * 将moved设为false，收尾。*/
            case MotionEvent.ACTION_UP:

                lastX = newX;
                lastY = newY;
                newX = event.getRawX();
                newY = event.getRawY();
                dx = newX - lastX;
                dy = newY - lastY;
                /*判断为点击事件*/
                if(!moved && Math.abs(newX - downX) < touchSlop && Math.abs(newY - downY) < touchSlop)
                {

                    for(int i = 0; i < tags.size(); i++)
                    {
                        TextView child = tagMap.get(tags.get(i));
                        if(child.getLeft() <= newX && child.getRight() >= newX)
                        {
                            notifyOnTagClickedListsners(i);
                            break;
                        }
                    }
                }
                moved = false;
                break;


        }
        return super.onTouchEvent(event);
    }

    /**使用在ViewPager.OnPageChangeListener.onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
    * 方法中，将三个参数原样传到该函数即可。
     * @param position 当前可见的第一个页面的序号，如果positionOffset不为0,那么position + 1页面也是可见的。
     * @param positionOffset 取值范围[0, 1)，表示当前position页面的偏离范围。
     * @param positionOffsetPixels 当前position页面的偏离值。
    * */
    public void listen(int position, float positionOffset, int positionOffsetPixels)
    {
        currentPosition = position;
        /*需要让被选中的tag完整地显示出来，因此在tag布局在指示器的显示范围之外时需要移动，并且修改选中的和未选中的字的颜色。*/
        if(positionOffset != 0.0f)
        {
            TextView current = tagMap.get(tags.get(position));
            TextView old = tagMap.get(tags.get(position + 1));
            int spaceBetweenTags = old.getLeft() - current.getLeft();
            int lineLeft = current.getLeft() + (int)(spaceBetweenTags * positionOffset);

            int currentLength = current.getWidth();
            int oldLength = old.getWidth();
            int lineLength = currentLength + (int)((oldLength - currentLength) * positionOffset);
            int lineRight = lineLeft + lineLength;
            if(lineLength >= (getMeasuredWidth() - getPaddingLeft() - getPaddingRight()))
            {
                textOffset += getPaddingLeft() - lineLeft;
                lineLeft = getPaddingLeft();
            }else if(lineLeft < getPaddingLeft())
            {
                textOffset += getPaddingLeft() - lineLeft;
                lineLeft = getPaddingLeft();
//                lineLength = lineRight - lineLeft;
            }else if(lineRight > (getMeasuredWidth() - getPaddingRight()))
            {
                textOffset -= lineRight - (getMeasuredWidth() - getPaddingRight());
                lineRight = (getMeasuredWidth() - getPaddingRight());
//                lineLength = lineRight - lineLeft;
                lineLeft = lineRight - lineLength;
            }
            layoutChildren(textOffset, lineLeft, lineLength);

            current.setTextColor(evaluateColor(textColor, selectedTextColor, 1 - positionOffset));
            old.setTextColor(evaluateColor(textColor, selectedTextColor, positionOffset));
        }else
        {
            /*positionOffset == 0时说明此时已经完成了页面切换，有且仅有一个页面是被完整显示的，此时只要根据被选择的序号来布局即可。关于颜色
            * 改变，因为在positionOffset == 0时我们已经丢失了页面切换的信息，所以无法得知上一个被选中的页面是哪个。另外，positionOffset == 0
            * 的情况实际上很极端，因此对于这种情况的处理并不影响大局，连这种情况的位置布局都可以不必考虑*/
            TextView current = tagMap.get(tags.get(position));
            if(current.getLeft() < getPaddingLeft())
            {
                textOffset += getPaddingLeft() - current.getLeft();
            }else if(current.getRight() > getMeasuredWidth() - getPaddingRight())
            {
                textOffset += getMeasuredWidth() - getPaddingRight() - current.getRight();
            }
            layoutChildren(textOffset, position);
        }

    }



    public void addTags(String[] tags)
    {
        for(String s : tags)
        {
            addTag(s);
        }
    }

    public void addTag(String tag, int... index)
    {
        log.v("add tag");

        TextView t = new TextView(getContext());

        ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        t.setLayoutParams(layoutParams);
        t.setText(tag);
        t.setTextColor(textColor);
        t.setTextSize(textSize);
        t.setPadding(2,2,2,2);

        if(index != null && index.length != 0)
        {
            if(index[0] <= currentPosition)
            {
                currentPosition++;
            }
            this.tags.add(index[0], tag);

        }else
        {
            this.tags.add(tag);
        }

        this.tagMap.put(tag, t);
        this.addView(t);

    }

    public void removeTag(String tag)
    {
        tags.remove(tag);
        tagMap.remove(tag);
        removeAllViews();
        layoutChildren(textOffset, currentPosition);
    }

    public int getCurrentPosition()
    {
        return currentPosition;
    }

    public void addOnTagClickedListener(OnTagClickedListener l)
    {
        if(!onTagClickedListeners.contains(l))
        {
            onTagClickedListeners.add(l);
        }
    }

    public void removeOnTagClickedListener(OnTagClickedListener l)
    {
        if(onTagClickedListeners.contains(l))
        {
            onTagClickedListeners.remove(l);
        }
    }

    /*回调监听器的方法，以当前被点击的tag序号为参数*/
    private void notifyOnTagClickedListsners(int position)
    {
        if(onTagClickedListeners.size() != 0)
        {
            for(OnTagClickedListener l : onTagClickedListeners)
            {
                l.onTagClicked(position);
            }
        }
    }

    /**tag点击监听器接口*/
    public interface OnTagClickedListener
    {
        public void onTagClicked(int position);
    }

    private int evaluateColor(int fromColor, int toColor, float percent)
    {
        int fromA = (fromColor >> 24) & 0xff;
        int fromR = (fromColor >> 16) & 0xff;
        int fromG = (fromColor >> 8) & 0xff;
        int fromB = fromColor & 0xff;

        int toA = (toColor >> 24) & 0xff;
        int toR = (toColor >> 16) & 0xff;
        int toG = (toColor >> 8) & 0xff;
        int toB = toColor & 0xff;

        int dA = (int)((toA - fromA) * percent);
        int dR = (int)((toR - fromR) * percent);
        int dG = (int)((toG - fromG) * percent);
        int dB = (int)((toB - fromB) * percent);

        int color = ((fromA + dA) << 24) | ((fromR + dR) << 16) | ((fromG + dG) << 8) | ((fromB + dB));
        return color;
    }

}
