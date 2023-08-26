package com.bll.lnkcommon.dialog

import android.app.Dialog
import android.content.Context
import android.widget.ImageView
import com.bll.lnkcommon.R
import com.bll.lnkcommon.manager.AppDaoManager
import com.bll.lnkcommon.mvp.model.AppBean
import com.bll.lnkcommon.mvp.model.User
import com.bll.lnkcommon.utils.BitmapUtils
import com.bll.lnkcommon.utils.SPUtil

class AppMenuDialog(val context: Context, val appBean: AppBean){

    private var iv_1:ImageView?=null
    private var iv_2:ImageView?=null
    private var iv_3:ImageView?=null
    private var iv_4:ImageView?=null
    private var iv_5:ImageView?=null

    fun builder(): AppMenuDialog {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_app_menu)
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        appBean.userId=if (SPUtil.getObj("user", User::class.java)==null) 0 else SPUtil.getObj("user", User::class.java)?.accountId!!

        iv_1=dialog.findViewById(R.id.iv_1)
        iv_2=dialog.findViewById(R.id.iv_2)
        iv_3=dialog.findViewById(R.id.iv_3)
        iv_4=dialog.findViewById(R.id.iv_4)
        iv_5=dialog.findViewById(R.id.iv_5)

        val list=AppDaoManager.getInstance().queryMenu()
        for (item in list){
            when(item.sort){
                1->{
                    iv_1?.setImageDrawable(BitmapUtils.byteToDrawable(item.imageByte))
                }
                2->{
                    iv_2?.setImageDrawable(BitmapUtils.byteToDrawable(item.imageByte))
                }
                3->{
                    iv_3?.setImageDrawable(BitmapUtils.byteToDrawable(item.imageByte))
                }
                4->{
                    iv_4?.setImageDrawable(BitmapUtils.byteToDrawable(item.imageByte))
                }
                5->{
                    iv_5?.setImageDrawable(BitmapUtils.byteToDrawable(item.imageByte))
                }
            }
        }

        iv_1?.setOnClickListener {
            setAddMenu(1,iv_1!!)
        }

        iv_2?.setOnClickListener {
            setAddMenu(2,iv_2!!)
        }

        iv_3?.setOnClickListener {
            setAddMenu(3,iv_3!!)
        }

        iv_4?.setOnClickListener {
            setAddMenu(4,iv_4!!)
        }

        iv_5?.setOnClickListener {
            setAddMenu(5,iv_5!!)
        }

        return this
    }

    private fun setAddMenu(type:Int,view: ImageView){
        if (AppDaoManager.getInstance().isExist(appBean.packageName,1)){
            val bean=AppDaoManager.getInstance().queryByType(appBean.packageName,1)
            if (bean.sort!=type){
                when(bean.sort){
                    1->{
                        iv_1?.setImageDrawable(null)
                    }
                    2->{
                        iv_2?.setImageDrawable(null)
                    }
                    3->{
                        iv_3?.setImageDrawable(null)
                    }
                    4->{
                        iv_4?.setImageDrawable(null)
                    }
                    5->{
                        iv_5?.setImageDrawable(null)
                    }
                }
                bean.sort=type
                AppDaoManager.getInstance().delete(type)
                AppDaoManager.getInstance().insertOrReplace(bean)
            }
        }
        else{
            appBean.type=1
            appBean.sort=type
            AppDaoManager.getInstance().insertOrReplace(appBean)
        }
        view.setImageDrawable(BitmapUtils.byteToDrawable(appBean.imageByte))
    }

}