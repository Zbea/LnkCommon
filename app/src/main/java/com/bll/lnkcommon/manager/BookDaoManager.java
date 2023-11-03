package com.bll.lnkcommon.manager;


import com.bll.lnkcommon.MyApplication;
import com.bll.lnkcommon.greendao.BookDao;
import com.bll.lnkcommon.greendao.DaoSession;
import com.bll.lnkcommon.mvp.model.Book;
import com.bll.lnkcommon.mvp.model.User;
import com.bll.lnkcommon.utils.SPUtil;

import org.greenrobot.greendao.query.WhereCondition;

import java.util.List;
import java.util.Objects;


public class BookDaoManager {

    /**
     * DaoSession
     */
    private DaoSession mDaoSession;
    private static BookDaoManager mDbController;
    private final BookDao bookDao;  //book表
    private static WhereCondition whereUser;


    /**
     * 构造初始化
     */
    public BookDaoManager() {
        mDaoSession = MyApplication.Companion.getMDaoSession();
        bookDao = mDaoSession.getBookDao(); //book表
    }

    /**
     * 获取单例（context 最好用application的context  防止内存泄漏）
     */
    public static BookDaoManager getInstance() {
        if (mDbController == null) {
            synchronized (BookDaoManager.class) {
                if (mDbController == null) {
                    mDbController = new BookDaoManager();
                }
            }
        }
        long userId = Objects.requireNonNull(SPUtil.INSTANCE.getObj("user", User.class)).accountId;
        whereUser= BookDao.Properties.UserId.eq(userId);
        return mDbController;
    }


    //增加书籍
    public void insertOrReplaceBook(Book bean) {
        bookDao.insertOrReplace(bean);
    }


    /**
     *  查找课本、作业、教学
     * @param type 0课本 6作业 7教学
     * @param bookID
     * @return
     */
    public Book queryTextBookByBookID(int type,int bookID) {
        WhereCondition whereCondition= BookDao.Properties.TypeId.eq(type);
        WhereCondition whereCondition1= BookDao.Properties.BookId.eq(bookID);
        return bookDao.queryBuilder().where(whereUser,whereCondition,whereCondition1).build().unique();
    }

    /**
     * 查找书籍
     * @param bookID
     * @return
     */
    public Book queryByBookID(int type,int bookID) {
        WhereCondition whereCondition= BookDao.Properties.Category.eq(type);
        WhereCondition whereCondition1;
        if (type==1){
            whereCondition1= BookDao.Properties.BookPlusId.eq(bookID);
        }
        else {
            whereCondition1= BookDao.Properties.BookId.eq(bookID);
        }
        return bookDao.queryBuilder().where(whereUser,whereCondition,whereCondition1).build().unique();
    }

    //查询所有书籍
    public List<Book> queryAllBook() {
        WhereCondition whereCondition=BookDao.Properties.Category.eq(1);
        return bookDao.queryBuilder().where(whereUser,whereCondition).orderDesc(BookDao.Properties.Time).build().list();
    }

    /**
     * 获取打开过的书籍
     * @param isLook
     * @return
     */
    public List<Book> queryAllBook(boolean isLook) {
        WhereCondition whereCondition=BookDao.Properties.Category.eq(1);
        WhereCondition whereCondition1=BookDao.Properties.IsLook.eq(isLook);
        return bookDao.queryBuilder().where(whereUser,whereCondition,whereCondition1).orderDesc(BookDao.Properties.Time).limit(13).build().list();
    }

    //根据类别 细分子类
    public List<Book> queryAllBook(String type) {
        WhereCondition whereCondition1=BookDao.Properties.Category.eq(1);
        WhereCondition whereCondition2=BookDao.Properties.SubtypeStr.eq(type);
        return bookDao.queryBuilder().where(whereUser,whereCondition1,whereCondition2)
                .orderDesc(BookDao.Properties.Time).build().list();
    }

    //根据类别 细分子类 分页处理
    public List<Book> queryAllBook(String type, int page, int pageSize) {
        WhereCondition whereCondition1=BookDao.Properties.Category.eq(1);
        WhereCondition whereCondition2=BookDao.Properties.SubtypeStr.eq(type);
        return bookDao.queryBuilder().where(whereUser,whereCondition1,whereCondition2)
                .orderDesc(BookDao.Properties.Time)
                .offset((page-1)*pageSize).limit(pageSize)
                .build().list();
    }

    //查询所有课本
    public List<Book> queryAllTextbook() {
        WhereCondition whereCondition=BookDao.Properties.Category.eq(0);
        return bookDao.queryBuilder().where(whereUser,whereCondition).orderDesc(BookDao.Properties.Time).build().list();
    }

    //查找课本 细分子类
    public List<Book> queryAllTextBook(String textType) {
        WhereCondition whereCondition1=BookDao.Properties.Category.eq(0);
        WhereCondition whereCondition2=BookDao.Properties.SubtypeStr.eq(textType);
        return bookDao.queryBuilder().where(whereUser,whereCondition1,whereCondition2)
                .orderDesc(BookDao.Properties.Time).build().list();
    }

    public List<Book> queryAllTextBook(String textType, int page, int pageSize) {
        WhereCondition whereCondition1=BookDao.Properties.Category.eq(0);
        WhereCondition whereCondition2=BookDao.Properties.SubtypeStr.eq(textType);
        return bookDao.queryBuilder().where(whereUser,whereCondition1,whereCondition2)
                .orderDesc(BookDao.Properties.Time)
                .offset((page-1)*pageSize).limit(pageSize)
                .build().list();
    }


    //删除书籍数据d对象
    public void deleteBook(Book book){
        bookDao.delete(book);
    }

    public void clear(){
        bookDao.deleteAll();
    }
}
