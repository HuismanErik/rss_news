package nl.erik.nieuwsapp;

import android.content.Context;
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
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements DownloadCallback<SyndFeed> {

    //   public static final String HTTPS_WWW_NU_NL_RSS_ALGEMEEN = "https://www.nu.nl/rss/Algemeen";
//   public static final String HTTPS_WWW_NU_NL_RSS_ALGEMEEN = "http://www.rtlnieuws.nl/service/rss/nederland/index.xml";
//    public static final String HTTPS_WWW_NU_NL_RSS_ALGEMEEN = "https://www.ad.nl/home/rss.xml";
//    public static final String HTTPS_WWW_NU_NL_RSS_ALGEMEEN = "https://www.telegraaf.nl/rss/";

    // TODO doorlinken vanuit NRC feed faalt...
    public static final String HTTPS_WWW_NU_NL_RSS_ALGEMEEN = "https://www.nrc.nl/rss/";
//    public static final String HTTPS_WWW_NU_NL_RSS_ALGEMEEN = "http://feeds.nos.nl/nosnieuwsalgemeen";

//



    private NetworkFragment mNetworkFragment;
    final static Set<String> allowedHosts = new HashSet<String>();

    static {
        allowedHosts.add("www.nu.nl");
        allowedHosts.add("www.rtlnieuws.nl");
        allowedHosts.add("www.ad.nl");
        allowedHosts.add("www.telegraaf.nl");
        allowedHosts.add("www.nrc.nl");
        allowedHosts.add("nos.nl");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        mNetworkFragment = NetworkFragment.getInstance(getFragmentManager(), HTTPS_WWW_NU_NL_RSS_ALGEMEEN);

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
                        if(webView.getVisibility() == WebView.VISIBLE) {
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

        SimpleAdapter simpleAdapter = new SimpleAdapter(this, arrayList, R.layout.list_view_items, from, to) {

        };//Create object and set the parameters for simpleAdapter
        ListView simpleListView = findViewById(R.id.news_content);
        simpleListView.setAdapter(simpleAdapter);//sets the adapter for listView
        simpleListView.invalidate();

        CookieSyncManager syncManager = CookieSyncManager.createInstance(this);
        syncManager.startSync();
        android.webkit.CookieManager cookieManager = android.webkit.CookieManager.getInstance();
        cookieManager.setCookie("wwww.ad.nl", "nl_cookiewall_version=1; Domain=ad.nl; Expires=Thu, 30-Dec-2099 00:00:00 GMT; Path=/");
        syncManager.sync();
        //perform listView item click event
        simpleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override

            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                WebView webView = findViewById(R.id.webView);
                webView.setWebViewClient(
                        new WebViewClient() {
                            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                String host = Uri.parse(url).getHost();
                                return !allowedHosts.contains(host);
                            }
                        }
                );
                // disables javascript
                webView.getSettings().setJavaScriptEnabled(false);
                webView.setVisibility(View.VISIBLE);
                // NRC uses link:
                entries.get(i).getLink();
                webView.loadUrl(entries.get(i).getUri());



            }
        });

    }

    private void startDownload() {
        boolean mDownloading = false;
        if (!mDownloading && mNetworkFragment != null) {
            // Execute the async download.
            mNetworkFragment.startDownload();
            mDownloading = true;
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
