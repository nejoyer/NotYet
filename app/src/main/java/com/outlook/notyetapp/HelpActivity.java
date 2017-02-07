package com.outlook.notyetapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HelpActivity extends AppCompatActivity {
    @BindView(R.id.help_activity_webview)
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        ButterKnife.bind(this);
        webView.loadData(getString(R.string.help_activity_html), "text/html", "utf-8");
    }
}
