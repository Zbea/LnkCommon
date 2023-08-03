package com.bll.lnkcommon.manager;

import com.bll.lnkcommon.MyApplication;
import com.bll.lnkcommon.greendao.AppBeanDao;
import com.bll.lnkcommon.greendao.DaoSession;
import com.bll.lnkcommon.greendao.NoteContentDao;
import com.bll.lnkcommon.mvp.model.AppBean;
import com.bll.lnkcommon.mvp.model.NoteContent;
import com.bll.lnkcommon.mvp.model.User;
import com.bll.lnkcommon.utils.SPUtil;

import org.greenrobot.greendao.query.WhereCondition;

import java.util.List;
import java.util.Objects;

public class AppDaoManager {

    /**
     * DaoSession
     */
    private DaoSession mDaoSession;
    private static AppDaoManager mDbController;
    private final AppBeanDao dao;
    private static WhereCondition whereUser;

    /**
     * 构造初始化
     */
    public AppDaoManager() {
        mDaoSession = MyApplication.Companion.getMDaoSession();
        dao = mDaoSession.getAppBeanDao(); //note表
    }

    /**
     * 获取单例（context 最好用application的context  防止内存泄漏）
     */
    public static AppDaoManager getInstance() {
        if (mDbController == null) {
            synchronized (AppDaoManager.class) {
                if (mDbController == null) {
                    mDbController = new AppDaoManager();
                }
            }
        }
        long userId = Objects.requireNonNull(SPUtil.INSTANCE.getObj("user", User.class)).accountId;
        whereUser= AppBeanDao.Properties.UserId.eq(userId);
        return mDbController;
    }

    public void insertOrReplaces(List<AppBean> beans) {
        dao.insertOrReplaceInTx(beans);
    }

    public List<AppBean> queryAll() {
        return dao.queryBuilder().where(whereUser).build().list();
    }

    public void deletes(List<AppBean> beans){
        dao.deleteInTx(beans);
    }

}
