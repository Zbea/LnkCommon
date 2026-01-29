package com.bll.lnkcommon.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.bll.lnkcommon.R;
import com.bll.lnkcommon.mvp.model.AppBean;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class AppUtils {
    public final static String WIDTH = "width";

    public final static String HEIGHT = "height";

    /**
     * px转dp
     *
     * @param context The context
     * @param px      the pixel value
     * @return value in dp
     */
    public static int pxToDp(Context context, float px) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (int) ((px / displayMetrics.density) + 0.5f);
    }

    /**
     * dp转px
     *
     * @param context
     * @param dp
     * @return
     */
    public static int dpToPx(Context context, float dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (int) ((dp * displayMetrics.density) + 0.5f);
    }

    /**
     * 获取状态栏高度
     *
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen",
                "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 获取应用版本名
     *
     * @return 成功返回版本名， 失败返回null
     */
    public static String getVersionName(Context context) {
        PackageInfo packageInfo = getPackageInfo(context);
        if (packageInfo != null) {
            return packageInfo.versionName;
        }

        return null;
    }

    /**
     * 获取版本号
     * @param context
     * @return
     */
    public static int getVersionCode(Context context){
        PackageManager manager = context.getPackageManager();
        int code = 0;
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            code = info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return code;
    }


    /**
     * @param context 上下文信息
     * @return 获取包信息
     * getPackageName()是当前类的包名，0代表获取版本信息
     */
    public static PackageInfo getPackageInfo(Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            return packageManager.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void installApp(Context context, String filePath) {
        File apkFile = new File(filePath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(context, context.getString(R.string.authority), apkFile);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        }
        context.startActivity(intent);
    }

    /**
     * uninstall apk file
     * @param packageName
     */
    public static void uninstallAPK(Context context,String packageName){
        Uri uri=Uri.parse("package:"+packageName);
        Intent intent=new Intent(Intent.ACTION_DELETE,uri);
        context.startActivity(intent);
    }


    public static void getSystemProperty() {
        Field[] fields = Build.class.getFields();
        for (Field f : fields) {
            try {
                String name = f.getName();
                Object value = f.get(name);

                System.out.println("key:" + name + ":value:" + value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
//        try {
//            Class build = Class.forName("android.os.Build");
//            String customName = (String) build.getDeclaredField("MODEL").toString();
//            return customName;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }

    /**
     * 清除缓存数据
     *
     * @param context
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void clearAppData(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            manager.clearApplicationUserData();
        }

    }

    /**
     * 判断当前应用是否是debug状态
     */

    public static boolean isApkInDebug(Context context) {
        if (context == null) {
            return false;
        }
        try {
            ApplicationInfo info = context.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 是否是终端PDA
     * @return
     */
//    public static boolean HCPDA(){
//        if ("HC".equals(getSystemProperty())){
//            return true;
//        }
//        return false;
//    }

    /**
     * 关闭当前进程
     */
    public static void closeAppProcess() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * 获取手机名称
     *
     * @return 手机型号
     */
    public static String getDeviceName() {
        return Build.DEVICE;
    }

    /**
     * 获取手机型号
     *
     * @return 手机型号
     */
    public static String getModelName() {
        return Build.MODEL;
    }

    /**
     * 获取手机名称
     *
     * @return 手机名称
     */
    public static String getMobileName() {
        return Build.MANUFACTURER;
    }

    /**
     * 获取产品名称
     *
     * @return 产品名称
     */
    public static String getProductName() {
        return Build.PRODUCT;
    }


    /**
     * 获取手机IMEI
     *
     * @param context
     * @return
     */
    @SuppressLint("MissingPermission")
    public static final String getIMEI(Context context) {
        try {
            //实例化TelephonyManager对象
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            //获取IMEI号
            String imei = telephonyManager.getDeviceId();
            //在次做个验证，也不是什么时候都能获取到的啊
            if (imei == null) {
                imei = "";
            }
            return imei;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

    }

    /**
     * 根据apk路径获取包名
     *
     * @param context
     * @param apkPath
     * @return
     */
    public static String getApkInfo(Context context, String apkPath) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
        ApplicationInfo appInfo = null;
        String name = "";
        if (info != null) {
            appInfo = info.applicationInfo;
            name = appInfo.packageName;//此为apk包名    }}
        }
        return name;
    }

    /**
     * 关闭第三方应用
     * @param packageName
     */
    public static void stopApp(Context context,String packageName){
//        Process process= Runtime.getRuntime().exec("su");
//        OutputStream out = process.getOutputStream();
//        String cmd = "am force-stop " + packageName + " \n";
//        out.write(cmd.getBytes());
//        out.flush();
//        process.getOutputStream().close();
        ActivityManager am = (ActivityManager)context.getSystemService(
                Context.ACTIVITY_SERVICE);
        am.killBackgroundProcesses(packageName);
    }

    //根据包名启动app
    public static void startAPP(Context context, String appPackageName) throws Exception {
        if (isAvailable(context,appPackageName)){
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(appPackageName);
            context.startActivity(intent);
        }
    }

    public static boolean isAvailable(Context context,String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        // 获取所有已安装程序的包信息
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        for (int i = 0; i < pinfo.size(); i++) {
            // 循环判断是否存在指定包名
            if (pinfo.get(i).packageName.equalsIgnoreCase(packageName)) {
                return true;
            }
        }
        return false;
    }

    //重启app
    public static void reOpenApk(Context context){
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
        //杀掉以前进程
        android.os.Process.killProcess(android.os.Process.myPid());

    }

    public static List<AppBean> scanLocalInstallAppList(Context context) {
        PackageManager packageManager=context.getPackageManager();
        List<AppBean> apps = new ArrayList();
        Bitmap bitmap=null;
        try {
            List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
            for (int i = 0; i < packageInfos.size(); i++) {
                PackageInfo packageInfo = packageInfos.get(i);
                //过滤掉系统app
                if (isSystemApp(packageInfo)) {
                    continue;
                }
                AppBean appBean = new AppBean();
                appBean.appName = packageInfo.applicationInfo.loadLabel(packageManager).toString();
                appBean.packageName = packageInfo.packageName;
                Drawable drawable=packageInfo.applicationInfo.loadIcon(packageManager);
                if (drawable!=null){
                    if (drawable instanceof android.graphics.drawable.AdaptiveIconDrawable){
                        bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                        Canvas canvas =new Canvas(bitmap);
                        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                        drawable.draw(canvas);
                        appBean.imageByte=BitmapUtils.drawableToByte(bitmap);
                    }
                    else {
                        appBean.imageByte=BitmapUtils.drawableToByte(drawable);
                    }
                }
                apps.add(appBean);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            if (bitmap!=null){
                bitmap.recycle();
                bitmap=null;
            }
        }
        return apps;
    }

    private static boolean isSystemApp(PackageInfo pi) {
        boolean isSysApp = (pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1;
        boolean isSysUpd = (pi.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 1;
        return isSysApp || isSysUpd;
    }


    /**
     * 查看本地应用图标
     * @param context
     * @param packageName
     * @return
     */
    public static byte[] scanLocalInstallAppDrawable(Context context, String packageName) {
        PackageManager packageManager=context.getPackageManager();
        byte[] btyes = new byte[0];
        Drawable drawable=null;
        Bitmap bitmap=null;
        try {
            List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
            for (int i = 0; i < packageInfos.size(); i++) {
                PackageInfo packageInfo = packageInfos.get(i);
                if (packageInfo.packageName.equals(packageName))
                {
                    drawable=packageInfo.applicationInfo.loadIcon(packageManager);
                    if (drawable instanceof android.graphics.drawable.AdaptiveIconDrawable){
                        bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                        Canvas canvas =new Canvas(bitmap);
                        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                        drawable.draw(canvas);
                        btyes=BitmapUtils.drawableToByte(bitmap);
                    }
                    else {
                        btyes=BitmapUtils.drawableToByte(drawable);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        finally {
            if (bitmap!=null){
                bitmap.recycle();
                bitmap=null;
            }
        }
        return btyes;
    }

    /**
     * 全版本兼容：判断指定包名的应用是否在前台运行
     * @param context 上下文（建议使用ApplicationContext）
     * @param targetPackageName 目标应用包名
     * @return true=前台运行；false=后台运行/未运行
     */
    public static boolean isAppInForegroundCompat(Context context, String targetPackageName) {
        if (context == null || targetPackageName == null || targetPackageName.trim().isEmpty()) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Android 5.0（API 21）及以上：使用UsageStatsManager
            return isAppInForegroundLollipop(context, targetPackageName);
        } else {
            // Android 5.0以下：使用ActivityManager（GET_TASKS权限）
            return isAppInForegroundLowVersion(context, targetPackageName);
        }
    }

    /**
     * Android 5.0+ 实现：使用UsageStatsManager（官方推荐）
     */
    private static boolean isAppInForegroundLollipop(Context context, String targetPackageName) {
        // 1. 获取UsageStatsManager实例
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        if (usageStatsManager == null) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        // 2. 查询最近1小时内的应用使用记录（时间范围可自定义，如 1000*60*10=10分钟）
        long startTime = currentTime - 1000 * 60 * 60;
        List<UsageStats> statsList = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_BEST,
                startTime,
                currentTime
        );

        // 3. 无使用记录，引导用户开启权限并返回false
        if (statsList == null || statsList.isEmpty()) {
            return false;
        }

        // 4. 按最后使用时间排序，获取最新使用的应用
        SortedMap<Long, UsageStats> sortedStats = new TreeMap<>();
        for (UsageStats stats : statsList) {
            sortedStats.put(stats.getLastTimeUsed(), stats);
        }

        UsageStats latestUsageStats = null;
        if (!sortedStats.isEmpty()) {
            latestUsageStats = sortedStats.get(sortedStats.lastKey());
        }

        // 5. 对比包名，判断是否为前台应用
        if (latestUsageStats != null && targetPackageName.equals(latestUsageStats.getPackageName())) {
            return true;
        }
        return false;
    }

    /**
     * Android 5.0以下 & Android 10以下 实现：使用ActivityManager（已废弃，仅兼容低版本）
     */
    @SuppressWarnings("deprecation")
    private static boolean isAppInForegroundLowVersion(Context context, String targetPackageName) {
        // Android 10（API 29）及以上，该API已废弃，直接返回false
        if (Build.VERSION.SDK_INT >= 29) {
            return false;
        }
        // 1. 获取ActivityManager实例
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null) {
            return false;
        }
        // 2. 获取前台任务列表（仅取1个，即当前最顶层任务）
        List<ActivityManager.RunningTaskInfo> taskList = activityManager.getRunningTasks(1);
        if (taskList == null || taskList.isEmpty()) {
            return false;
        }
        // 3. 获取前台任务的包名并对比
        ActivityManager.RunningTaskInfo foregroundTask = taskList.get(0);
        if (foregroundTask.topActivity == null) {
            return false;
        }
        String foregroundPackageName = foregroundTask.topActivity.getPackageName();
        return targetPackageName.equals(foregroundPackageName);
    }


}
