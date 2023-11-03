package com.bll.lnkcommon.manager;

import com.bll.lnkcommon.MyApplication;
import com.bll.lnkcommon.greendao.CalenderItemBeanDao;
import com.bll.lnkcommon.greendao.DaoSession;
import com.bll.lnkcommon.greendao.FreeNoteBeanDao;
import com.bll.lnkcommon.mvp.model.CalenderItemBean;
import com.bll.lnkcommon.mvp.model.FreeNoteBean;
import com.bll.lnkcommon.mvp.model.User;
import com.bll.lnkcommon.utils.SPUtil;

import org.greenrobot.greendao.query.WhereCondition;

import java.util.List;

public class CalenderDaoManager {

    /**
     * DaoSession
     */
    private DaoSession mDaoSession;
    private static CalenderDaoManager mDbController;
    private final CalenderItemBeanDao dao;
    private static WhereCondition whereUser;

    /**
     * 构造初始化
     */
    public CalenderDaoManager() {
        mDaoSession = MyApplication.Companion.getMDaoSession();
        dao = mDaoSession.getCalenderItemBeanDao();
    }

    /**
     * 获取单例（context 最好用application的context  防止内存泄漏）
     */
    public static CalenderDaoManager getInstance() {
        if (mDbController == null) {
            synchronized (CalenderDaoManager.class) {
                if (mDbController == null) {
                    mDbController = new CalenderDaoManager();
                }
            }
        }
        User mUser=SPUtil.INSTANCE.getObj("user", User.class);
        long userId =0;
        if (mUser!=null) {
            userId=mUser.accountId;
        }
        whereUser= CalenderItemBeanDao.Properties.UserId.eq(userId);
        return mDbController;
    }

    public void insertOrReplace(CalenderItemBean bean) {
        dao.insertOrReplace(bean);
    }

    public List<CalenderItemBean> queryListOld(int year) {
        WhereCondition whereCondition= CalenderItemBeanDao.Properties.Year.lt(year);
        return dao.queryBuilder().where(whereUser,whereCondition).orderDesc(CalenderItemBeanDao.Properties.Date).build().list();
    }

    public List<CalenderItemBean> queryList(int year) {
        WhereCondition whereCondition= CalenderItemBeanDao.Properties.Year.eq(year);
        return dao.queryBuilder().where(whereUser,whereCondition).orderDesc(CalenderItemBeanDao.Properties.Date).build().list();
    }

    public List<CalenderItemBean> queryList(int year,int index,int size) {
        WhereCondition whereCondition= CalenderItemBeanDao.Properties.Year.eq(year);
        return dao.queryBuilder().where(whereUser,whereCondition).orderDesc(CalenderItemBeanDao.Properties.Date)
                .offset(index-1).limit(size)
                .build().list();
    }

    public CalenderItemBean queryCalenderBean() {
        WhereCondition whereCondition= CalenderItemBeanDao.Properties.IsSet.eq(true);
        return dao.queryBuilder().where(whereUser,whereCondition).build().unique();
    }

    public boolean isExist(int pid){
        WhereCondition whereCondition= CalenderItemBeanDao.Properties.Pid.eq(pid);
        CalenderItemBean item=dao.queryBuilder().where(whereUser,whereCondition).build().unique();
        return item!=null;
    }

    public void setSetFalse(){
        WhereCondition whereCondition= CalenderItemBeanDao.Properties.IsSet.eq(true);
        CalenderItemBean item=dao.queryBuilder().where(whereUser,whereCondition).build().unique();
        if (item!=null){
            item.isSet=false;
            insertOrReplace(item);
        }
    }

    public void deleteBean(CalenderItemBean bean){
        dao.delete(bean);
    }


    public void deleteBeans(List<CalenderItemBean> items){
        dao.deleteInTx(items);
    }

    public void clear(){
        dao.deleteAll();
    }
}
