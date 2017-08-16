package com.zu.customview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.zu.customview.view.ItemAdapter;
import com.zu.customview.view.ZoomView;

public class ZoomViewActivity extends AppCompatActivity {

    private ZoomView zoomView ;

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
                case 0:
                    Toast.makeText(ZoomViewActivity.this, "Long click " + msg.arg1, Toast.LENGTH_SHORT).show();
                    zoomView.setMultiCheckMode(true);
                    zoomView.setCheckedItems(new int[]{msg.arg1});
                    break;
                case 1:
                    Toast.makeText(ZoomViewActivity.this, "click " + msg.arg1, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom_view);
        zoomView = (ZoomView)findViewById(R.id.ZoomViewActivity_zoomView);
        createDebugData();

        zoomView.addOnItemLongClickListener(new ZoomView.OnItemLongClickListener() {
            @Override
            public void onLongClick(int position) {
                Log.v("ZoomViewActivity", "LongClick");
                Message message = Message.obtain(null, 0);
                message.arg1 = position;
                mHandler.sendMessage(message);
            }
        });
        zoomView.addOnItemClickListener(new ZoomView.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                Log.v("ZoomViewActivity", "Click");
                Message message = Message.obtain(null, 1);
                message.arg1 = position;
                mHandler.sendMessage(message);
            }
        });


    }

    private void createDebugData()
    {
        final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.chuyin);

        ItemAdapter itemAdapter = new ItemAdapter() {
            int count = 5;
            @Override
            public Object getItem(int position, ZoomView.ItemManager.Callback callback) {
                if(position >= 0 && position < count)
                {
                    return bitmap.copy(Bitmap.Config.ARGB_4444, false);
                }else
                {
                    return null;
                }
            }

            @Override
            public int getCount() {
                return count;
            }

            @Override
            public Object getItem(int position) {
                if(position >= 0 && position < count)
                {
                    return bitmap.copy(Bitmap.Config.ARGB_4444, false);
                }else
                {
                    return null;
                }
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return null;
            }
        };
        zoomView.setAdapter(itemAdapter);
    }
}
