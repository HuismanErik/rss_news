package nl.erik.nieuwsapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class NewsItemActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_item);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        RssChannel activeChannel = (RssChannel) intent.getExtras().get("activeChannel");
        String url = (String) intent.getExtras().get("url");

        WebView webView = findViewById(R.id.webView);
        webView.setWebViewClient(
                new WebViewClient() {
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        String host = Uri.parse(url).getHost();
                        return !activeChannel.getNewsDomain().equals(host);
                    }
                }
        );
        // disables javascript
        webView.getSettings().setJavaScriptEnabled(false);
        webView.loadUrl(url);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }
}
