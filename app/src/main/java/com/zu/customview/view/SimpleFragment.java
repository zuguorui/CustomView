package com.zu.customview.view;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.fragment.app.Fragment;

import com.zu.customview.R;


/**
 * Created by zu on 17-3-10.
 */

public class SimpleFragment extends Fragment {

    private String TAG = "SimpleFragment";

    private TextView textView;
    private int color = Color.RED;
    private String text = "Hello";
    public SimpleFragment() {
        Log.d(TAG + text, "constructor");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG + text, "onCreate");
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG + text, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_simple, null, false);

        textView = (TextView)view.findViewById(R.id.fragment_text);
//        imageView.setBackgroundColor(color);
        textView.setText(text);
        return view;
    }

    public void setColor(int color)
    {

        this.color = color;
    }

    public void setTextView(String text)
    {
        this.text = text;
    }


}
