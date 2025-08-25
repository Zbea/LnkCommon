package com.bll.lnkcommon.mvp.model;


import com.bll.lnkcommon.MethodManager;
import com.bll.lnkcommon.utils.SPUtil;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Unique;

import java.util.Objects;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class AppBean {
    @Unique
    @Id(autoincrement = true)
    public Long id;
    public long userId= MethodManager.getAccountId();
    public String appName;
    public String packageName;
    public byte[] imageByte;
    public long time;
    public boolean isTool;
    @Transient
    public boolean isCheck;

    @Generated(hash = 2108146592)
    public AppBean(Long id, long userId, String appName, String packageName,
            byte[] imageByte, long time, boolean isTool) {
        this.id = id;
        this.userId = userId;
        this.appName = appName;
        this.packageName = packageName;
        this.imageByte = imageByte;
        this.time = time;
        this.isTool = isTool;
    }
    @Generated(hash = 285800313)
    public AppBean() {
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
    public String getAppName() {
        return this.appName;
    }
    public void setAppName(String appName) {
        this.appName = appName;
    }
    public String getPackageName() {
        return this.packageName;
    }
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    public byte[] getImageByte() {
        return this.imageByte;
    }
    public void setImageByte(byte[] imageByte) {
        this.imageByte = imageByte;
    }
    public long getTime() {
        return this.time;
    }
    public void setTime(long time) {
        this.time = time;
    }
    public boolean getIsTool() {
        return this.isTool;
    }
    public void setIsTool(boolean isTool) {
        this.isTool = isTool;
    }
}
