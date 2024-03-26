package com.sunny.oyoapp.model;

import java.util.ArrayList;

public class UserInfoRes {

    public String result;
    public UserItem items;

    public static class UserItem {
        public String nickname;
        public String email;
        public String profileUrl;

        public int postingCnt;
        public int follwerCnt;
        public int followeeCnt;

    }
}