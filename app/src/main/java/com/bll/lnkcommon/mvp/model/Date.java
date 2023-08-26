package com.bll.lnkcommon.mvp.model;


import com.bll.lnkcommon.utils.date.Lunar;
import com.bll.lnkcommon.utils.date.Solar;

import java.io.Serializable;

public class Date implements Serializable {

    public int year;
    public int month;
    public int day;
    public int week;//2星期一 8星期日
    public long time;
    public boolean isNow;//是否是当天
    public boolean isNowMonth;//是否是当月

    public Solar solar=new Solar();
    public Lunar lunar=new Lunar();

}
