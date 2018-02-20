package com.zu.customview.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.util.Pair;

import java.text.DecimalFormat;

/**
 * Created by zu on 2018/2/17.
 */

/**
 * 绘制折线图的工具，参数丰富，可根据参数来指定多种图表样式。其中所涉及到的Bitmap.Config都为ARGB_8888
 * 使用步骤：
 * 1、手动初始化一个Bitmap，并用它初始化一个Canvas；设置坐标系参数{@link CoordinateParam}，并将两者传入{@link #drawCoordinate(Canvas, CoordinateParam)}开始绘制，并得到一个{@link DataParam}，
 *      其中{@link DataParam#width}、{@link DataParam#height}、{@link DataParam#drawTop}、{@link DataParam#drawLeft} {@link DataParam#drawBottom} {@link DataParam#drawRight}
 *      {@link DataParam#startHAxis} {@link DataParam#endHAxis} {@link DataParam#startVAxis} {@link DataParam#endVAxis}都已经根据绘制结果设置好了。
 * 2、继续为第1步得到的DataParam设置数据等其他一些参数，然后将其传入{@link #drawDataLine(DataParam)}，得到一张绘制了折线和坐标点的Bitmap。
 * 3、如果需要设置背景，那么仍然为第1步拿到的DataParam设置背景参数，并将其传入{@link #drawDataBackground(DataParam)}，会得到一张绘制折线图背景的Bitmap。
 * 4、需要使用{@link #mergeBitmap(Bitmap[], PointF[], boolean)}将上面几步得到的Bitmap合成为一张。通常如下设置参数：
 *      <code>
 *          Bitmap[] bitmaps = new Bitmap[]{CoordinateBitmap, DataBackgroundBitmap, DataLineBitmap};
 *          PointF point1 = new PointF(0,0);
 *          PointF point2 = new PointF(DataParam.drawLeft, DataParam.drawTop);
 *          PointF[] positions = new PointF[]{point1, point2, point2};
 *          Bitmap result = mergeBitmap(bitmaps, positions, true or false);
 *      </code>
 *      调节bitmaps和positions中元素的位置可达到不同的叠加效果。
 *
 * */
public class ChartUtil {

    public enum Orientation{Horizontal, Vertical}
    public enum PointStyle{X, O, FillO}

    public static Bitmap drawGradientBackground(Point[] edgePoints, int[] colors, Orientation orientation, int width, int height)
    {
        if(width <= 0 || height <= 0)
        {
            return null;
        }
        Bitmap b = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(b);

        Path path = new Path();

        path.moveTo(0, 20);
        path.lineTo(20,30);
        path.lineTo(40, 15);
        path.lineTo(60, 20);
        path.lineTo(80, 10);
        path.lineTo(100, 30);
        path.lineTo(100,0);
        path.lineTo(0,0);
        path.close();

        canvas.clipPath(path);

        LinearGradient linearGradient = new LinearGradient(0, 0, 0, 100, 0xffff00ff, 0x00ff00ff, Shader.TileMode.CLAMP);
        Paint paint = new Paint();

        paint.setShader(linearGradient);
        canvas.drawRect(new Rect(0,0,100,100), paint);

        canvas.save();

        return b;

    }


