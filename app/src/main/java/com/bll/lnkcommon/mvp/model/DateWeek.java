package com.bll.lnkcommon.mvp.model;

import java.io.Serializable;

public class DateWeek implements Serializable {

    public String name;
    public int week;
    public boolean isCheck;
    public boolean isSelected;//该星期是否已选

    public DateWeek(String name, int week, boolean isCheck) {
        this.name = name;
        this.week = week;
        this.isCheck = isCheck;
    }
}
