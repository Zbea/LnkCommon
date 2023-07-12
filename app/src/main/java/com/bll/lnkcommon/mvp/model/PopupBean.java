package com.bll.lnkcommon.mvp.model;

public class PopupBean {

    public int id;
    public String name;
    public boolean isCheck;
    public int resId;
    public int index;

    public PopupBean() {
    }

    public PopupBean(int id, String name) {
        this.id=id;
        this.name = name;
    }

    public PopupBean(int id, String name, boolean isCheck) {
        this.id=id;
        this.name = name;
        this.isCheck = isCheck;
    }

    public PopupBean(int id, String name, int resId) {
        this.id = id;
        this.name = name;
        this.resId = resId;
    }
}