    /**
     * 绘制坐标系。
     * @param canvas 具体用来绘制的画布，长宽都必须大于0
     * @param params 坐标轴参数
     * @return 折线图对应的数据的参数，其中width、height、drawTop、drawLeft、drawBottom、drawRight都已经根据绘制结果设置好了。
     *          startHAxis、endHAxis、startVAxis、endVAxis也根据传入的params设置为相同的数据
     *
     * */
    public static DataParam drawCoordinate(@NonNull Canvas canvas, CoordinateParam params)
    {
        if(canvas.getWidth() <= 0 || canvas.getHeight() == 0)
        {
            throw new IllegalArgumentException("The height and width of canvas must bigger than 0");
        }
        if(params.VAxisValues == null)
        {
            throw new IllegalArgumentException("There is no available VAxisNums");
        }

        if((params.HAxisLabels != null && params.HAxisLabels.length != params.HAxisValues.length) || (params.VAxisLabels != null && params.VAxisLabels.length != params.VAxisValues.length))
        {
            throw new IllegalArgumentException("If you passed the labels of axis, you must keep its length the same as values");
        }
//        if(params.startVAxis >= params.endVAxis || params.startHAxis >= params.endHAxis)
//        {
//            throw new IllegalStateException("The start of axis should be less than the end");
//        }

        float textPaddingRight = params.textPaddingRight, textPaddingTop = params.textPaddingTop;

        //初始化返回值的一些数据
        DataParam dataParam = new DataParam();
        dataParam.width = canvas.getWidth();
        dataParam.height = canvas.getHeight();

        dataParam.startHAxis = params.startHAxis;
        dataParam.endHAxis = params.endHAxis;
        dataParam.startVAxis = params.startVAxis;
        dataParam.endVAxis = params.endVAxis;

        //初始化横纵坐标显示数据的格式器
        DecimalFormat vFormatter = null, hFormatter = null;

        if(params.vFormatter == null)
        {
            vFormatter = new DecimalFormat("0.0");
        }else
        {
            vFormatter = params.vFormatter;
        }

        if(params.hFormatter == null)
        {
            hFormatter = new DecimalFormat("0");
        }else
        {
            hFormatter = params.hFormatter;
        }
        //文字最大的宽度和高度，以此来计算坐标图的绘制区域，计算文字尺寸前，要先为Paint设置文字大小
        float maxTextWidth = 0f, maxTextHeight = 0f;
        Paint textPaint = new Paint();
        textPaint.setTextSize(params.textSize);
        //坐标轴显示文字
        String[] vLabels = null;
        String[] hLabels = null;


        //初始化横纵坐标显示的文字
        if(params.VAxisLabels != null)
        {
            vLabels = params.VAxisLabels;
            for(int i = 0; i < vLabels.length; i++)
            {
                String t = vFormatter.format(vLabels[i]);
                float width = textPaint.measureText(t);
                vLabels[i] = t;
                if(maxTextWidth < width)
                {
                    maxTextWidth = width;
                }
            }
        }else
        {
            vLabels = new String[params.VAxisValues.length];
            for(int i = 0; i < vLabels.length; i++)
            {
                double num = params.VAxisValues[i];
                String t = vFormatter.format(num);
                float width = textPaint.measureText(t);
                vLabels[i] = t;
                if(maxTextWidth < width)
                {
                    maxTextWidth = width;
                }
            }
        }

        if(params.HAxisLabels != null)
        {
            hLabels = params.HAxisLabels;
        }else
        {
            hLabels = new String[params.HAxisValues.length];
            for(int i = 0; i < hLabels.length; i++)
            {
                double num = params.HAxisValues[i];
                String t = hFormatter.format(num);

                hLabels[i] = t;

            }
        }



        //计算文字高度的方法
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        maxTextHeight = fm.descent - fm.top;
        float vTextHeight = fm.leading - fm.ascent;
        fm.leading = 0;


        //计算坐标图绘制区域并将其设置到返回值中
        dataParam.drawLeft = (int)(params.paddingLeft + maxTextWidth + textPaddingRight);
        dataParam.drawBottom = canvas.getHeight() - (int)(params.paddingBottom + maxTextHeight * 2 + textPaddingTop * 2);
        dataParam.drawTop = (int)(params.paddingTop + maxTextHeight * 1.5f + textPaddingTop);
        float lastWidth = Math.max(textPaint.measureText(params.HAxisUnit), textPaint.measureText(hLabels[hLabels.length - 1]));
        dataParam.drawRight = canvas.getWidth() - (int)(params.paddintRight + lastWidth * 0.5f);


        double vLength = dataParam.drawBottom - dataParam.drawTop;
        double hLength = dataParam.drawRight - dataParam.drawLeft;
        double vRange = params.endVAxis - params.startVAxis;
        double hRange = params.endHAxis - params.startHAxis;





        textPaint.setElegantTextHeight(true);

        textPaint.setTextAlign(Paint.Align.RIGHT);
        textPaint.setColor(params.textColor);
        textPaint.setTextSize(params.textSize);
        textPaint.setAntiAlias(true);
        Paint linePaint = new Paint();
        linePaint.setColor(params.axisColor);
        linePaint.setStrokeWidth(params.axisWidth);
        //绘制纵坐标单位
        canvas.drawText(params.VAxisUnit, dataParam.drawLeft - textPaddingRight, dataParam.drawTop - textPaddingTop - maxTextHeight / 2, textPaint);

        for(int i = 0; i < params.VAxisValues.length; i++)
        {
            double num = params.VAxisValues[i];
            String str = vLabels[i];
            double dis = (num - params.startVAxis) / vRange * vLength;
            float linePos = dataParam.drawBottom - (float)dis;
            //绘制纵坐标数值
            canvas.drawText(str, dataParam.drawLeft - textPaddingRight, linePos + vTextHeight / 2, textPaint);
            //如果要求绘制纵坐标线
            if(params.drawVAxis)
            {
                canvas.drawLine(dataParam.drawLeft, linePos, dataParam.drawRight, linePos, linePaint);
            }

        }

        //绘制横坐标数据
        textPaint.setTextAlign(Paint.Align.CENTER);
        for(int i = 0; i < params.HAxisValues.length; i++)
        {
            double num = params.HAxisValues[i];
            String str = hLabels[i];
            double dis = (num - params.startHAxis) / hRange * hLength;
            float linePos = dataParam.drawLeft + (float)dis;
            float textBaseline = dataParam.drawBottom + textPaddingTop + maxTextHeight;
            canvas.drawText(str, linePos, textBaseline, textPaint);
            textBaseline += maxTextHeight;
            canvas.drawText(params.HAxisUnit, linePos, textBaseline, textPaint);

            if(params.drawHAxis)
            {
                canvas.drawLine(linePos, dataParam.drawTop, linePos, dataParam.drawBottom, linePaint);
            }
        }



        return dataParam;

    }

