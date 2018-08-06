package com.zu.customview.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.zu.customview.App;
import com.zu.customview.MyLog;

/**
 * Created by rikson on 2018/5/22.
 */

public class VolumeLimitReceiver extends BroadcastReceiver {

    static String TAG = "VolumeLimitReceiver";
    static AudioManager audioManager = (AudioManager) App.getAppContext().getSystemService(Context.AUDIO_SERVICE);
    static MyLog log = new MyLog("VolumeLimitReceiver", true);


    @Override
    public void onReceive(Context context, Intent intent) {
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentColume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        Log.d(TAG,"maxVolume = " + maxVolume);
        Log.d(TAG, "currentVolume = " + currentColume);
        if(currentColume * 1.0f / maxVolume > 0.7f)
        {
            Toast.makeText(context, "声音过大会损伤听力", Toast.LENGTH_SHORT).show();
        }



    }
}
