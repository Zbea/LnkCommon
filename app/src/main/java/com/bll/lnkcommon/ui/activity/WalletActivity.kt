package com.bll.lnkcommon.ui.activity

import android.app.Dialog
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import com.bll.lnkcommon.DataBeanManager
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.dialog.WalletBuyDialog
import com.bll.lnkcommon.dialog.WalletStudentRechargeDialog
import com.bll.lnkcommon.mvp.model.AccountOrder
import com.bll.lnkcommon.mvp.model.AccountQdBean
import com.bll.lnkcommon.mvp.model.User
import com.bll.lnkcommon.mvp.presenter.WalletPresenter
import com.bll.lnkcommon.mvp.view.IContractView
import com.bll.lnkcommon.utils.SPUtil
import com.king.zxing.util.CodeUtils
import kotlinx.android.synthetic.main.ac_wallet.*

class WalletActivity:BaseActivity(),IContractView.IWalletView{

    private var walletPresenter=WalletPresenter(this)
    private var xdDialog: WalletBuyDialog?=null
    private var xdList= mutableListOf<AccountQdBean>()
    private var qrCodeDialog:Dialog?=null
    private var orderThread: OrderThread?=null//定时器
    private val handlerThread = Handler(Looper.myLooper()!!)
    private var money=0

    override fun onXdList(list: MutableList<AccountQdBean>) {
        xdList= list
    }

    override fun onXdOrder(order: AccountOrder?) {
        showQrCodeDialog(order?.qrCode)
        checkOrderState(order?.outTradeNo)
    }

    override fun checkOrder(order: AccountOrder?) {
        //订单支付成功
        if (order?.status == 2) {
            handlerThread.removeCallbacks(orderThread!!)
            if (qrCodeDialog!=null)
                qrCodeDialog?.dismiss()
            runOnUiThread {
                tv_xdmoney.text = "" + order.amount
                mUser?.balance = order.amount
                SPUtil.putObj("user",mUser!!)
            }
        }
    }

    override fun transferSuccess() {
        tv_xdmoney.text="青豆:  "+(mUser?.balance!!-money)
        mUser?.balance=mUser?.balance!!-money
        SPUtil.putObj("user",mUser!!)
        showToast("转账成功")
    }

    override fun getAccount(user: User) {
        mUser=user
        tv_xdmoney.text="青豆:  "+mUser?.balance
        SPUtil.putObj("user",mUser!!)
    }

    override fun layoutId(): Int {
        return R.layout.ac_wallet
    }

    override fun initData() {
        mUser=getUser()
    }

    override fun initView() {


        tv_buy.setOnClickListener {
            if (xdList.size>0){
                getXdView()
            }
            else{
                walletPresenter.getXdList(true)
            }
        }

        tv_student.setOnClickListener {
            if (DataBeanManager.students.size==0){
                showToast("请先关联学生")
                return@setOnClickListener
            }
            WalletStudentRechargeDialog(this,mUser?.balance!!).builder()?.setOnClickListener{
                money,id->
                this.money=money
                val map=HashMap<String,Any>()
                map["accountId"]=id
                map["balance"]=money
                walletPresenter.transferQd(map)
            }
        }

        walletPresenter.getXdList(false)
        walletPresenter.accounts()

    }

    //购买学豆
    private fun getXdView(){
        if (xdDialog==null){
            xdDialog= WalletBuyDialog(this,xdList).builder()
            xdDialog?.setOnDialogClickListener { id ->
                xdDialog?.dismiss()
                walletPresenter.postXdOrder(id)
            }
        }
        else{
            xdDialog?.show()
        }
    }

    //展示支付二维码的图片
    private fun showQrCodeDialog(url: String?) {
        qrCodeDialog = Dialog(this)
        qrCodeDialog?.setContentView(R.layout.dialog_account_qrcode)
        qrCodeDialog?.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        val iv_qrcode = qrCodeDialog?.findViewById<ImageView>(R.id.iv_qrcode)
        qrCodeDialog?.show()
        val bitmap = CodeUtils.createQRCode(url, 300, null)
        iv_qrcode?.setImageBitmap(bitmap)

        qrCodeDialog?.setOnDismissListener {
            handlerThread.removeCallbacks(orderThread!!)
        }
    }

    //订单轮询 handler?
    private fun checkOrderState(orderID: String?) {
        //create thread
        if (orderThread != null) {
            handlerThread.removeCallbacks(orderThread!!)
        }
        orderThread = OrderThread(orderID)
        orderThread?.run()
    }

    //定时器 (定时请求订单状态)
    inner class OrderThread(private val orderID: String?) : Runnable {
        override fun run() {
            queryOrderById(orderID!!)
            handlerThread.postDelayed(this, 30*1000)
        }
        //查询订单状态接口
        private fun queryOrderById(orderID: String) {
            walletPresenter.checkOrder(orderID)
        }
    }


}