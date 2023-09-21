package com.bll.lnkcommon.manager;

import com.bll.lnkcommon.MyApplication;
import com.bll.lnkcommon.greendao.DaoSession;
import com.bll.lnkcommon.greendao.DiaryBeanDao;
import com.bll.lnkcommon.mvp.model.DiaryBean;
import com.bll.lnkcommon.mvp.model.User;
import com.bll.lnkcommon.utils.SPUtil;

import org.greenrobot.greendao.query.WhereCondition;

import java.util.List;
import java.util.Objects;

public class DiaryDaoManager {

    /**
     * DaoSession
     */
    private DaoSession mDaoSession;
    private static DiaryDaoManager mDbController;
    private final DiaryBeanDao dao;
    private static WhereCondition whereUser;

    /**
     * 构造初始化
     */
    public DiaryDaoManager() {
        mDaoSession = MyApplication.Companion.getMDaoSession();
        dao = mDaoSession.getDiaryBeanDao();
    }

    /**
     * 获取单例（context 最好用application的context  防止内存泄漏）
     */
    public static DiaryDaoManager getInstance() {
        if (mDbController == null) {
            synchronized (DiaryDaoManager.class) {
                if (mDbController == null) {
                    mDbController = new DiaryDaoManager();
                }
            }
        }
        User mUser=SPUtil.INSTANCE.getObj("user", User.class);
        long userId =0;
        if (mUser!=null) {
            userId=mUser.accountId;
        }
        whereUser= DiaryBeanDao.Properties.UserId.eq(userId);
        return mDbController;
    }

    public void insertOrReplace(DiaryBean bean) {
        dao.insertOrReplace(bean);
    }

    public DiaryBean queryBean(long time) {
        WhereCondition whereCondition= DiaryBeanDao.Properties.Date.eq(time);
        return dao.queryBuilder().where(whereUser,whereCondition).orderDesc(DiaryBeanDao.Properties.Date).build().unique();
    }

    public List<DiaryBean> queryList() {
        return dao.queryBuilder().where(whereUser).orderDesc(DiaryBeanDao.Properties.Date).build().list();
    }

    public List<DiaryBean> queryList(long time) {
        WhereCondition whereCondition= DiaryBeanDao.Properties.Date.lt(time);
        return dao.queryBuilder().where(whereUser,whereCondition).orderDesc(DiaryBeanDao.Properties.Date).build().list();
    }

    public void delete(DiaryBean item){
        dao.delete(item);
    }

    public void clear(){
        dao.deleteAll();
    }

}
