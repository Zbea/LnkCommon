package com.bll.lnkcommon.mvp.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class BookTypeBean {

    @Unique
    @Id(autoincrement = true)
    public Long id;
    public long userId;
    @Unique
    public String name;
    public long date;
    @Transient
    public boolean isCheck;

    @Generated(hash = 1000367589)
    public BookTypeBean(Long id, long userId, String name, long date) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.date = date;
    }
    @Generated(hash = 1751641649)
    public BookTypeBean() {
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
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public long getDate() {
        return this.date;
    }
    public void setDate(long date) {
        this.date = date;
    }
}
