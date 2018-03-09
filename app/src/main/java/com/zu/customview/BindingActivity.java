package com.zu.customview;

import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.zu.customview.databinding.ActivityBindingBinding;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;


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

    public static String readFromFile(String path)
    {
        File file = new File(path);
        if(!file.exists() || file.isDirectory())
        {
            return "";
        }

        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        StringBuf
        try{
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String temp = null;
            while((temp = reader.readLine()) != null)
            {
                sb.append(temp);
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }finally {
            if(reader != null)
            {
                try{
                    reader.close();
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    public static void writeToFile(String data, String path)
    {
        if(path == null || path.length() == 0)
        {
            return;
        }
        if(data == null || data.length() == 0)
        {
            return;
        }
        File file = new File(path);
        if(file.isDirectory())
        {
            return;
        }

        BufferedWriter writer = null;
        try{
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
            writer.write(data);
            writer.flush();
        }catch (Exception e)
        {
            e.printStackTrace();
        }finally {
            if(writer != null)
            {
                try{
                    writer.close();
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

    }


}
