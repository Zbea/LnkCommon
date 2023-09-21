package com.bll.lnkcommon.mvp.model;

import java.io.Serializable;

public class ItemList implements Serializable ,Comparable<ItemList>{

    public int type;
    public String desc;

    public int id;
    public String name;
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
