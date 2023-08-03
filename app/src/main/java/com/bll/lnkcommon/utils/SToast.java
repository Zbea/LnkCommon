package com.bll.lnkcommon.utils;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IntRange;
import androidx.annotation.StringRes;

import com.bll.lnkcommon.R;


public class SToast {
    private static Context ctx;
    private static Toast toast;
    private static Handler handler;

    public static void initToast(Context context) {
        ctx = context;
        handler = new Handler(ctx.getMainLooper());
    }

    public static void showText(@StringRes int res) {
        showText(ctx.getString(res), Toast.LENGTH_SHORT);
    }


    /**
     * 默认显示短的提示。
     *
     * @param str
     */
    public static void showText(CharSequence str) {
        showText(str, Toast.LENGTH_SHORT);
    }

    public static void showTextLong(CharSequence str) {
        showText(str, Toast.LENGTH_LONG);
    }

    public static void showText(@StringRes int res, @IntRange(from = 0, to = 1) int duration) {
        showText(ctx.getString(res), duration);
    }

    public static void showText(final CharSequence str, @IntRange(from = 0, to = 1) final int duration) {
        if (Thread.currentThread().getId() != 1) {
            // 在子线程
            handler.post(new Runnable() {
                @Override
                public void run() {
                    finalShow(str, duration);
                }
            });
        } else {
            finalShow(str, duration);
        }
    }

    private static void finalShow(CharSequence str, @IntRange(from = 0, to = 1) int duration) {
        if (toast == null) {
            toast = Toast.makeText(ctx, str,duration);
        }
        if (Build.VERSION.SDK_INT < 30) {
            TextView text = toast.getView().findViewById(android.R.id.message);
            text.setTextColor(Color.WHITE);
            text.setTextSize(20);
            text.setPadding(30, 20, 30, 20);
            toast.getView().setBackground(ctx.getDrawable(R.drawable.bg_black_solid_10dp_corner));
        }
        toast.setText(str);
        toast.setDuration(duration);
        toast.show();
    }

    /**
     * 取消显示
     * 建议放在 baseActivity中做统一处理
     */
    public static void cancel() {
        if (toast != null) {
            toast.cancel();
        }
    }
}
