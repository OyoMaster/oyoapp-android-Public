package com.sunny.oyoapp.model;

public class Posting {
    public int id;
    public int userId;
    public String title;
    public String imageURL;

    public String profileUrl;
    public String ingredients;
    public String recipe;
    public String createdAt;
    public String updatedAt;
    public String nickname;
    public String favorites;

    public int postingid;
    public float avgRating;
    public int isFavorite;


    // 내가 즐겨찾기한 레시피 API만 이걸로 씀
    public int postingId;

}

