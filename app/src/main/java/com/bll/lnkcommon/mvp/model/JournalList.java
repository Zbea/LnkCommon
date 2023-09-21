package com.bll.lnkcommon.mvp.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class JournalList {

    public int total;
    public List<JournalBean> list;

    public static class JournalBean{
        public int id;
        @SerializedName("drawName")
        public String title;
        public String bodyUrl;
        public long date;
    }
}
