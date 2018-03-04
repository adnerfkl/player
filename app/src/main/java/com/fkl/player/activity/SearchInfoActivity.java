package com.fkl.player.activity;

import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.fkl.player.R;

public class SearchInfoActivity extends AppCompatActivity {
private WebView web;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_info);
        web= (WebView) findViewById(R.id.wb);
        String weburl = getIntent().getStringExtra("weburl");
        web.loadUrl(weburl);
        web.setWebViewClient(new WebViewClient(){
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri url = request.getUrl();
                web.loadUrl(String.valueOf(url));
                return true;
            }
        });

    }
}
