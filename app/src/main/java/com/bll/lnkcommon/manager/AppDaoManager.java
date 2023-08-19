package com.bll.lnkcommon.manager;

import com.bll.lnkcommon.MyApplication;
import com.bll.lnkcommon.greendao.AppBeanDao;
import com.bll.lnkcommon.greendao.DaoSession;
import com.bll.lnkcommon.greendao.FreeNoteBeanDao;
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
        User mUser=SPUtil.INSTANCE.getObj("user", User.class);
        long userId =0;
        if (mUser!=null) {
            userId=mUser.accountId;
        }
        whereUser= AppBeanDao.Properties.UserId.eq(userId);
        return mDbController;
    }

    public void insertOrReplace(AppBean bean) {
        dao.insertOrReplace(bean);
    }

    /**
     * 获取工具应用
     * @return
     */
    public List<AppBean> queryTool() {
        WhereCondition whereCondition1= AppBeanDao.Properties.Type.eq(0);
        return dao.queryBuilder().where(whereUser,whereCondition1).build().list();
    }

    /**
     * 获取首页菜单
     * @return
     */
    public List<AppBean> queryMenu() {
        WhereCondition whereCondition1= AppBeanDao.Properties.Type.eq(1);
        return dao.queryBuilder().where(whereUser,whereCondition1).orderAsc(AppBeanDao.Properties.Sort).build().list();
    }

    /**
     * 删除工具应用
     * @param packageName
     */
    public void delete(String packageName) {
        WhereCondition where1= AppBeanDao.Properties.PackageName.eq(packageName);
        WhereCondition where2= AppBeanDao.Properties.Type.eq(0);
        AppBean appBean=dao.queryBuilder().where(whereUser,where1,where2).build().unique();
        if (appBean!=null)
            dao.deleteInTx(appBean);
    }

    public boolean isExist(String packageName,int type){
        WhereCondition where1= AppBeanDao.Properties.PackageName.eq(packageName);
        WhereCondition where2= AppBeanDao.Properties.Type.eq(type);
        AppBean appBean=dao.queryBuilder().where(whereUser,where1,where2).build().unique();
        return appBean!=null;
    }

    public AppBean queryByType(String packageName,int type){
        WhereCondition where1= AppBeanDao.Properties.PackageName.eq(packageName);
        WhereCondition where2= AppBeanDao.Properties.Type.eq(type);
        return dao.queryBuilder().where(whereUser,where1,where2).build().unique();
    }

    public void delete(int sort) {
        WhereCondition where1= AppBeanDao.Properties.Sort.eq(sort);
        WhereCondition where2= AppBeanDao.Properties.Type.eq(1);
        AppBean appBean=dao.queryBuilder().where(whereUser,where1,where2).build().unique();
        if (appBean!=null)
            dao.deleteInTx(appBean);
    }

    public void deletes(List<AppBean> beans){
        dao.deleteInTx(beans);
    }

}
