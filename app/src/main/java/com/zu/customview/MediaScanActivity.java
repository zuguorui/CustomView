package com.zu.customview;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.File;
import java.security.Permission;
import java.security.Permissions;

public class MediaScanActivity extends AppCompatActivity {

    private TextView textView ;
    private MediaScannerConnection.MediaScannerConnectionClient client;
    private MediaScannerConnection connection;
    private String[] paths;


    private Handler mHandler = new Handler();

    private MediaScannerConnection.OnScanCompletedListener onScanCompletedListener = new MediaScannerConnection.OnScanCompletedListener() {
        @Override
        public void onScanCompleted(final String path, final Uri uri) {
            mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        textView.append("path: " + path + "\nuri: " + uri.toString() + "\n");
                    }
                });
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_scan);
        textView = (TextView)findViewById(R.id.MediaScan_textView_uris);
        File folder = new File("/sdcard/DCIM/Camera");
        File[] files = folder.listFiles();
        paths = new String[files.length];
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }
        }

        for(int i = 0; i < files.length; i++)
        {
            paths[i] = files[i].getPath();
        }

//        client = new MediaScannerConnection.MediaScannerConnectionClient() {
//            @Override
//            public void onMediaScannerConnected() {
//                mHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        textView.append("Scan started\n");
//                        connection.scanFile("/sdcard/DCIM/Camera/", null);
//                    }
//                });
//            }
//
//            @Override
//            public void onScanCompleted(final String path, final Uri uri) {
//                mHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        textView.append("path: " + path + "\nuri: " + uri.toString());
//                    }
//                });
//            }
//        };
//
//        connection = new MediaScannerConnection(this, client);


        new Thread(new Runnable() {
            @Override
            public void run() {
                MediaScannerConnection.scanFile(MediaScanActivity.this, paths, null, onScanCompletedListener);
            }
        }).start();
    }
}
