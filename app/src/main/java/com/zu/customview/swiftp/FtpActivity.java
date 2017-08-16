package com.zu.customview.swiftp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.zu.customview.R;

import java.net.InetAddress;

public class FtpActivity extends AppCompatActivity {

    private EditText userNameTextView;
    private EditText passwordTextView;
    private TextView statusTextView;
    private SwitchCompat startStopSwitch;
    private CheckBox allowAnonymousCheckBox;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            initRunningState();
            switch (intent.getAction())
            {
                case FsService.ACTION_STARTED:
                    Toast.makeText(FtpActivity.this, "服务器已启动", Toast.LENGTH_SHORT).show();
                    break;
                case FsService.ACTION_STOPPED:
                    Toast.makeText(FtpActivity.this, "服务器已停止", Toast.LENGTH_SHORT).show();
                    break;
                case FsService.ACTION_FAILEDTOSTART:
                    Toast.makeText(FtpActivity.this, "服务器启动失败", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ftp);
        initViews();
        initRunningState();


    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FsService.ACTION_FAILEDTOSTART);
        intentFilter.addAction(FsService.ACTION_STARTED);
        intentFilter.addAction(FsService.ACTION_STOPPED);
        registerReceiver(receiver, intentFilter);
        initRunningState();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receiver);
    }

    private void initViews()
    {
        userNameTextView = (EditText)findViewById(R.id.FtpActivity_editText_userName);
        passwordTextView = (EditText)findViewById(R.id.FtpActivity_editText_password);
        statusTextView = (TextView) findViewById(R.id.FtpActivity_textView_status);
        startStopSwitch = (SwitchCompat)findViewById(R.id.FtpActivity_switch_startStop);
        allowAnonymousCheckBox = (CheckBox)findViewById(R.id.FtpActivity_checkBox_allowAnonymous);

        userNameTextView.setText(FsSettings.getUserName());
        passwordTextView.setText(FsSettings.getPassWord());
        allowAnonymousCheckBox.setChecked(FsSettings.allowAnoymous());

        userNameTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus)
                {
                    String text = userNameTextView.getText().toString();
                    if(!text.equals(FsSettings.getUserName()))
                    {
                        FsSettings.setUserName(text);
                    }

                }
            }
        });

        passwordTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus)
                {
                    String text = passwordTextView.getText().toString();
                    if(!text.equals(FsSettings.getPassWord()))
                    {
                        FsSettings.setPassWord(text);
                    }
                }
            }
        });

        allowAnonymousCheckBox.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus)
                {
                    if(allowAnonymousCheckBox.isChecked() != FsSettings.allowAnoymous())
                    {
                        FsSettings.setAllowAnonymous(allowAnonymousCheckBox.isChecked());
                    }
                }
            }
        });

        startStopSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    Intent intent = new Intent(FsService.ACTION_START_FTPSERVER);
                    sendBroadcast(intent);
                }else
                {
                    Intent intent = new Intent(FsService.ACTION_STOP_FTPSERVER);
                    sendBroadcast(intent);
                }
            }
        });

    }

    private void initRunningState()
    {
        boolean isServerRunning = FsService.isRunning();
        userNameTextView.setEnabled(!isServerRunning);
        passwordTextView.setEnabled(!isServerRunning);
        startStopSwitch.setChecked(isServerRunning);
        allowAnonymousCheckBox.setEnabled(!isServerRunning);
        if(isServerRunning)
        {
            InetAddress address = FsService.getLocalInetAddress();
            StringBuilder sb = new StringBuilder();
            sb.append("FTP服务正在运行\n，地址为ftp://");
            sb.append(address.getHostAddress() + ":" + FsSettings.getPortNumber());
            statusTextView.setText(sb.toString());
        }else
        {
            statusTextView.setText("FTP服务未在运行");
        }
    }
}
