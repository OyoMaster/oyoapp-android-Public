package com.sunny.oyoapp.model;

import java.util.ArrayList;

public class VideoList {

    public String kind;
    public String etag;
    public String nextPageToken;
    public String regionCode;
    public PageInfo pageInfo;
    public ArrayList<Video> items;

    public class PageInfo {
        public int totalResults;
        public int resultsPerPage;
    }

}
