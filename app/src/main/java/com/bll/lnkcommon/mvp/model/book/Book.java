package com.bll.lnkcommon.mvp.model.book;

import com.bll.lnkcommon.MethodManager;
import com.google.gson.annotations.SerializedName;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Unique;

/**
 * 书籍
 */
@Entity
public class Book {

    @Id(autoincrement = true)
    @Unique
    public Long id;
    public long userId= MethodManager.getAccountId();
    @Unique
    public int bookId;
    public int type;//1古籍2自然科学3社会科学4思维科学5运动才艺
    public String subtypeStr;//书架所有分类
    public String imageUrl;
    public String bookName;//书名
    public int grade; //年级
    public int supply ;//官方
    @SerializedName("bodyUrl")
    public String downloadUrl;//书籍下载url
    public String bookPath;  //book书的路径
    public String bookDrawPath;  //book书的手写路径
    public long time;//观看时间
    public int pageIndex;//当前页
    public String pageUrl;//当前页路径
    public boolean isLook;//是否已经打开

    @Transient
    public int price;//书的价格
    @Transient
    public String bookDesc;//描述
    @Transient
    public String version;//版本
    @Transient
    public int loadSate;//0未下载 1正下载 2已下载
    @Transient
    public int buyStatus;//购买状态1
    @Transient
    public int cloudId;
    @Transient
    public String drawUrl;
    @Generated(hash = 24855883)
    public Book(Long id, long userId, int bookId, int type, String subtypeStr, String imageUrl,
            String bookName, int grade, int supply, String downloadUrl, String bookPath,
            String bookDrawPath, long time, int pageIndex, String pageUrl, boolean isLook) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.type = type;
        this.subtypeStr = subtypeStr;
        this.imageUrl = imageUrl;
        this.bookName = bookName;
        this.grade = grade;
        this.supply = supply;
        this.downloadUrl = downloadUrl;
        this.bookPath = bookPath;
        this.bookDrawPath = bookDrawPath;
        this.time = time;
        this.pageIndex = pageIndex;
        this.pageUrl = pageUrl;
        this.isLook = isLook;
    }
    @Generated(hash = 1839243756)
    public Book() {
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
    public int getBookId() {
        return this.bookId;
    }
    public void setBookId(int bookId) {
        this.bookId = bookId;
    }
    public int getType() {
        return this.type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public String getSubtypeStr() {
        return this.subtypeStr;
    }
    public void setSubtypeStr(String subtypeStr) {
        this.subtypeStr = subtypeStr;
    }
    public String getImageUrl() {
        return this.imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    public String getBookName() {
        return this.bookName;
    }
    public void setBookName(String bookName) {
        this.bookName = bookName;
    }
    public int getGrade() {
        return this.grade;
    }
    public void setGrade(int grade) {
        this.grade = grade;
    }
    public int getSupply() {
        return this.supply;
    }
    public void setSupply(int supply) {
        this.supply = supply;
    }
    public String getDownloadUrl() {
        return this.downloadUrl;
    }
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
    public String getBookPath() {
        return this.bookPath;
    }
    public void setBookPath(String bookPath) {
        this.bookPath = bookPath;
    }
    public String getBookDrawPath() {
        return this.bookDrawPath;
    }
    public void setBookDrawPath(String bookDrawPath) {
        this.bookDrawPath = bookDrawPath;
    }
    public long getTime() {
        return this.time;
    }
    public void setTime(long time) {
        this.time = time;
    }
    public int getPageIndex() {
        return this.pageIndex;
    }
    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }
    public String getPageUrl() {
        return this.pageUrl;
    }
    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }
    public boolean getIsLook() {
        return this.isLook;
    }
    public void setIsLook(boolean isLook) {
        this.isLook = isLook;
    }


}
