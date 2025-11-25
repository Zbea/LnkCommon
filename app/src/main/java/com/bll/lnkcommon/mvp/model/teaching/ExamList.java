package com.bll.lnkcommon.mvp.model.teaching;

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
        public String score;
        public long expTime;//考试时间
        public String startTime;//布置时间
        public String createTime;//下发时间
        public String examName;
        public int classId;
        public String className;
        public int questionType;
        public int questionMode;
        public String question;
        public String answerUrl;
    }

}
