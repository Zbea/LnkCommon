package com.bll.lnkcommon.manager;

import com.bll.lnkcommon.MyApplication;
import com.bll.lnkcommon.greendao.DaoSession;
import com.bll.lnkcommon.greendao.NoteContentDao;
import com.bll.lnkcommon.mvp.model.NoteContent;
import com.bll.lnkcommon.mvp.model.User;
import com.bll.lnkcommon.utils.SPUtil;

import org.greenrobot.greendao.query.WhereCondition;

import java.util.List;
import java.util.Objects;

public class NoteContentDaoManager {

    /**
     * DaoSession
     */
    private DaoSession mDaoSession;
    private static NoteContentDaoManager mDbController;
    private final NoteContentDao dao;  //note表
    private static WhereCondition whereUser;

    /**
     * 构造初始化
     */
    public NoteContentDaoManager() {
        mDaoSession = MyApplication.Companion.getMDaoSession();
        dao = mDaoSession.getNoteContentDao(); //note表
    }

    /**
     * 获取单例（context 最好用application的context  防止内存泄漏）
     */
    public static NoteContentDaoManager getInstance() {
        if (mDbController == null) {
            synchronized (NoteContentDaoManager.class) {
                if (mDbController == null) {
                    mDbController = new NoteContentDaoManager();
                }
            }
        }
        long userId = Objects.requireNonNull(SPUtil.INSTANCE.getObj("user", User.class)).accountId;
        whereUser= NoteContentDao.Properties.UserId.eq(userId);
        return mDbController;
    }

    public void insertOrReplaceNote(NoteContent bean) {
        dao.insertOrReplace(bean);
    }

    public long getInsertId(){
        List<NoteContent> queryList = dao.queryBuilder().build().list();
        return queryList.get(queryList.size()-1).id;
    }


    public List<NoteContent> queryAll(String type, String notebookTitle) {
        WhereCondition whereCondition=NoteContentDao.Properties.TypeStr.eq(type);
        WhereCondition whereCondition1=NoteContentDao.Properties.NotebookTitle.eq(notebookTitle);
        return dao.queryBuilder().where(whereUser,whereCondition,whereCondition1).build().list();
    }

    public void editNoteTitles(String type, String notebookTitle,String editTitle){
        List<NoteContent> noteContents=queryAll(type,notebookTitle);
        for (NoteContent noteContent: noteContents) {
            noteContent.notebookTitle=editTitle;
        }
        dao.insertOrReplaceInTx(noteContents);
    }

    public void editNoteTypes(String type, String notebookTitle,String editType){
        List<NoteContent> noteContents=queryAll(type,notebookTitle);
        for (NoteContent noteContent: noteContents) {
            noteContent.typeStr=editType;
        }
        dao.insertOrReplaceInTx(noteContents);
    }

    public void deleteNote(NoteContent noteContent){
        dao.delete(noteContent);
    }

    public void deleteType(String type,String notebookTitle){
        WhereCondition whereCondition=NoteContentDao.Properties.TypeStr.eq(type);
        WhereCondition whereCondition1=NoteContentDao.Properties.NotebookTitle.eq(notebookTitle);
        List<NoteContent> list = dao.queryBuilder().where(whereUser,whereCondition,whereCondition1).build().list();
        dao.deleteInTx(list);
    }

    public void clear(){
        dao.deleteAll();
    }
}
