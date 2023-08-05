package com.bll.lnkcommon.manager;

import android.util.Log;

import com.bll.lnkcommon.MyApplication;
import com.bll.lnkcommon.MyApplication;
import com.bll.lnkcommon.greendao.DaoSession;
import com.bll.lnkcommon.greendao.RecordBeanDao;
import com.bll.lnkcommon.mvp.model.RecordBean;
import com.bll.lnkcommon.mvp.model.User;
import com.bll.lnkcommon.utils.SPUtil;

import org.greenrobot.greendao.query.WhereCondition;

import java.util.List;
import java.util.Objects;

public class RecordDaoManager {

    /**
     * DaoSession
     */
    private DaoSession mDaoSession;
    private static RecordDaoManager mDbController;
    private final RecordBeanDao recordBeanDao;
    private static WhereCondition whereUser;

    /**
     * 构造初始化
     */
    public RecordDaoManager() {
        mDaoSession = MyApplication.Companion.getMDaoSession();
        recordBeanDao = mDaoSession.getRecordBeanDao();
    }

    /**
     * 获取单例（context 最好用application的context  防止内存泄漏）
     */
    public static RecordDaoManager getInstance() {
        if (mDbController == null) {
            synchronized (RecordDaoManager.class) {
                if (mDbController == null) {
                    mDbController = new RecordDaoManager();
                }
            }
        }
        User mUser=SPUtil.INSTANCE.getObj("user", User.class);
        long userId =0;
        if (mUser!=null) {
            userId=mUser.accountId;
        }
        whereUser= RecordBeanDao.Properties.UserId.eq(userId);
        return mDbController;
    }

    public void insertOrReplace(RecordBean bean) {
        recordBeanDao.insertOrReplace(bean);
    }

    public List<RecordBean> queryAllList() {
        return recordBeanDao.queryBuilder().where(whereUser)
                .orderDesc(RecordBeanDao.Properties.Date).build().list();
    }

    public List<RecordBean> queryAllList(int page, int pageSize) {
        return recordBeanDao.queryBuilder().where(whereUser)
                .orderDesc(RecordBeanDao.Properties.Date).offset((page-1)*pageSize).limit(pageSize).build().list();
    }

    public void deleteBean(RecordBean bean){
        recordBeanDao.delete(bean);
    }

    public void clear(){
        recordBeanDao.deleteAll();
    }

}
