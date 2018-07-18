package com.travel.reco;

public class Photo {

    private String url;
    private String location;
    private String matchDescription;

    public Photo(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}