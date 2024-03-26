package com.sunny.oyoapp.model;

import java.io.Serializable;

public class User implements Serializable {

    public int id;
    public int type;
    public String email;
    public String password;

    public String nickname;
    public String profileUrl;

    public String age;
    public String username;
    public String createdAt;

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public User(String email, String password, String nickName) {
        this.email = email;
        this.password = password;
        this.nickname = nickName;
    }

    public User(String email, String password, String nickname, String age, String username) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.age = age;
        this.username = username;
    }

    public User(String email, String nickname, String profileUrl, String username) {
        this.email = email;
        this.profileUrl = profileUrl;
        this.nickname = nickname;
        this.username = username;
    }
}
