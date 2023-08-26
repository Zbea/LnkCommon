package com.bll.lnkcommon.manager;

import com.bll.lnkcommon.MyApplication;
import com.bll.lnkcommon.greendao.BookTypeBeanDao;
import com.bll.lnkcommon.greendao.DaoSession;
import com.bll.lnkcommon.greendao.RecordBeanDao;
import com.bll.lnkcommon.mvp.model.BookTypeBean;
import com.bll.lnkcommon.mvp.model.RecordBean;
import com.bll.lnkcommon.mvp.model.User;
import com.bll.lnkcommon.utils.SPUtil;

import org.greenrobot.greendao.query.WhereCondition;

import java.util.List;

public class BookTypeDaoManager {

    /**
     * DaoSession
     */
    private DaoSession mDaoSession;
    private static BookTypeDaoManager mDbController;
    private final BookTypeBeanDao dao;
    private static WhereCondition whereUser;

    /**
     * 构造初始化
     */
    public BookTypeDaoManager() {
        mDaoSession = MyApplication.Companion.getMDaoSession();
        dao = mDaoSession.getBookTypeBeanDao();
    }

    /**
     * 获取单例（context 最好用application的context  防止内存泄漏）
     */
    public static BookTypeDaoManager getInstance() {
        if (mDbController == null) {
            synchronized (BookTypeDaoManager.class) {
                if (mDbController == null) {
                    mDbController = new BookTypeDaoManager();
                }
            }
        }
        whereUser= BookTypeBeanDao.Properties.UserId.eq(SPUtil.INSTANCE.getObj("user", User.class).accountId);
        return mDbController;
    }

    public void insertOrReplace(BookTypeBean bean) {
        dao.insertOrReplace(bean);
    }

    public List<BookTypeBean> queryAllList() {
        return dao.queryBuilder().where(whereUser)
                .orderAsc(BookTypeBeanDao.Properties.Date).build().list();
    }

    public boolean isExistType(String name){
        WhereCondition whereCondition=BookTypeBeanDao.Properties.Name.eq(name);
        BookTypeBean bean=dao.queryBuilder().where(whereUser,whereCondition).build().unique();
        return bean!=null;
    }

    public void deleteBean(String name){
        WhereCondition whereCondition=BookTypeBeanDao.Properties.Name.eq(name);
        BookTypeBean bean=dao.queryBuilder().where(whereUser,whereCondition).build().unique();
        dao.delete(bean);
    }

}
