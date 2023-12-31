package com.bll.lnkcommon;

import android.content.Context;
import android.content.Intent;

import com.bll.lnkcommon.manager.AppDaoManager;
import com.bll.lnkcommon.manager.BookDaoManager;
import com.bll.lnkcommon.mvp.model.AppBean;
import com.bll.lnkcommon.mvp.model.Book;
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
        intent.putExtra("userId",user.accountId);
        intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED|Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("android.intent.extra.LAUNCH_SCREEN", 2);
        context.startActivity(intent);
    }
    
}
