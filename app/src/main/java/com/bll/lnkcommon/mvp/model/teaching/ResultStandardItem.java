package com.bll.lnkcommon.mvp.model.teaching;

import java.util.List;

public class ResultStandardItem {
    public String title;
    public List<ResultChildItem> list;
    public double score;

    public static class ResultChildItem{
        public int sort;
        public String sortStr;
        public double score;
        public boolean isCheck;
    }
}
