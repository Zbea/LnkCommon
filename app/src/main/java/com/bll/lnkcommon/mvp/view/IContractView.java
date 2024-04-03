package com.bll.lnkcommon.mvp.view;

import com.bll.lnkcommon.mvp.model.AccountOrder;
import com.bll.lnkcommon.mvp.model.AccountXDList;
import com.bll.lnkcommon.mvp.model.AppList;
import com.bll.lnkcommon.mvp.model.AppUpdateBean;
import com.bll.lnkcommon.mvp.model.BookStore;
import com.bll.lnkcommon.mvp.model.BookStoreType;
import com.bll.lnkcommon.mvp.model.CalenderList;
import com.bll.lnkcommon.mvp.model.CloudList;
import com.bll.lnkcommon.mvp.model.CommonData;
import com.bll.lnkcommon.mvp.model.ExamList;
import com.bll.lnkcommon.mvp.model.ExamRankList;
import com.bll.lnkcommon.mvp.model.FriendList;
import com.bll.lnkcommon.mvp.model.HomeworkCorrectList;
import com.bll.lnkcommon.mvp.model.HomeworkTypeList;
import com.bll.lnkcommon.mvp.model.JournalList;
import com.bll.lnkcommon.mvp.model.MessageList;
import com.bll.lnkcommon.mvp.model.SchoolBean;
import com.bll.lnkcommon.mvp.model.Score;
import com.bll.lnkcommon.mvp.model.ShareNoteList;
import com.bll.lnkcommon.mvp.model.StudentBean;
import com.bll.lnkcommon.mvp.model.TeacherHomeworkList;
import com.bll.lnkcommon.mvp.model.User;
import com.bll.lnkcommon.mvp.model.WallpaperList;
import com.bll.lnkcommon.net.IBaseView;

import java.util.List;

public interface IContractView {

    //登录
    interface ILoginView extends IBaseView {
        void getLogin(User user);
        void getAccount(User user);
    }

    //注册 找回密码
    interface IRegisterView extends IBaseView {
        void onSms();
        void onRegister();
        void onFindPsd();
        void onEditPsd();
    }

    interface IAccountInfoView extends IBaseView {
        void onEditNameSuccess();
        void onBind();
        void onUnbind();
        void onListStudent(List<StudentBean> beans);
        void onListFriend(FriendList list);
    }

    //钱包页面回调
    interface IWalletView extends IBaseView {
        void onXdList(AccountXDList list);
        void onXdOrder(AccountOrder order);
        void checkOrder(AccountOrder order);
    }

    interface ISchoolView extends IBaseView{
        void onListSchools(List<SchoolBean> list);
    }

    //主页
    interface ICommonView extends IBaseView {
        void onCommon(CommonData commonData);
        void onAppUpdate(AppUpdateBean item);
    }

    interface IBookStoreView extends IBaseView {
        void onBook(BookStore bookStore);
        void onType(BookStoreType bookStoreType);
        void buyBookSuccess();
    }

    //应用
    interface IAPPView extends IBaseView {
        void onType(CommonData commonData);
        void onAppList(AppList appBean);
        void buySuccess();
    }

    interface ICalenderView extends IBaseView {
        void onList(CalenderList list);
        void buySuccess();
    }

    interface IWallpaperView extends IBaseView {
        void onList(WallpaperList list);
        void buySuccess();
    }

    interface IHomeworkView extends IBaseView{
        void onList(TeacherHomeworkList item);
        void onDeleteSuccess();
    }

    interface IMyHomeworkView extends IBaseView{
        void onList(HomeworkTypeList homeworkTypeList);
        void onCreateSuccess();
        void onDeleteSuccess();
        void onSendSuccess();
    }

    interface IScoreRankView extends IBaseView{
        void onScore(List<Score> scores);
        void onExamScore(ExamRankList list);
    }

    interface IRelationView extends IBaseView{
        void onListStudents(List<StudentBean> list);
        void onListFriend(FriendList list);
    }
    interface IHomeworkCorrectView extends IBaseView{
        void onList(HomeworkCorrectList list);
        void onToken(String token);
        void onUpdateSuccess();
        void onDeleteSuccess();
    }

    interface IShareNoteView extends IBaseView{
        void onList(ShareNoteList list);
        void onToken(String token);
        void onDeleteSuccess();
        void onShare();
    }

    interface IJournalView extends IBaseView{
        void onList(JournalList list);
    }


    interface IQiniuView extends IBaseView {
        void onToken(String token);
    }

    /**
     * 云书库上传
     */
    interface ICloudUploadView extends IBaseView{
        void onSuccess(List<Integer> cloudIds);
        void onDeleteSuccess();
    }

    interface ICloudView extends IBaseView {
        void onList(CloudList item);
        void onType(List<String> types);
        void onDelete();
    }

    interface IMessageView extends IBaseView{
        void onList(MessageList message);
        void onCommitSuccess();
    }

    interface IExamView extends IBaseView {
        void onList(ExamList list);
    }

}
