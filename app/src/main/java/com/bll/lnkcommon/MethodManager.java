package com.bll.lnkcommon;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bll.lnkcommon.dialog.ImageDialog;
import com.bll.lnkcommon.manager.AppDaoManager;
import com.bll.lnkcommon.manager.BookDaoManager;
import com.bll.lnkcommon.manager.TextbookGreenDaoManager;
import com.bll.lnkcommon.mvp.model.ItemTypeBean;
import com.bll.lnkcommon.mvp.model.book.TextbookBean;
import com.bll.lnkcommon.mvp.model.AppBean;
import com.bll.lnkcommon.mvp.model.book.Book;
import com.bll.lnkcommon.mvp.model.AreaBean;
import com.bll.lnkcommon.mvp.model.PrivacyPassword;
import com.bll.lnkcommon.mvp.model.User;
import com.bll.lnkcommon.ui.activity.account.AccountLoginActivity;
import com.bll.lnkcommon.ui.activity.MainActivity;
import com.bll.lnkcommon.ui.activity.drawing.FileDrawingActivity;
import com.bll.lnkcommon.utils.ActivityManager;
import com.bll.lnkcommon.utils.AppUtils;
import com.bll.lnkcommon.utils.FileUtils;
import com.bll.lnkcommon.utils.SPUtil;
import com.bll.lnkcommon.utils.SToast;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MethodManager {

    public static User getUser(){
        return SPUtil.INSTANCE.getObj("user", User.class);
    }

    public static boolean isLogin(){
        String tokenStr=SPUtil.INSTANCE.getString("token");
        return !TextUtils.isEmpty(tokenStr) && getUser()!=null;
    }

    public static long getAccountId(){
        User user=SPUtil.INSTANCE.getObj("user", User.class);
        if (user==null){
            return 0L;
        }
        else {
            return user.accountId;
        }
    }

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
        EventBus.getDefault().post(Constants.STUDENT_EVENT);
        ActivityManager.getInstance().finishOthers(MainActivity.class);
        context.startActivity(new Intent(context, AccountLoginActivity.class));

        //发出退出登录广播
        Intent intent = new Intent();
        intent.putExtra("token", "");
        intent.putExtra("userId", 0L);
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
        intent.putExtra("userId", 0L);
        intent.setAction(Constants.LOGOUT_BROADCAST_EVENT);
        context.sendBroadcast(intent);
    }

    public static void gotoDocument(Context context,File file){
        String format=FileUtils.getUrlFormat(file.getPath());
        if (format.equals(".ppt") || format.equals(".pptx")){
            if (AppUtils.isAvailable(context,Constants.PACKAGE_PPT)) {
                String url=SPUtil.INSTANCE.getString(file.getName());
                gotoPptDetails(context,file.getPath(),url);
            }
        }
        else if (format.equals(".png") || format.equals(".jpg")||format.equals(".jpeg")){
            List<String> images=new ArrayList<>();
            images.add(file.getPath());
            new ImageDialog(context,images).builder();
        }
        else {
                String fileName=FileUtils.getUrlName(file.getPath());
                String drawPath=file.getParent()+"/"+fileName+"draw/";
                Intent intent=new Intent();
                intent.setAction("com.geniatech.reader.action.VIEW_BOOK_PATH");
                intent.setPackage(Constants.PACKAGE_READER);
                intent.putExtra("path", file.getPath());
                intent.putExtra("bookName", fileName);
                intent.putExtra("tool", getJsonArray().toString());
                intent.putExtra("userId", getAccountId());
                intent.putExtra("type", 1);
                intent.putExtra("drawPath", drawPath);
                intent.putExtra("key_book_type", 1);
                intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
        }
    }

    public static void gotoPptDetails(Context context,String localPath,String url){
        if (AppUtils.isAvailable(context,Constants.PACKAGE_PPT)){
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(Constants.PACKAGE_PPT,"com.htfyun.dualdocreader.OpenFileActivity"));
            intent.putExtra("path", localPath);
            intent.putExtra("url", url);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    /**
     * 跳转阅读器
     * @param context
     * @param bookBean key_book_type 0普通书籍 1pdf书籍 2pdf课本 3文档
     */
    public static void gotoBookDetails(Context context,int type, Book bookBean)  {
        AppUtils.stopApp(context,Constants.PACKAGE_READER);

        bookBean.isLook=true;
        bookBean.time=System.currentTimeMillis();
        BookDaoManager.getInstance().insertOrReplaceBook(bookBean);

        String format = FileUtils.getUrlFormat(bookBean.bookPath);
        int key_type = 0;
        if (type==1){
            if (format.contains("pdf")) {
                key_type = 1;
            }
        }
        else {
            key_type=2;
        }

        Intent intent = new Intent();
        intent.setAction( "com.geniatech.reader.action.VIEW_BOOK_PATH");
        intent.setPackage(Constants.PACKAGE_READER);
        intent.putExtra("path", bookBean.bookPath);
        intent.putExtra("key_book_id",bookBean.bookId+"");
        intent.putExtra("bookName", bookBean.bookName);
        intent.putExtra("tool",getJsonArray().toString());
        intent.putExtra("userId",getUser()!=null?getUser().accountId:0);
        intent.putExtra("type", type);
        intent.putExtra("drawPath", bookBean.bookDrawPath);
        intent.putExtra("key_book_type", key_type);
        intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED|Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        Handler handler=new Handler(Looper.getMainLooper());
        handler.postDelayed(() ->
                        EventBus.getDefault().post(Constants.BOOK_EVENT)
                ,3000);
    }

    private static @NonNull JSONArray getJsonArray() {
        List<AppBean> toolApps= AppDaoManager.getInstance().queryTool();
        JSONArray result =new JSONArray();
        for (AppBean item : toolApps) {
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
        return result;
    }


    public static void deleteBook(Book book){
        BookDaoManager.getInstance().deleteBook(book); //删除本地数据库
        FileUtils.deleteFile(new File(book.bookPath));//删除下载的书籍资源
        FileUtils.deleteFile(new File(book.bookDrawPath));
        EventBus.getDefault().post(Constants.BOOK_EVENT) ;
    }

    public static void deleteTextbook(TextbookBean book){
        TextbookGreenDaoManager.getInstance().deleteBook(book); //删除本地数据库
        FileUtils.deleteFile(new File(book.bookPath));//删除下载的书籍资源
        FileUtils.deleteFile(new File(book.bookDrawPath));
        EventBus.getDefault().post(Constants.TEXT_BOOK_EVENT);
    }

    /**
     * 跳转截图列表
     * @param context
     * @param index
     * @param tabPath
     */
    public static void gotoScreenFile(Context context,int index,String tabPath){
        Intent intent=new Intent(context, FileDrawingActivity.class);
        intent.putExtra("pageIndex",index);
        intent.putExtra("pagePath",tabPath);
        ActivityManager.getInstance().finishActivity(intent.getClass().getName());
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
     * 加载不失真背景
     * @param context
     * @param resId
     * @param imageView
     */
    public static void setImageResource(Context context, int resId, ImageView imageView){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false; // 防止自动缩放
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId, options);
        imageView.setImageBitmap(bitmap);
    }

    /**
     * 加载本地图片
     * @param path
     * @param imageView
     */
    public static void setImageFile(String path, ImageView imageView){
        File file=new File(path);
        if (file.exists()){
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            imageView.setImageBitmap(bitmap);
        }
    }

    /**
     * 获取省
     * @param context
     * @return
     * @throws IOException
     */
    public static List<AreaBean> getProvinces(Context context) throws IOException {
        String areaJson = FileUtils.readFileContent(context.getResources().getAssets().open("city.json"));
        return new Gson().fromJson(areaJson, new TypeToken<List<AreaBean>>(){}.getType());
    }

    /**
     * 初始化不选中 指定位置选中
     * @param list
     * @param position
     * @return
     */
    public static List<ItemTypeBean> setItemTypeBeanCheck(List<ItemTypeBean> list, int position){
        if (list.size()>position){
            for (ItemTypeBean item:list) {
                item.isCheck=false;
            }
            list.get(position).isCheck=true;
        }
        return list;
    }

    public static ItemTypeBean getDefaultItemTypeDocument(){
        String title="默认";
        ItemTypeBean itemTypeBean=new ItemTypeBean();
        itemTypeBean.type=6;
        itemTypeBean.path=new FileAddress().getPathDocument(title);
        itemTypeBean.title=title;
        return itemTypeBean;
    }

    /**
     * 创建共享文件件
     * @param context
     * @param path
     */
    public static void createFileScan(Context context,String path){
        if (!FileUtils.isExist(path)){
            File file=new File(path+"/1");
            file.mkdirs();
            MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()},null, null);
            new Handler().postDelayed(() -> {
                FileUtils.deleteFile(file);
                MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()},null, null);
            },10*1000);
        }
    }

    /**
     * 通知共享文件
     * @param context
     * @param path
     */
    public static void notifyFileScan(Context context,String path){
        MediaScannerConnection.scanFile(context, new String[]{path},null, null);
    }
    /**
     * 通知共享文件
     * @param context
     * @param paths
     */
    public static void notifyFileScan(Context context,String[] paths){
        MediaScannerConnection.scanFile(context, paths,null, null);
    }
}
