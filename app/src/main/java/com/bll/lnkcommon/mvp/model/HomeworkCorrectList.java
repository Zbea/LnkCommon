package com.bll.lnkcommon.mvp.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class HomeworkCorrectList {

    public int total;
    public List<CorrectBean> list;

    public static class CorrectBean implements Serializable {
        public int id;
        public int childId;
        public String submitUrl;
        public String changeUrl;
        public int type;
        public int parentHomeworkId;
        @SerializedName("title")
        public String content;
        public long endTime;
        public long submitTime;
        @SerializedName("name")
        public String homeworkName;
        public int subject;
        public long time;
        @SerializedName("submitStatus")
        public int status;//1未提交2已提交3已批改

    }
}
