package com.bll.lnkcommon.manager;

import com.bll.lnkcommon.MethodManager;
import com.bll.lnkcommon.MyApplication;
import com.bll.lnkcommon.greendao.DaoSession;
import com.bll.lnkcommon.greendao.ItemTypeBeanDao;
import com.bll.lnkcommon.mvp.model.Date;
import com.bll.lnkcommon.mvp.model.ItemTypeBean;
import com.bll.lnkcommon.mvp.model.User;
import com.bll.lnkcommon.utils.SPUtil;

import org.greenrobot.greendao.query.WhereCondition;

import java.util.List;
import java.util.Objects;

public class ItemTypeDaoManager {

    /**
     * DaoSession
     */
    private DaoSession mDaoSession;
    private static ItemTypeDaoManager mDbController;
    private final ItemTypeBeanDao dao;
    private static WhereCondition whereUser;

    /**
     * 构造初始化
     */
    public ItemTypeDaoManager() {
        mDaoSession = MyApplication.Companion.getMDaoSession();
        dao = mDaoSession.getItemTypeBeanDao();
    }

    /**
     * 获取单例（context 最好用application的context  防止内存泄漏）
     */
    public static ItemTypeDaoManager getInstance() {
        if (mDbController == null) {
            synchronized (ItemTypeDaoManager.class) {
                if (mDbController == null) {
                    mDbController = new ItemTypeDaoManager();
                }
            }
        }
        long userId = MethodManager.getAccountId();
        whereUser= ItemTypeBeanDao.Properties.UserId.eq(userId);
        return mDbController;
    }

    public void insertOrReplace(ItemTypeBean bean) {
        dao.insertOrReplace(bean);
    }

    public List<ItemTypeBean> queryAll(int type) {
        WhereCondition whereUser1= ItemTypeBeanDao.Properties.Type.eq(type);
        return dao.queryBuilder().where(whereUser,whereUser1).orderAsc(ItemTypeBeanDao.Properties.Date).build().list();
    }

    public List<ItemTypeBean> queryAllOrderDesc(int type) {
        WhereCondition whereUser1= ItemTypeBeanDao.Properties.Type.eq(type);
        return dao.queryBuilder().where(whereUser,whereUser1).orderDesc(ItemTypeBeanDao.Properties.Date).build().list();
    }

    public Boolean isExist(String title,int type){
        WhereCondition whereUser1= ItemTypeBeanDao.Properties.Title.eq(title);
        WhereCondition whereUser2= ItemTypeBeanDao.Properties.Type.eq(type);
        return dao.queryBuilder().where(whereUser,whereUser1,whereUser2).unique()!=null;
    }

    /**
     * 查看日记分类是否已经下载
     * @param typeId
     * @return
     */
    public Boolean isExistDiaryType(int typeId){
        WhereCondition whereUser1= ItemTypeBeanDao.Properties.TypeId.eq(typeId);
        WhereCondition whereUser2= ItemTypeBeanDao.Properties.Type.eq(4);
        return !dao.queryBuilder().where(whereUser, whereUser1, whereUser2).build().list().isEmpty();
    }

    public void deleteBean(ItemTypeBean bean){
        dao.delete(bean);
    }

    public void clear(int type){
        dao.deleteInTx(queryAll(type));
    }
    public void clear(){
        dao.deleteAll();
    }
}
