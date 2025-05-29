package com.bll.lnkcommon.mvp.model;

import java.util.List;

public class ResultStandardItem {
    public String title;
    public List<ResultChildItem> list;

    public static class ResultChildItem{
        public int sort;
        public String sortStr;
        public double score;
        public boolean isCheck;
    }
}
