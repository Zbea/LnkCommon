package com.bll.lnkcommon.mvp.model;

import com.bll.lnkcommon.utils.SPUtil;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Unique;

import java.util.Objects;

/**
 * 书籍
 */
@Entity
public class Book {

    @Id(autoincrement = true)
    @Unique
    public Long id;
    public long userId= Objects.requireNonNull(SPUtil.INSTANCE.getObj("user", User.class)).accountId;
    public int bookId;
    public int bookPlusId;
    public String imageUrl;
    public String bookName;//书名
    public int price;//书的价格
    public int category;//0教材1书籍
    public int typeId;//0教材1古籍2自然科学3社会科学4思维科学5运动才艺6作业7教学教育
    public String subtypeStr;//书架所有分类
    public String bookDesc;//描述
    public int semester;//学期
    public int subjectName;//课目
    public String area;//地区
    public int grade; //年级
    public String version;//版本
    public String supply ;  //官方
    public String bodyUrl;//书籍下载url
    public String bookPath;  //book书的路径
    public String bookDrawPath;  //book书的手写路径
    public long time;//观看时间
    public int pageIndex;//当前页
    public String pageUrl;//当前页路径
    public boolean isLook;//是否已经打开
    @Transient
    public int loadSate;//0未下载 1正下载 2已下载
    @Transient
    public int buyStatus;//购买状态1
    //教学教育数据
    @Transient
    public String title;
    @Transient
    public String desc;
    @Transient
    public int subject;
    @Transient
    public int cloudId;//云书库id
    @Transient
    public String drawUrl;//云书库手写下载地址


    @Generated(hash = 2146450712)
    public Book(Long id, long userId, int bookId, int bookPlusId, String imageUrl, String bookName,
            int price, int category, int typeId, String subtypeStr, String bookDesc, int semester,
            int subjectName, String area, int grade, String version, String supply, String bodyUrl,
            String bookPath, String bookDrawPath, long time, int pageIndex, String pageUrl,
            boolean isLook) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.bookPlusId = bookPlusId;
        this.imageUrl = imageUrl;
        this.bookName = bookName;
        this.price = price;
        this.category = category;
        this.typeId = typeId;
        this.subtypeStr = subtypeStr;
        this.bookDesc = bookDesc;
        this.semester = semester;
        this.subjectName = subjectName;
        this.area = area;
        this.grade = grade;
        this.version = version;
        this.supply = supply;
        this.bodyUrl = bodyUrl;
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
    public int getBookPlusId() {
        return this.bookPlusId;
    }
    public void setBookPlusId(int bookPlusId) {
        this.bookPlusId = bookPlusId;
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
    public int getPrice() {
        return this.price;
    }
    public void setPrice(int price) {
        this.price = price;
    }
    public int getCategory() {
        return this.category;
    }
    public void setCategory(int category) {
        this.category = category;
    }
    public int getTypeId() {
        return this.typeId;
    }
    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }
    public String getSubtypeStr() {
        return this.subtypeStr;
    }
    public void setSubtypeStr(String subtypeStr) {
        this.subtypeStr = subtypeStr;
    }
    public String getBookDesc() {
        return this.bookDesc;
    }
    public void setBookDesc(String bookDesc) {
        this.bookDesc = bookDesc;
    }
    public int getSemester() {
        return this.semester;
    }
    public void setSemester(int semester) {
        this.semester = semester;
    }
    public int getSubjectName() {
        return this.subjectName;
    }
    public void setSubjectName(int subjectName) {
        this.subjectName = subjectName;
    }
    public String getArea() {
        return this.area;
    }
    public void setArea(String area) {
        this.area = area;
    }
    public int getGrade() {
        return this.grade;
    }
    public void setGrade(int grade) {
        this.grade = grade;
    }
    public String getVersion() {
        return this.version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public String getSupply() {
        return this.supply;
    }
    public void setSupply(String supply) {
        this.supply = supply;
    }
    public String getBodyUrl() {
        return this.bodyUrl;
    }
    public void setBodyUrl(String bodyUrl) {
        this.bodyUrl = bodyUrl;
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
