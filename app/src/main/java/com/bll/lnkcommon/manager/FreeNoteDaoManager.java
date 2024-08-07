package com.bll.lnkcommon.manager;

import com.bll.lnkcommon.MyApplication;
import com.bll.lnkcommon.greendao.DaoSession;
import com.bll.lnkcommon.greendao.FreeNoteBeanDao;
import com.bll.lnkcommon.greendao.NoteDao;
import com.bll.lnkcommon.greendao.RecordBeanDao;
import com.bll.lnkcommon.mvp.model.FreeNoteBean;
import com.bll.lnkcommon.mvp.model.Note;
import com.bll.lnkcommon.mvp.model.RecordBean;
import com.bll.lnkcommon.mvp.model.User;
import com.bll.lnkcommon.utils.SPUtil;

import org.greenrobot.greendao.query.WhereCondition;

import java.util.List;

public class FreeNoteDaoManager {

    /**
     * DaoSession
     */
    private DaoSession mDaoSession;
    private static FreeNoteDaoManager mDbController;
    private final FreeNoteBeanDao dao;
    private static WhereCondition whereUser;

    /**
     * 构造初始化
     */
    public FreeNoteDaoManager() {
        mDaoSession = MyApplication.Companion.getMDaoSession();
        dao = mDaoSession.getFreeNoteBeanDao();
    }

    /**
     * 获取单例（context 最好用application的context  防止内存泄漏）
     */
    public static FreeNoteDaoManager getInstance() {
        if (mDbController == null) {
            synchronized (FreeNoteDaoManager.class) {
                if (mDbController == null) {
                    mDbController = new FreeNoteDaoManager();
                }
            }
        }
        User mUser=SPUtil.INSTANCE.getObj("user", User.class);
        long userId =0;
        if (mUser!=null) {
            userId=mUser.accountId;
        }
        whereUser= FreeNoteBeanDao.Properties.UserId.eq(userId);
        return mDbController;
    }

    public void insertOrReplace(FreeNoteBean bean) {
        dao.insertOrReplace(bean);
    }

    public List<FreeNoteBean> queryList( int page, int pageSize) {
        WhereCondition whereCondition= FreeNoteBeanDao.Properties.Type.eq(0);
        return dao.queryBuilder().where(whereUser,whereCondition).orderDesc(FreeNoteBeanDao.Properties.Date)
                .offset((page-1)*pageSize).limit(pageSize).build().list();
    }

    public FreeNoteBean queryBean() {
        WhereCondition whereCondition= FreeNoteBeanDao.Properties.IsSave.eq(false);
        List<FreeNoteBean> items=dao.queryBuilder().where(whereUser,whereCondition).orderDesc(FreeNoteBeanDao.Properties.Date).build().list();
        if (items.size()>0){
            return items.get(0);
        }
        else {
            return null;
        }
    }

    public FreeNoteBean queryByDate(long date){
        WhereCondition whereCondition= FreeNoteBeanDao.Properties.Date.eq(date);
        List<FreeNoteBean> items=dao.queryBuilder().where(whereUser,whereCondition).orderDesc(FreeNoteBeanDao.Properties.Date).build().list();
        if (items.size()>0){
            return items.get(0);
        }
        else {
            return null;
        }
    }

    public List<FreeNoteBean> queryList() {
        WhereCondition whereCondition= FreeNoteBeanDao.Properties.Type.eq(0);
        return dao.queryBuilder().where(whereUser,whereCondition).orderDesc(FreeNoteBeanDao.Properties.Date).build().list();
    }

    public boolean isExist(long date) {
        WhereCondition whereCondition= FreeNoteBeanDao.Properties.Date.eq(date);
        return dao.queryBuilder().where(whereUser,whereCondition).build().unique()!=null;
    }

    public void deleteBean(FreeNoteBean bean){
        dao.delete(bean);
    }

    public void clear(){
        dao.deleteAll();
    }
}
