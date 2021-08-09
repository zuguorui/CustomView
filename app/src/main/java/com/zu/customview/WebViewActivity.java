package com.zu.customview;


import android.provider.ContactsContract;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.zu.customview.databinding.ActivityWebViewBinding;

public class WebViewActivity extends AppCompatActivity {

    ActivityWebViewBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_web_view);
//        binding.wvWeb.setWebViewClient(new WebViewClient() {
//            //覆盖shouldOverrideUrlLoading 方法
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
////                view.loadUrl(url);
//                return super.shouldOverrideUrlLoading(view, url);
//            }
//        });

        binding.wvWeb.setWebChromeClient(new WebChromeClient());
        binding.wvWeb.getSettings().setJavaScriptEnabled(true);
//        binding.wvWeb.getSettings().setAppCacheEnabled(true);
//        //设置 缓存模式
//        binding.wvWeb.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
//        // 开启 DOM storage API 功能
//        binding.wvWeb.getSettings().setDomStorageEnabled(true);
        binding.wvWeb.loadUrl("https://www.baidu.com/");
    }
}
