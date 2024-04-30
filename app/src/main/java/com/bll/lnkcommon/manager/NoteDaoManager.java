package com.bll.lnkcommon.manager;

import com.bll.lnkcommon.MyApplication;
import com.bll.lnkcommon.greendao.DaoSession;
import com.bll.lnkcommon.greendao.NoteDao;
import com.bll.lnkcommon.mvp.model.Note;
import com.bll.lnkcommon.mvp.model.User;
import com.bll.lnkcommon.utils.SPUtil;

import org.greenrobot.greendao.query.WhereCondition;

import java.util.List;
import java.util.Objects;

public class NoteDaoManager {

    /**
     * DaoSession
     */
    private DaoSession mDaoSession;
    private static NoteDaoManager mDbController;
    private final NoteDao dao;
    private static WhereCondition whereUser;

    /**
     * 构造初始化
     */
    public NoteDaoManager() {
        mDaoSession=MyApplication.Companion.getMDaoSession();
        dao = mDaoSession.getNoteDao();
    }

    /**
     * 获取单例（context 最好用application的context  防止内存泄漏）
     */
    public static NoteDaoManager getInstance() {
        if (mDbController == null) {
            synchronized (NoteDaoManager.class) {
                if (mDbController == null) {
                    mDbController = new NoteDaoManager();
                }
            }
        }
        long userId = Objects.requireNonNull(SPUtil.INSTANCE.getObj("user", User.class)).accountId;
        whereUser= NoteDao.Properties.UserId.eq(userId);
        return mDbController;
    }

    public void insertOrReplace(Note bean) {
        dao.insertOrReplace(bean);
    }

    public Note queryBean(long id) {
        WhereCondition whereCondition=NoteDao.Properties.Id.eq(id);
        return dao.queryBuilder().where(whereUser,whereCondition).orderDesc(NoteDao.Properties.Date).build().unique();
    }
    public List<Note> queryAll() {
        return dao.queryBuilder().where(whereUser).orderDesc(NoteDao.Properties.Date).build().list();
    }

    /**
     * @return
     */
    public List<Note> queryAll(String type) {
        WhereCondition whereCondition=NoteDao.Properties.TypeStr.eq(type);
        return dao.queryBuilder().where(whereUser,whereCondition).orderDesc(NoteDao.Properties.Date).build().list();
    }

    /**
     * @return
     */
    public List<Note> queryAll(String type, int page, int pageSize) {
        WhereCondition whereCondition=NoteDao.Properties.TypeStr.eq(type);
        return dao.queryBuilder().where(whereUser,whereCondition).orderDesc(NoteDao.Properties.Date)
                .offset((page-1)*pageSize).limit(pageSize).build().list();
    }

    public void editNotes(String type,String editType){
        List<Note> notes=queryAll(type);
        for (Note note: notes) {
            note.typeStr=editType;
        }
        dao.insertOrReplaceInTx(notes);
    }

    /**
     * 是否存在笔记（云书库）
     * @return
     */
    public Boolean isExistCloud(String typeStr,String title,long date){
        WhereCondition whereCondition1=NoteDao.Properties.TypeStr.eq(typeStr);
        WhereCondition whereCondition2= NoteDao.Properties.Title.eq(title);
        WhereCondition whereCondition3= NoteDao.Properties.Date.eq(date);
        return dao.queryBuilder().where(whereUser,whereCondition1,whereCondition2,whereCondition3).unique()!=null;
    }

    /**
     * 是否存在笔记
     * @return
     */
    public Boolean isExist(String typeStr,String title){
        WhereCondition whereCondition1=NoteDao.Properties.TypeStr.eq(typeStr);
        WhereCondition whereCondition2= NoteDao.Properties.Title.eq(title);
        return dao.queryBuilder().where(whereUser,whereCondition1,whereCondition2).unique()!=null;
    }

    public void deleteBean(Note bean){
        dao.delete(bean);
    }

    public void clear(){
        dao.deleteAll();
    }
}
