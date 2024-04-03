package com.bll.lnkcommon.mvp.model;

import java.util.List;

public class ExamList {

    public int total;
    public List<ExamBean> list;

    public static class ExamBean{
        public int id;
        public int schoolExamJobId;
        public String subject;
        public String examUrl;
        public String studentUrl;
        public String teacherUrl;
        public int score;
        public long expTime;
        public String examName;
    }

}
