package com.bll.lnkcommon.mvp.model;

import com.bll.lnkcommon.greendao.StringConverter;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

import java.util.List;

@Entity
public class FreeNoteBean {

    @Unique
    @Id(autoincrement = true)
    public Long id;
    public long userId;
    public String title;
    public long date;
    public String bgRes;
    @Convert(columnType = String.class,converter = StringConverter.class)
    public List<String> paths;

    @Generated(hash = 1782484194)
    public FreeNoteBean(Long id, long userId, String title, long date, String bgRes,
            List<String> paths) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.date = date;
        this.bgRes = bgRes;
        this.paths = paths;
    }
    @Generated(hash = 1976554700)
    public FreeNoteBean() {
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
    public String getTitle() {
        return this.title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public long getDate() {
        return this.date;
    }
    public void setDate(long date) {
        this.date = date;
    }
    public List<String> getPaths() {
        return this.paths;
    }
    public void setPaths(List<String> paths) {
        this.paths = paths;
    }
    public String getBgRes() {
        return this.bgRes;
    }
    public void setBgRes(String bgRes) {
        this.bgRes = bgRes;
    }


}
