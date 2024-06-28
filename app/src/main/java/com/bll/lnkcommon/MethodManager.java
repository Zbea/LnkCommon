package com.bll.lnkcommon;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import com.bll.lnkcommon.manager.AppDaoManager;
import com.bll.lnkcommon.manager.BookDaoManager;
import com.bll.lnkcommon.mvp.model.AppBean;
import com.bll.lnkcommon.mvp.model.Book;
import com.bll.lnkcommon.mvp.model.PrivacyPassword;
import com.bll.lnkcommon.mvp.model.User;
import com.bll.lnkcommon.ui.activity.AccountLoginActivity;
import com.bll.lnkcommon.ui.activity.MainActivity;
import com.bll.lnkcommon.utils.ActivityManager;
import com.bll.lnkcommon.utils.AppUtils;
import com.bll.lnkcommon.utils.SPUtil;
import com.bll.lnkcommon.utils.SToast;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

public class MethodManager {

    /**
     * 退出登录(失效)
     * @param context
     */
    public static void logoutFailure(Context context){
        SToast.showText(R.string.login_timeout);
        SPUtil.INSTANCE.putString("token", "");
        SPUtil.INSTANCE.removeObj("user");
        EventBus.getDefault().post(Constants.USER_EVENT);
        DataBeanManager.INSTANCE.getStudents().clear();
        DataBeanManager.INSTANCE.getStudents().clear();
        EventBus.getDefault().post(Constants.STUDENT_EVENT);
        ActivityManager.getInstance().finishOthers(MainActivity.class);
        context.startActivity(new Intent(context, AccountLoginActivity.class));

        //发出退出登录广播
        Intent intent = new Intent();
        intent.putExtra("token", "");
        intent.putExtra("userId", 0);
        intent.setAction(Constants.LOGOUT_BROADCAST_EVENT);
        context.sendBroadcast(intent);
    }

    /**
     * 退出登录
     * @param context
     */
    public static void logout(Context context){
        SPUtil.INSTANCE.putString("token", "");
        SPUtil.INSTANCE.removeObj("user");
        EventBus.getDefault().post(Constants.USER_EVENT);
        ActivityManager.getInstance().finishOthers(MainActivity.class);

        DataBeanManager.INSTANCE.getStudents().clear();
        EventBus.getDefault().post(Constants.STUDENT_EVENT);

        //发出退出登录广播
        Intent intent = new Intent();
        intent.putExtra("token", "");
        intent.putExtra("userId", 0);
        intent.setAction(Constants.LOGOUT_BROADCAST_EVENT);
        context.sendBroadcast(intent);
    }

    /**
     * 跳转阅读器
     * @param context
     * @param bookBean
     */
    public static void gotoBookDetails(Context context, Book bookBean)  {
        AppUtils.stopApp(context,Constants.PACKAGE_READER);
        User user=SPUtil.INSTANCE.getObj("user", User.class);

        bookBean.isLook=true;
        bookBean.time=System.currentTimeMillis();
        BookDaoManager.getInstance().insertOrReplaceBook(bookBean);
        EventBus.getDefault().post(Constants.BOOK_EVENT);

        List<AppBean> toolApps= AppDaoManager.getInstance().queryTool();
        JSONArray result =new JSONArray();
        for (AppBean item :toolApps) {
            if (Objects.equals(item.packageName, Constants.PACKAGE_GEOMETRY))
                continue;
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("appName", item.appName);
                jsonObject.put("packageName", item.packageName);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            result.put(jsonObject);
        }

        Intent intent = new Intent();
        intent.setAction( "com.geniatech.reader.action.VIEW_BOOK_PATH");
        intent.setPackage(Constants.PACKAGE_READER);
        intent.putExtra("path", bookBean.bookPath);
        intent.putExtra("key_book_id",bookBean.bookId+"");
        intent.putExtra("bookName", bookBean.bookName);
        intent.putExtra("tool",result.toString());
        intent.putExtra("userId",user!=null?user.accountId:0);
        intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED|Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 保存私密密码
     * type 0日记1密本
     * @param privacyPassword
     */
    public static void savePrivacyPassword(int type,PrivacyPassword privacyPassword){
        if (type==0){
            SPUtil.INSTANCE.putObj("privacyPasswordDiary",privacyPassword);
        }
        else{
            SPUtil.INSTANCE.putObj("privacyPasswordNote",privacyPassword);
        }
    }

    /**
     * 获取私密密码
     * type 0日记1密本
     * @return
     */
    public static PrivacyPassword getPrivacyPassword(int type){
         if (type==0){
             return SPUtil.INSTANCE.getObj("privacyPasswordDiary", PrivacyPassword.class);
        }
        else{
             return SPUtil.INSTANCE.getObj("privacyPasswordNote", PrivacyPassword.class);
        }
    }

    /**
     * 获取状态栏的值
     * @return
     */
    public static int getStatusBarValue(){
        return Settings.System.getInt(MyApplication.Companion.getMContext().getContentResolver(), "statusbar_hide_time", 0);
    }

    /**
     * 设置状态栏的值
     *
     * @return
     */
    public static void setStatusBarValue(int value){
        Settings.System.putInt(MyApplication.Companion.getMContext().getContentResolver(),"statusbar_hide_time", value);
    }

    /**
     * 获取url的格式后缀
     * @param url
     * @return
     */
    public static String getUrlFormat(String url){
        return url.substring(url.lastIndexOf("."));
    }

}