    public static Bitmap drawData(DataParam param)
    {
        if(param == null)
        {
            return null;
        }

        if(param.data == null || param.data.length == 0)
        {
            return null;
        }
        if(param.drawBottom - param.drawTop <= 0 || param.drawRight - param.drawLeft <= 0)
        {
            throw new IllegalArgumentException("The width and height of draw area should be bigger than 0");
        }

        PointF[] drawPoints = new PointF[param.data.length];
        double vLength = param.drawBottom - param.drawTop;
        double hLength = param.drawRight - param.drawLeft;
        double vRange = param.endVAxis - param.startVAxis;
        double hRange = param.endHAxis - param.startHAxis;

        for(int i = 0; i < param.data.length; i++)
        {
            PointF point = param.data[i];
            float x = (float)((point.x - param.startHAxis) / hRange * hLength);
            float y = (float)((point.y - param.startVAxis) / vRange * vLength);
            drawPoints[i] = new PointF(x,y);
        }




        Bitmap bitmap = Bitmap.createBitmap((int)Math.abs(param.drawRight - param.drawLeft), (int)Math.abs(param.drawBottom - param.drawTop), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        //先剪裁，然后绘制背景，再撤销剪裁

        canvas.save();

        Path path = new Path();
        //由于Android View的坐标原点是在左上角，y向下为正，x向右为正，因此需要转换一下。
        path.moveTo(drawPoints[0].x, canvas.getHeight() - drawPoints[0].y);

        for(int i = 1; i < drawPoints.length; i++)
        {
            path.lineTo(drawPoints[i].x, canvas.getHeight() - drawPoints[i].y);
        }

//        Paint linePaint = new Paint();
//        linePaint.setStrokeWidth(param.lineWidth);
//        linePaint.setColor(param.lineColor);
//
//        backgroundCanvas.drawPath(path, linePaint);
//
//        backgroundCanvas.save();

        path.lineTo(canvas.getWidth(), canvas.getHeight());
        path.lineTo(0, canvas.getHeight());
        path.close();

        canvas.clipPath(path);

        if(param.colors != null)
        {
            Paint backgroundPaint = new Paint();
            LinearGradient linearGradient = null;
            if(param.colorPosition != null)
            {

                if(param.colorPosition.length != param.colors.length)
                {
                    throw new IllegalArgumentException("If you set the 'colorPosition' of DataParam, you should keep it have the same length as 'colors'");
                }


            }
            if(param.orientation == Orientation.Horizontal)
            {
                linearGradient = new LinearGradient(0, 0, canvas.getWidth(), 0,param.colors, param.colorPosition, Shader.TileMode.CLAMP);
            }else{

                linearGradient = new LinearGradient(0, 0, 0, canvas.getHeight(), param.colors, param.colorPosition, Shader.TileMode.CLAMP);
            }

            backgroundPaint.setShader(linearGradient);
            backgroundPaint.setAntiAlias(true);
            canvas.drawRect(0,0,canvas.getWidth(), canvas.getHeight(), backgroundPaint);

        }


        canvas.restore();

        //绘制折线



        Paint linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStrokeWidth(param.lineWidth);
        linePaint.setColor(param.lineColor);

        for(int i = 0; i < drawPoints.length - 1; i++)
        {
            float left = drawPoints[i].x;
            float top = canvas.getHeight() - drawPoints[i].y;
            float right = drawPoints[i + 1].x;
            float bottom  = canvas.getHeight() - drawPoints[i + 1].y;
            canvas.drawLine(left, top, right, bottom, linePaint);
        }




        //绘制坐标点
        for(int i = 0; i < drawPoints.length; i++)
        {
            PointF point = drawPoints[i];
            drawPoint(canvas, point.x, canvas.getHeight() - point.y, param.pointStyle, param.pointSize, param.pointSize, param.lineColor, param.lineWidth);
        }

        return bitmap;

    }

    /**
     * 绘制折线图背景
     * */
    public static Bitmap drawDataBackground(DataParam param)
    {
        Bitmap bitmap = Bitmap.createBitmap((int)Math.abs(param.drawRight - param.drawLeft), (int)Math.abs(param.drawBottom - param.drawTop), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        PointF[] drawPoints = computeDrawPoints(param);

        //先剪裁，然后绘制背景，再撤销剪裁

        canvas.save();


        Path path = new Path();
        //由于Android View的坐标原点是在左上角，y向下为正，x向右为正，因此需要转换一下。
        path.moveTo(drawPoints[0].x, canvas.getHeight() - drawPoints[0].y);

        for(int i = 1; i < drawPoints.length; i++)
        {
            path.lineTo(drawPoints[i].x, canvas.getHeight() - drawPoints[i].y);
        }

//        Paint linePaint = new Paint();
//        linePaint.setStrokeWidth(param.lineWidth);
//        linePaint.setColor(param.lineColor);
//
//        backgroundCanvas.drawPath(path, linePaint);
//
//        backgroundCanvas.save();

        path.lineTo(canvas.getWidth(), canvas.getHeight());
        path.lineTo(0, canvas.getHeight());
        path.close();

        canvas.clipPath(path);

        if(param.colors != null)
        {
            Paint backgroundPaint = new Paint();
            LinearGradient linearGradient = null;

            if(param.colorPosition != null)
            {

                if(param.colorPosition.length != param.colors.length)
                {
                    throw new IllegalArgumentException("If you set the 'colorPosition' of DataParam, you should keep it have the same length as 'colors'");
                }


            }
            if(param.orientation == Orientation.Horizontal)
            {
                linearGradient = new LinearGradient(0, 0, canvas.getWidth(), 0,param.colors, param.colorPosition, Shader.TileMode.CLAMP);
            }else{

                linearGradient = new LinearGradient(0, 0, 0, canvas.getHeight(), param.colors, param.colorPosition, Shader.TileMode.CLAMP);
            }

            backgroundPaint.setShader(linearGradient);
            backgroundPaint.setAntiAlias(true);
            canvas.drawRect(0,0,canvas.getWidth(), canvas.getHeight(), backgroundPaint);

        }


        canvas.restore();
        return bitmap;
    }


    /**
     * 绘制折线图的线和数据点
     * */
    public static Bitmap drawDataLine(DataParam param)
    {
        //绘制折线

        Bitmap bitmap = Bitmap.createBitmap((int)Math.abs(param.drawRight - param.drawLeft), (int)Math.abs(param.drawBottom - param.drawTop), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        PointF[] drawPoints = computeDrawPoints(param);

        Paint linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStrokeWidth(param.lineWidth);
        linePaint.setColor(param.lineColor);

        for(int i = 0; i < drawPoints.length - 1; i++)
        {
            float left = drawPoints[i].x;
            float top = canvas.getHeight() - drawPoints[i].y;
            float right = drawPoints[i + 1].x;
            float bottom  = canvas.getHeight() - drawPoints[i + 1].y;
            canvas.drawLine(left, top, right, bottom, linePaint);
        }




        //绘制坐标点
        for(int i = 0; i < drawPoints.length; i++)
        {
            PointF point = drawPoints[i];
            drawPoint(canvas, point.x, canvas.getHeight() - point.y, param.pointStyle, param.pointSize, param.pointSize, param.lineColor, param.lineWidth);
        }

        return bitmap;

    }

    /**
     * 将数据参数中带的数据根据坐标轴的数据转换成实际在图中绘制的位置
     * */
    private static PointF[] computeDrawPoints(DataParam param)
    {
        if(param == null)
        {
            return null;
        }

        if(param.data == null || param.data.length == 0)
        {
            return null;
        }


        PointF[] drawPoints = new PointF[param.data.length];
        double vLength = param.drawBottom - param.drawTop;
        double hLength = param.drawRight - param.drawLeft;
        double vRange = param.endVAxis - param.startVAxis;
        double hRange = param.endHAxis - param.startHAxis;

        for(int i = 0; i < param.data.length; i++)
        {
            PointF point = param.data[i];
            float x = (float)((point.x - param.startHAxis) / hRange * hLength);
            float y = (float)((point.y - param.startVAxis) / vRange * vLength);
            drawPoints[i] = new PointF(x,y);
        }

        return drawPoints;
    }

    /**
     * 绘制坐标点
     * */
    public static void drawPoint(Canvas canvas, float x, float y, PointStyle style, float width, float height, int color, float strokeWidth)
    {
        if(style == PointStyle.FillO)
        {
            drawFillOPoint(canvas, x, y, width / 2, color);
        }else if(style == PointStyle.O)
        {
            drawOPoint(canvas, x, y, width / 2, color, strokeWidth);
        }else if(style == PointStyle.X)
        {
            drawXPoint(canvas, x, y, width, height, color, strokeWidth);
        }
    }

    public static void drawXPoint(Canvas canvas, float x, float y, float width, float height, int color, float strokeWidth )
    {
        Path path = new Path();
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStrokeWidth(strokeWidth);
        paint.setAntiAlias(true);

        float left = x - width / 2;
        float right = left + width;
        float top = y - height / 2;
        float bottom = top + height;

//        path.moveTo(left, top);
//        path.lineTo(right, bottom);
//        path.moveTo(right, top);
//        path.lineTo(left, bottom);
        canvas.drawLine(left, top, right, bottom, paint);
        canvas.drawLine(left, bottom, right, top, paint);
        canvas.drawPath(path, paint);
    }

    public static void drawOPoint(Canvas canvas, float x, float y, float radius, int color, float strokeWidth)
    {
        drawFillOPoint(canvas, x, y, radius, 0xffffffff);

        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        canvas.drawCircle(x, y, radius, paint);


    }

    public static void drawFillOPoint(Canvas canvas, float x, float y, float radius, int color)
    {
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setAntiAlias(true);

        canvas.drawCircle(x, y, radius, paint);
    }

    /**
     * 将多张Bitmap合成为一张
     * @param bitmaps 源Bitmap，合唱顺序为从0开始，后一张会叠加在前一张的上面
     * @param drawPosition 每个源Bitmap在最终合成的Bitmap中的位置的左上角坐标
     * @param recycleSourceAfterMerged 是否在合成结束后回收所有传入的Bitmap，因为最终合成的Bitmap是单独生成的。
     * @return 最终合成的结果
     * */
    public static Bitmap mergeBitmap(Bitmap[] bitmaps,PointF[] drawPosition,  boolean recycleSourceAfterMerged)
    {
        if(bitmaps == null || bitmaps.length == 0)
        {
            return null;
        }else if(bitmaps.length == 1)
        {
            return bitmaps[0];
        }

        if(drawPosition != null && drawPosition.length != bitmaps.length)
        {
            throw new IllegalArgumentException("If you set 'drawPosition', you should keep it has the same length as 'bitmaps'");
        }

        int maxWidth = 0;
        int maxHeight = 0;
        for(Bitmap b : bitmaps)
        {
            int wTemp = b.getWidth();
            int hTemp = b.getHeight();
            if(maxHeight < hTemp)
            {
                maxHeight = hTemp;
            }
            if(maxWidth < wTemp)
            {
                maxWidth = wTemp;
            }
        }

        if(maxWidth == 0 || maxHeight == 0)
        {
            throw new IllegalArgumentException("You should pass at least one Bitmap who's width and height are bigger than 0");
        }

        Bitmap bitmap = Bitmap.createBitmap(maxWidth, maxHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        for(int i = 0; i < bitmaps.length; i++)
        {
            Bitmap b = bitmaps[i];
            if(drawPosition != null)
            {
                canvas.drawBitmap(b, drawPosition[i].x, drawPosition[i].y, null);
            }else
            {
                canvas.drawBitmap(b, 0, 0, null);
            }

            if(recycleSourceAfterMerged)
            {
                b.recycle();
            }
        }



        return bitmap;
    }

    /**
    * 坐标系参数，{@link #drawCoordinate(Canvas, CoordinateParam)}将会根据该参数来生成坐标系。
    * */
    public static class CoordinateParam{
        //横纵坐标显示的单位
        public String HAxisUnit = null, VAxisUnit = null;
        //横纵坐标轴的值，这些值是用来计算的。
        public double[] HAxisValues = null, VAxisValues = null;
        //横纵坐标轴的显示的值，如果设置了这两个参数，那么最终图中横纵坐标显示的数据将会是这些labels而不是HAxisValues和VAxisValues。
        //如果不设置，那么会根据hFormatter和vFormatter来格式化对应坐标轴的values作为坐标轴显示的数据。
        public String[] HAxisLabels = null, VAxisLabels = null;
        public DecimalFormat hFormatter = null, vFormatter = null;

        //作用于绘图canvas的padding值
        public float paddingLeft = 0f, paddintRight = 0f, paddingTop = 0f, paddingBottom = 0f;
        public int textColor = 0xff000000, axisColor = 0xff000000, backgroundColor = 0x00000000;

        //是否绘制横纵坐标
        public boolean drawVAxis = true, drawHAxis = true;
        //坐标图中文字的大小
        public float textSize = 30f;
        //坐标线的宽度
        public float axisWidth = 2.0f;
        //横纵坐标轴的起止范围
        public double startVAxis = 0, endVAxis = 0, startHAxis = 0, endHAxis = 0;
        //分别为纵、横坐标的显示文字距离坐标轴的距离
        public float textPaddingRight = 5, textPaddingTop = 5;

    }

    /**
     * 绘制数据图的参数
     * */
    public static class DataParam{
        //width和height是对应整个坐标图的尺寸，而剩下的四个参数则是代表折线图的绘制区域在坐标图中的位置
        public float width = 0f, height = 0f, drawTop = 0f, drawLeft = 0f, drawBottom = 0f, drawRight = 0f;
        //传入的数据，必要
        public PointF[] data = null;
        //数据点显示的位置标记，现在还没有实现这个功能
        public Pair<String, String>[] pointLabels = null;
        //数据图的背景颜色，可以实现渐变效果，可不设置
        public int[] colors = null;
        //颜色对应的位置，可不设置
        public float[] colorPosition = null;
        //背景色渐变的方向
        public Orientation orientation = Orientation.Vertical;
        //折线的颜色，对应点的颜色也是这个
        public int lineColor;
        //数据点的样式，可为X、圆形和填充圆形三种
        public PointStyle pointStyle = PointStyle.O;
        //折线的宽度，同时也是数据点中线的宽度
        public float lineWidth = 3f;
        //数据点绘制的尺寸
        public float pointSize = 15;
        //横纵坐标轴的起止范围
        public double startVAxis = 0, endVAxis = 0, startHAxis = 0, endHAxis = 0;
    }
}
