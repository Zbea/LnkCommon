package com.bll.lnkcommon.mvp.model;

public class CloudListBean {
    public int id;
    public int type;//1书籍2课本3笔记4日记5随笔6截图
    public int subType;
    public String subTypeStr;
    public long date;//上传时间
    public int year;
    public int bookId;//书籍id
    public String downloadUrl;//上传的下载链接
    public String zipUrl;//原来的下载链接
    public String listJson;//封面列表json
    public String contentJson;//内容json
    public String contentSubtypeJson;//子内容json
    public int skip;//为1时可以重复
}
