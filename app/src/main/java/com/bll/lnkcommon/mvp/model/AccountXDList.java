package com.bll.lnkcommon.mvp.model;

import java.util.List;

//学豆
public class AccountXDList {

    public int pageIndex;
    public int pageSize;
    public int pageCount;
    public int totalCount;
    public List<ListBean> list;


    public static class ListBean {

        public int id;
        public String price;
        public int amount;
        public String description;

        public int times;
        public String name;

    }

}
