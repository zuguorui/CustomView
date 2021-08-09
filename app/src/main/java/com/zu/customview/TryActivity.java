package com.zu.customview;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.widget.SeekBar;

import com.zu.customview.databinding.ActivityTryBinding;
import com.zu.customview.view.SlideLayout;
import com.zu.customview.view.SwitchLayout;

public class TryActivity extends AppCompatActivity {

    private EventHandler handler = new EventHandler();
    private ActivityTryBinding binding;
    private MyLog log = new MyLog("TryActivity", true);

    private boolean isPlaying = false;

    private SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
    private SwitchLayout.SwitchLayoutListener switchListener = new SwitchLayout.SwitchLayoutListener() {
        @Override
        public void beginSwitch(SwitchLayout.SWITCH_DIRECTION direction) {

        }

        @Override
        public void switching(SwitchLayout.SWITCH_DIRECTION direction, float process) {
            log.d("direction = " + direction.toString() + ", process = " + process);
            binding.ivLeftPlay.setAlpha(1f - process);
            binding.ivLeftPlaying.setAlpha(process);
            binding.ivRightPlay.setAlpha(process);
            binding.ivRightPlaying.setAlpha(1f - process);
        }

        @Override
        public void endSwitch(SwitchLayout.SWITCH_DIRECTION direction) {

        }

    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_try);
        binding.setHandler(handler);
        binding.switchLayout.setSwitchLayoutListener(switchListener);
        binding.sbPlayProgress.setOnSeekBarChangeListener(seekBarListener);

        initViews();
    }

    private void initViews()
    {
        float current = binding.switchLayout.getCurrentPercent();
        float base = binding.switchLayout.getSwitchPercent();
        float percent = (current - base) / (1 - 2 * base);
        binding.ivLeftPlay.setAlpha(1f - percent);
        binding.ivLeftPlaying.setAlpha(percent);
        binding.ivRightPlay.setAlpha(percent);
        binding.ivRightPlaying.setAlpha(1f - percent);

    }

    private void playStarted()
    {

    }

    private void playStopped()
    {

    }

    private void startPlay()
    {

    }

    private void stopPlay()
    {

    }

    private void playNext()
    {

    }

    private void playPrevious()
    {

    }



    public class EventHandler{
        public void onPlayClicked(){
            log.d("onPlayClicked");
        }

        public void onPlayPreviousClicked(){
            log.d("onPlayPreviousClicked");
        }

        public void onPlayNextClicked(){
            log.d("onPlayNextClicked");
        }

        public void onListClicked()
        {
            log.d("onListClicked");
        }




    }
}
