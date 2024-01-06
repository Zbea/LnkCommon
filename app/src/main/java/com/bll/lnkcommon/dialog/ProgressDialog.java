package com.bll.lnkcommon.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.bll.lnkcommon.Constants;
import com.bll.lnkcommon.R;
import com.bll.lnkcommon.utils.DP2PX;


public class ProgressDialog {


    private Context context;
    private Dialog mDialog;

    public ProgressDialog(Context context) {
        this.context = context;
        createDialog();
    }

    public void createDialog() {
        mDialog = new Dialog(context);
        mDialog.setContentView(R.layout.dialog_progress);
        mDialog.setCanceledOnTouchOutside(false);
        Window window = mDialog.getWindow();
        //要加上设置背景，否则dialog宽高设置无作用
        window.setBackgroundDrawableResource(android.R.color.transparent);
    }

    public void show() {
        Activity activity= (Activity) context;
        if (activity!=null && !activity.isFinishing() && !activity.isDestroyed() &&
                mDialog != null && !mDialog.isShowing()) {
            mDialog.show();
        }
    }



    public void dismiss() {
        Activity activity= (Activity) context;
        if (activity!=null && !activity.isFinishing() && !activity.isDestroyed() &&
                mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

}
