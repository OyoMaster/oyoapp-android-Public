package com.sunny.oyoapp.model;

import java.io.Serializable;

public class Review implements Serializable {
    public int postingId;
    public int reviewId;
    public int userId;
    public String profileUrl;
    public String nickname;
    public String content;
    public String createdAt;
    public String updatedAt;

    public double rating;

    public Review(String content, double rating) {
        this.content = content;
        this.rating = rating;

    }

    public Review(String profileUrl, String nickname, String content, double rating) {
        this.profileUrl = profileUrl;
        this.nickname = nickname;
        this.content = content;
        this.rating = rating;
    }
}
