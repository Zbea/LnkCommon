package com.bll.lnkcommon.mvp.model;

import com.bll.lnkcommon.MethodManager;
import com.bll.lnkcommon.greendao.StringConverter;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

import java.util.ArrayList;
import java.util.List;

@Entity
public class FreeNoteBean {

    @Unique
    @Id(autoincrement = true)
    public Long id;
    public long userId= MethodManager.getAccountId();
    public String title;
    @Unique
    public long date;
    public boolean isSave;
    public int page;
    public Integer type;//0自建 1下载
    @Convert(columnType = String.class,converter = StringConverter.class)
    public List<String> paths;
    @Convert(columnType = String.class,converter = StringConverter.class)
    public List<String> bgRes;
    @Transient
    public int cloudId;
    @Transient
    public String downloadUrl;

    @Generated(hash = 1840328203)
    public FreeNoteBean(Long id, long userId, String title, long date,
            boolean isSave, int page, Integer type, List<String> paths,
            List<String> bgRes) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.date = date;
        this.isSave = isSave;
        this.page = page;
        this.type = type;
        this.paths = paths;
        this.bgRes = bgRes;
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
    public boolean getIsSave() {
        return this.isSave;
    }
    public void setIsSave(boolean isSave) {
        this.isSave = isSave;
    }
    public int getPage() {
        return this.page;
    }
    public void setPage(int page) {
        this.page = page;
    }
    public Integer getType() {
        return this.type;
    }
    public void setType(Integer type) {
        this.type = type;
    }
    public List<String> getPaths() {
        return this.paths;
    }
    public void setPaths(List<String> paths) {
        this.paths = paths;
    }
    public List<String> getBgRes() {
        return this.bgRes;
    }
    public void setBgRes(List<String> bgRes) {
        this.bgRes = bgRes;
    }

    
}
