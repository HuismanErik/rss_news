package nl.erik.nieuwsapp;

import java.io.Serializable;

public class RssChannel implements Serializable {
    private final String feedUrl;
    private final String newsDomain;
    private String cookie;

    public RssChannel(String feedUrl, String newsDomain) {
        this.feedUrl = feedUrl;
        this.newsDomain = newsDomain;
    }

    public RssChannel(String feedUrl, String newsDomain, String cookie) {
        this(feedUrl, newsDomain);
        this.cookie = cookie;
    }

    public String getFeedUrl() {
        return feedUrl;
    }

    public String getNewsDomain() {
        return newsDomain;
    }

    public String getCookie() {
        return cookie;
    }

}
