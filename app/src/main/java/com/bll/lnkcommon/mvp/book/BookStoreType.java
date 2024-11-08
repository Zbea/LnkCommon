package com.bll.lnkcommon.mvp.book;

import com.bll.lnkcommon.mvp.model.ItemList;

import java.util.List;
import java.util.Map;

/**
 * 教材分类
 */
public class BookStoreType {
    public List<ItemList> type;//除开教材分类
    public Map<String,List<ItemList>> subType ;//书籍分类
    public List<ItemList> bookLibType;//教库分类
    public List<ItemList> bookVersion;//版本

}
