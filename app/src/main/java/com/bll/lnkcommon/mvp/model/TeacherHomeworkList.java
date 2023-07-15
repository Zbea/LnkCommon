package com.bll.lnkcommon.mvp.model;

import java.util.List;

public class TeacherHomeworkList {

    public int total;
    public List<TeacherHomeworkBean> list;

    public static class TeacherHomeworkBean{
        public int id;
        public int status;
        public String course;
        public String typeStr;
        public int type;
        public String content;
        public long date;
        public long commitDate;
    }
}
