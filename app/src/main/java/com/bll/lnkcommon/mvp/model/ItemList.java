package com.bll.lnkcommon.mvp.model;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

public class ItemList implements Serializable ,Comparable<ItemList>{

    public int type;
    public String desc;

    public int id;
    public String name;
    public Drawable icon; //1
    public Drawable icon_check; //2 选中的状态
    public int page;//目录页码
    public boolean isCheck;
    public String url;
    public int resId;

    public ItemList() {
    }

    public ItemList(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public int compareTo(ItemList itemList) {
        return this.id- itemList.id;
    }
}
