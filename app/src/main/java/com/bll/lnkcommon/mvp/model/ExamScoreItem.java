package com.bll.lnkcommon.mvp.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ExamScoreItem implements Serializable {
    public double score=0;
    public int sort;
    public int result;//0错1对
    public double label;//题目标准分数
    public List<ExamScoreItem> childScores=new ArrayList<>();
}
