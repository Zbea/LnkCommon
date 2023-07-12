package com.bll.lnkcommon.mvp.model;

import com.bll.lnkcommon.utils.SPUtil;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Unique;

import java.io.Serializable;
import java.util.Objects;

import org.greenrobot.greendao.annotation.Generated;

@Entity
public class DateEvent implements Serializable {

    @Transient
    private static final long serialVersionUID = 1L;

    @Unique
    @Id(autoincrement = true)
    public Long id;
    public long userId= Objects.requireNonNull(SPUtil.INSTANCE.getObj("user", User.class)).accountId;
    public String content;
    public long date;//当天日期
    public int week;//当天星期 2星期一 8星期日
    public int classId;//班级id
    @Generated(hash = 730171961)
    public DateEvent(Long id, long userId, String content, long date, int week, int classId) {
        this.id = id;
        this.userId = userId;
        this.content = content;
        this.date = date;
        this.week = week;
        this.classId = classId;
    }
    @Generated(hash = 1511002217)
    public DateEvent() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public long getUserId() {
        return this.userId;
    }
    public void setUserId(long userId) {
        this.userId = userId;
    }
    public String getContent() {
        return this.content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public long getDate() {
        return this.date;
    }
    public void setDate(long date) {
        this.date = date;
    }
    public int getWeek() {
        return this.week;
    }
    public void setWeek(int week) {
        this.week = week;
    }
    public int getClassId() {
        return this.classId;
    }
    public void setClassId(int classId) {
        this.classId = classId;
    }

}
