package com.bll.lnkcommon.mvp.view;

import com.bll.lnkcommon.mvp.model.AppList;
import com.bll.lnkcommon.mvp.model.BookStore;
import com.bll.lnkcommon.mvp.model.BookStoreType;
import com.bll.lnkcommon.mvp.model.CommonData;
import com.bll.lnkcommon.mvp.model.HomeworkCorrectList;
import com.bll.lnkcommon.mvp.model.HomeworkTypeList;
import com.bll.lnkcommon.mvp.model.SchoolBean;
import com.bll.lnkcommon.mvp.model.Score;
import com.bll.lnkcommon.mvp.model.StudentBean;
import com.bll.lnkcommon.mvp.model.TeacherHomeworkList;
import com.bll.lnkcommon.mvp.model.User;
import com.bll.lnkcommon.net.IBaseView;

import java.util.List;

public interface IContractView {

    //登录
    interface ILoginView extends IBaseView {
        void getLogin(User user);
        void getAccount(User user);
        void onStudentList(List<StudentBean> studentBeans);
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
        void onBindStudent();
        void onUnbindStudent();
        void onStudentList(List<StudentBean> studentBeans);
    }

    interface ISchoolView extends IBaseView{
        void onListSchools(List<SchoolBean> list);
    }

    //主页
    interface ICommonView extends IBaseView {
        void onCommon(CommonData commonData);
    }

    interface IBookStoreView extends IBaseView {
        void onBook(BookStore bookStore);
        void onType(BookStoreType bookStoreType);
        void buyBookSuccess();
    }

    //应用
    interface IAPPView extends IBaseView {
        void onAppList(AppList appBean);
        void buySuccess();
    }

    interface IHomeworkView extends IBaseView{
        void onList(TeacherHomeworkList item);
        void onDeleteSuccess();
        void onScore(List<Score> scores);
    }

    interface IMyHomeworkView extends IBaseView{
        void onList(HomeworkTypeList homeworkTypeList);
        void onCreateSuccess();
        void onDeleteSuccess();
        void onSendSuccess();
    }

    interface IStudentView extends IBaseView{
        void onListStudents(List<StudentBean> list);
    }
    interface IHomeworkCorrectView extends IBaseView{
        void onList(HomeworkCorrectList list);
        void onToken(String token);
        void onUpdateSuccess();
        void onDeleteSuccess();
        void onSendSuccess();
    }

}
