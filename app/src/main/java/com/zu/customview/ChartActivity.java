package com.zu.customview;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.zu.customview.utils.ChartUtil;

import java.util.Random;

public class ChartActivity extends AppCompatActivity {

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        imageView = (ImageView)findViewById(R.id.Chart_ImageView);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap coord = Bitmap.createBitmap(1000,1000, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(coord);
                //canvas.drawRGB(0xff, 0, 0);
                ChartUtil.CoordinateParam coordinateParam = new ChartUtil.CoordinateParam();
//                coordinateParam.startHAxis = 250;
//                coordinateParam.endHAxis = 8000;
//                coordinateParam.HAxisUnit = "Hz";
//                coordinateParam.startVAxis = 80;
//                coordinateParam.endVAxis = -20;
//                coordinateParam.VAxisUnit = "dp";

                coordinateParam.startHAxis = 0;
                coordinateParam.endHAxis = 5;
                coordinateParam.HAxisUnit = "Hz";
                coordinateParam.startVAxis = 80;
                coordinateParam.endVAxis = -20;
                coordinateParam.VAxisUnit = "dp";

                coordinateParam.HAxisValues = new double[]{0, 1, 2, 3, 4, 5};
                coordinateParam.HAxisLabels = new String[]{"250", "500", "1000", "2000", "4000", "8000"};

                coordinateParam.VAxisValues = new double[]{80, 60, 40, 20, 0, -20};

                ChartUtil.DataParam param = ChartUtil.drawCoordinate(canvas, coordinateParam);

                PointF[] data1 = new PointF[coordinateParam.HAxisValues.length];

                Random random = new Random();
                float[] nums = new float[]{18, 5, -5, -10, -15, 0};
                for(int i = 0; i < data1.length; i++)
                {
                    int index = random.nextInt(6);
                    data1[i] = new PointF(i, nums[index]);
                }

                param.data = data1;
                param.colors = new int[]{0x880000ff,0x220000ff,  0x000000ff};
                param.lineColor = 0xff0000ff;
                param.lineWidth = 6.0f;


//                Bitmap data1Bitmap = ChartUtil.drawData(param);

                Bitmap data1Background = ChartUtil.drawDataBackground(param);
                Bitmap data1Line = ChartUtil.drawDataLine(param);


                PointF[] data2 = new PointF[coordinateParam.HAxisValues.length];
                for(int i = 0; i < data1.length; i++)
                {
                    int index = random.nextInt(6);
                    data2[i] = new PointF(i, nums[index]);
                }

                param.data = data2;
                param.colors = new int[]{0x99ff0000,0x44ff0000, 0x00ff0000};
                param.lineColor = 0xff000000;
                param.pointStyle = ChartUtil.PointStyle.X;

                Bitmap data2Background = ChartUtil.drawDataBackground(param);
                Bitmap data2Line = ChartUtil.drawDataLine(param);
//                Bitmap data2Bitmap = ChartUtil.drawData(param);

//                Bitmap[] bitmaps = new Bitmap[]{coord, data1Bitmap, data2Bitmap};
//                PointF[] positions = new PointF[]{new PointF(0,0), new PointF(param.drawLeft, param.drawTop), new PointF(param.drawLeft, param.drawTop)};
                Bitmap[] bitmaps = new Bitmap[]{coord, data1Background, data2Background, data1Line, data2Line};
                PointF[] positions = new PointF[]{new PointF(0,0), new PointF(param.drawLeft, param.drawTop), new PointF(param.drawLeft, param.drawTop), new PointF(param.drawLeft, param.drawTop), new PointF(param.drawLeft, param.drawTop)};
                final Bitmap d = ChartUtil.mergeBitmap(bitmaps, positions, false );
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(d);
                    }
                });

            }
        }).start();
    }
}
