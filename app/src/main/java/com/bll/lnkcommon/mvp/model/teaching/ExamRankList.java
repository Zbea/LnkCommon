package com.bll.lnkcommon.mvp.model.teaching;

import java.util.List;

public class ExamRankList {

    public List<ExamRankBean> list;
    public static class ExamRankBean{
        public String className;
        public int classId;
        public String studentName;
        public double score;
    }
}
