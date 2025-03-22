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
    public int type;//1首页菜单2工具
    public int subType;//分类
    public int sort;
    @Transient
    public boolean isCheck;

    @Generated(hash = 743368013)
    public AppBean(Long id, long userId, String appName, String packageName,
            byte[] imageByte, int type, int subType, int sort) {
        this.id = id;
        this.userId = userId;
        this.appName = appName;
        this.packageName = packageName;
        this.imageByte = imageByte;
        this.type = type;
        this.subType = subType;
        this.sort = sort;
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
    public int getType() {
        return this.type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public int getSort() {
        return this.sort;
    }
    public void setSort(int sort) {
        this.sort = sort;
    }
    public int getSubType() {
        return this.subType;
    }
    public void setSubType(int subType) {
        this.subType = subType;
    }

   
}
