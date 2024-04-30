package com.bll.lnkcommon.mvp.model;

import java.util.List;

public class FriendList {
    public int total;
    public List<FriendBean> list;

    public static class FriendBean {
        public int id;
        public String account;
        public int friendId;
        public String nickname;
        public boolean isCheck;
    }

}
