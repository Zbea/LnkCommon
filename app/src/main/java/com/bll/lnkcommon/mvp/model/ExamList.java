package com.bll.lnkcommon.mvp.model;

import java.io.Serializable;
import java.util.List;

public class ExamList {

    public int total;
    public List<ExamBean> list;

    public static class ExamBean implements Serializable {
        public int id;
        public int schoolExamJobId;
        public int subject;
        public String examUrl;
        public String studentUrl;
        public String teacherUrl;
        public int score;
        public long expTime;
        public String examName;
        public int classId;
        public String className;
        public int questionType;
        public int questionMode;
        public String question;
        public String answerUrl;
    }

}
