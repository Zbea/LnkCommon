package com.bll.lnkcommon.mvp.model;

import java.util.List;

public class JournalList {

    public int total;
    public List<JournalBean> list;

    public static class JournalBean{
        public int id;
        public String title;
        public String imageUrl;
        public long date;
    }
}
