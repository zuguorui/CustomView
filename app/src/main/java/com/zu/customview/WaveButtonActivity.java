package com.zu.customview;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RadioButton;

import com.zu.customview.view.FlowRadioGroup;

public class WaveButtonActivity extends AppCompatActivity {

    FlowRadioGroup group;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wave_button);
        group = (FlowRadioGroup)findViewById(R.id.wave_activity_flow_radio_group);

        for(int i = 0; i < 10; i++)
        {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(i + "***");
            group.addView(radioButton);
        }
    }
}
