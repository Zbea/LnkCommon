package com.bll.lnkcommon.mvp.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TeacherHomeworkList {

    public int total;
    public List<TeacherHomeworkBean> list;

    public static class TeacherHomeworkBean{
        public int id;
        public int childId;
        public String subject;
        public int type;
        public String homeworkName;
        public long submitTime;
        public long time;
        public String submitContent;
        public String homeworkContent;
        public String correctContent;
        public String title;
        @SerializedName("msgType")
        public int status;
        public int studentTaskId;
    }
}
