package com.zu.customview;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.SeekBar;

import com.zu.customview.databinding.ActivityTryBinding;
import com.zu.customview.view.SlideLayout;
import com.zu.customview.view.SwitchLayout;

public class TryActivity extends AppCompatActivity {

    private EventHandler handler = new EventHandler();
    private ActivityTryBinding binding;

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

    }

    private class EventHandler{
        public void onPlayClicked(){

        }

        public void onPlayPreviousClicked(){

        }

        public void onPlayNextClicked(){

        }

        public void onListClicked()
        {

        }




    }
}
