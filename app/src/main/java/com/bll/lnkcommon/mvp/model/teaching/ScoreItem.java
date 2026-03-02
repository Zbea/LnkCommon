package com.bll.lnkcommon.mvp.model.teaching;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ScoreItem implements Serializable {
    public double score;
    public int type=1;
    public String sortStr="";
    public int level;
    public int result;//0错1对
    public double label;//题目标准分数
    public boolean isChildBottom;//将最大层级都没有子的时候其父为true 然后横排
    public ScoreItem parentItem;
    public List<ScoreItem> childScores=new ArrayList<>();
    public String pos;

    //为true childScores为同级子
    public boolean getIsChildLevel(){
        return this.type==2;
    }

    public static class Point implements Serializable{
        public int x;
        public int y;
    }
}
