package com.travel.reco;

import android.net.Uri;

public class Photo {

    private String[] url;
    private String location;
    private String[] description;

    public Photo(String[] url, String location, String[] description) {
        this.url = url;
        this.description = description;
        this.location = location;
    }

    public String[] getUrls() {
        return url;
    }
    public String getLocation() {
        return location;
    }
    public String[] getDescription() {
        return description;
    }
}