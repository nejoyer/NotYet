package com.outlook.notyetapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        WebView webView = (WebView)findViewById(R.id.help_activity_webview);
        webView.loadData(getString(R.string.help_activity_html), "text/html", "utf-8");
    }
}
