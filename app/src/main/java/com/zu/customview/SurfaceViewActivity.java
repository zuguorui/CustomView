package com.zu.customview;

import android.graphics.PixelFormat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.zu.customview.view.FreqSurfaceView;

public class SurfaceViewActivity extends AppCompatActivity {

    private FreqSurfaceView surfaceView;
    private int start = 0;
    Thread t = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surface_view);
        surfaceView = findViewById(R.id.surfaceView);
        surfaceView.setPeriod(4);

        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        t = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true)
                {
                    float data = Math.abs(FreqSurfaceView.sinTable[start] * 1.0f / Short.MAX_VALUE);
                    surfaceView.setData(data);
                    start += 5;
                    if(start >= FreqSurfaceView.sinTable.length)
                    {
                        start = start % FreqSurfaceView.sinTable.length;
                    }
                    try{
                        Thread.sleep(100);
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }

            }
        });
        t.start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        surfaceView.setDrawContent(false);
        if(t != null){
            t.interrupt();
        }
    }
}
