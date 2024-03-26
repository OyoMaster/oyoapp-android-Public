package com.sunny.oyoapp.model;


public class Video {

    public String videoId;
    public String title;
    public String description;
    public String mediumUrl;
    public String highUrl;
    public String channelTitle;
    public String publishTime;

    public Video(String videoId, String title, String highUrl, String channelTitle, String publishTime) {
        this.videoId = videoId;
        this.title = title;
        this.highUrl = highUrl;
        this.channelTitle = channelTitle;
        this.publishTime = publishTime;
    }

    public Video(String videoId, String title, String chTitle, String highUrl) {
    }

}
