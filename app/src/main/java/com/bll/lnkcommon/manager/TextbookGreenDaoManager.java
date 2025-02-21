package com.bll.lnkcommon.manager;


import com.bll.lnkcommon.Constants;
import com.bll.lnkcommon.MyApplication;
import com.bll.lnkcommon.greendao.BookDao;
import com.bll.lnkcommon.greendao.DaoSession;
import com.bll.lnkcommon.greendao.TextbookBeanDao;
import com.bll.lnkcommon.mvp.book.TextbookBean;
import com.bll.lnkcommon.mvp.model.User;
import com.bll.lnkcommon.utils.SPUtil;

import org.greenrobot.greendao.query.WhereCondition;

import java.util.List;
import java.util.Objects;

public class TextbookGreenDaoManager {

    /**
     * DaoSession
     */
    private DaoSession mDaoSession;
    private static TextbookGreenDaoManager mDbController;
    private final TextbookBeanDao bookDao;  //book表
    static WhereCondition whereUser;


    /**
     * 构造初始化
     */
    public TextbookGreenDaoManager() {
        mDaoSession = MyApplication.Companion.getMDaoSession();
        bookDao = mDaoSession.getTextbookBeanDao(); //book表
    }

    /**
     * 获取单例（context 最好用application的context  防止内存泄漏）
     */
    public static TextbookGreenDaoManager getInstance() {
        if (mDbController == null) {
            synchronized (TextbookGreenDaoManager.class) {
                if (mDbController == null) {
                    mDbController = new TextbookGreenDaoManager();
                }
            }
        }
        long userId = Objects.requireNonNull(SPUtil.INSTANCE.getObj("user", User.class)).accountId;
        whereUser= TextbookBeanDao.Properties.UserId.eq(userId);
        return mDbController;
    }

    //增加书籍
    public void insertOrReplaceBook(TextbookBean bean) {
        bookDao.insertOrReplace(bean);
    }

    public TextbookBean queryTextBookByBookId(int type, int bookID) {
        WhereCondition whereCondition= TextbookBeanDao.Properties.Category.eq(type);
        WhereCondition whereCondition1= TextbookBeanDao.Properties.BookId.eq(bookID);
        return bookDao.queryBuilder().where(whereUser,whereCondition,whereCondition1).build().unique();
    }

    //查找课本 细分子类
    public List<TextbookBean> queryAllTextBook(int typeId) {
        WhereCondition whereCondition1=TextbookBeanDao.Properties.Category.eq(typeId);
        return bookDao.queryBuilder().where(whereUser,whereCondition1)
                .orderDesc(TextbookBeanDao.Properties.Time).build().list();
    }

    public List<TextbookBean> queryAllTextBook(int typeId, int page, int pageSize) {
        WhereCondition whereCondition1=TextbookBeanDao.Properties.Category.eq(typeId);
        return bookDao.queryBuilder().where(whereUser,whereCondition1)
                .orderDesc(TextbookBeanDao.Properties.Time)
                .offset((page-1)*pageSize).limit(pageSize)
                .build().list();
    }

    /**
     * 获取半年以前的课本
     */
    public List<TextbookBean> queryTextBookByHalfYear(){
        long time=System.currentTimeMillis()- Constants.halfYear;
        WhereCondition whereCondition1= TextbookBeanDao.Properties.Time.le(time);
        return bookDao.queryBuilder().where(whereUser,whereCondition1).build().list();
    }

    public void deleteBook(TextbookBean book){
        bookDao.delete(book);
    }

    public void clear(){
        bookDao.deleteAll();
    }


}
