package com.bll.lnkcommon.mvp.model.teaching;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ScoreItem implements Serializable {
    public double score=0;
    public int sort;
    public String sortStr;//标题
    public int result=0;//0错1对
    public double label=0;//题目标准分数
    public List<ScoreItem> childScores=new ArrayList<>();

    public String pos;
    public List<Point> points=parsePoints(pos);

    private List<Point> parsePoints(String json){
        List<Point> points=new ArrayList<>();
        try{
            points=new Gson().fromJson(json, new TypeToken<List<Point>>() {}.getType());
        }
        catch (Exception ignored){
        }
        return points;
    }

    public static class Point implements Serializable{
        public int x;
        public int y;
    }
}
