package com.zu.customview;

import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.zu.customview.databinding.ActivityBindingBinding;


public class BindingActivity extends AppCompatActivity{

    ActivityBindingBinding binding = null;

    public int buttonClickCount = 0;
    public String editText = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_binding);
        binding.setActivity(this);
    }


    public void onButtonClick(View v) {
        buttonClickCount++;
    }


}
