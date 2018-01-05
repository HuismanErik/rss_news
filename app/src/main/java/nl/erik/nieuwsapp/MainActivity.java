package nl.erik.nieuwsapp;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DownloadCallback<SyndFeed> {

    private RssChannel activeChannel;
    private NetworkFragment mNetworkFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RssChannel nuChannel = new RssChannel("https://www.nu.nl/rss/Algemeen", "www.nu.nl");
        RssChannel rtlChannel = new RssChannel("http://www.rtlnieuws.nl/service/rss/nederland/index.xml", "www.rtlnieuws.nl");
        RssChannel adChannel = new RssChannel("https://www.ad.nl/home/rss.xml", "www.ad.nl", "nl_cookiewall_version=1; Domain=ad.nl; Expires=Thu, 30-Dec-2099 00:00:00 GMT; Path=/");
        RssChannel nosChannel = new RssChannel("http://feeds.nos.nl/nosnieuwsalgemeen", "nos.nl");
        RssChannel nrcChannel = new RssChannel("https://www.nrc.nl/rss/", "www.nrc.nl");

        this.activeChannel = nuChannel;

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mNetworkFragment = NetworkFragment.getInstance(getFragmentManager(), activeChannel.getFeedUrl());

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    public void onBackStackChanged() {
                        WebView webView = findViewById(R.id.webView);
                        if (webView.getVisibility() == WebView.VISIBLE) {
                            webView.setVisibility(WebView.INVISIBLE);
                        }
                    }
                });

    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        this.startDownload();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void updateFromDownload(SyndFeed feed) {
        final List<SyndEntry> entries = feed.getEntries();
        List<HashMap<String, String>> arrayList = new ArrayList<>();

        for (SyndEntry entry : entries) {
            HashMap<String, String> hashMap = new HashMap<>();//create a hashmap to store the data in key value pair
            hashMap.put("name", entry.getTitle());
            List<SyndEnclosure> enclosures = entry.getEnclosures();
            String url = "";
            if (enclosures != null && !enclosures.isEmpty()) {
                url = enclosures.get(0).getUrl();
            }
            hashMap.put("image", url + "");
            arrayList.add(hashMap);//add the hashmap into arrayList
        }

        String[] from = {"name", "image"};//string array
        int[] to = {R.id.textView, R.id.imageView};//int array of views id's

        SimpleAdapter simpleAdapter = new SimpleAdapter(this, arrayList, R.layout.list_view_items, from, to);//Create object and set the parameters for simpleAdapter
        simpleAdapter.setViewBinder(new ImageViewBinder());
        ListView simpleListView = findViewById(R.id.news_content);
        simpleListView.setAdapter(simpleAdapter);//sets the adapter for listView
        simpleListView.invalidate();

        setCookie();
        //perform listView item click event
        simpleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // NRC uses link:
                SyndEntry newsEntry = entries.get(i);
                String url;
                if (newsEntry.getLink() != null || !newsEntry.getLink().isEmpty()) {
                    url = newsEntry.getLink();
                } else {
                    url = newsEntry.getUri();
                }
                openNewWebviewActitivity(url);
            }
        });

    }

    private void openNewWebviewActitivity(String url) {
        Intent mIntent = new Intent(this, NewsItemActivity.class);
        mIntent.putExtra("activeChannel", activeChannel);
        mIntent.putExtra("url", url);
        startActivity(mIntent);
    }

    private void setCookie() {
        if (activeChannel.getCookie() != null) {
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setCookie(activeChannel.getNewsDomain(), activeChannel.getCookie());
        }
    }

    private void startDownload() {
        if (mNetworkFragment != null) {
            // Execute the async download.
            mNetworkFragment.startDownload();
        }
    }

    @Override
    public NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo();
    }

    @Override
    public void onProgressUpdate(int progressCode, int percentComplete) {
    }

    @Override
    public void finishDownloading() {
    }

}
