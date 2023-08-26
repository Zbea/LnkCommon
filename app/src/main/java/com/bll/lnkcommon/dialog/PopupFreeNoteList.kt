import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bll.lnkcommon.FileAddress
import com.bll.lnkcommon.R
import com.bll.lnkcommon.dialog.CommonDialog
import com.bll.lnkcommon.dialog.DateDialog
import com.bll.lnkcommon.dialog.InputContentDialog
import com.bll.lnkcommon.manager.FreeNoteDaoManager
import com.bll.lnkcommon.manager.RecordDaoManager
import com.bll.lnkcommon.mvp.model.FreeNoteBean
import com.bll.lnkcommon.mvp.model.PopupBean
import com.bll.lnkcommon.utils.DP2PX
import com.bll.lnkcommon.utils.DateUtils
import com.bll.lnkcommon.utils.FileUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import org.w3c.dom.Text
import java.io.File

class PopupFreeNoteList(var context: Context, var view: View) {

    private var list= mutableListOf<FreeNoteBean>()
    private var mPopupWindow: PopupWindow? = null
    private var dayStartTime=0L
    private var dayEndTime=0L
    private var tv_date:TextView?=null
    private var mAdapter: MAdapter?=null

    fun builder(): PopupFreeNoteList {
        val popView = LayoutInflater.from(context).inflate(R.layout.popup_free_note_list, null, false)
        mPopupWindow = PopupWindow(context).apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            // 设置PopupWindow的内容view
            contentView = popView
            isFocusable = true // 设置PopupWindow可获得焦点
            isTouchable = true // 设置PopupWindow可触摸
            isOutsideTouchable = true // 设置非PopupWindow区域可触摸
            width=DP2PX.dip2px(context,280f)
        }

        dayStartTime=DateUtils().startOfDayInMillis
        dayEndTime=DateUtils().endOfDayInMillis

        tv_date= popView.findViewById(R.id.tv_date)
        val iv_up = popView.findViewById<ImageView>(R.id.iv_up)
        val iv_down = popView.findViewById<ImageView>(R.id.iv_down)

        val rvList = popView.findViewById<RecyclerView>(R.id.rv_list)
        rvList.layoutManager = LinearLayoutManager(context)//创建布局管理
        mAdapter = MAdapter(R.layout.item_free_note, null)
        rvList.adapter=mAdapter
        mAdapter?.bindToRecyclerView(rvList)
        mAdapter?.setOnItemClickListener { adapter, view, position ->
            onSelectListener?.onSelect(list[position])
            dismiss()
        }
        mAdapter?.setOnItemChildClickListener { adapter, view, position ->
            val item=list[position]
            if (view.id==R.id.iv_edit){
                InputContentDialog(context,item.title).builder().setOnDialogClickListener{
                    item.title=it
                    FreeNoteDaoManager.getInstance().insertOrReplace(item)
                    mAdapter?.notifyItemChanged(position)
                }
            }
            if (view.id==R.id.iv_delete){
                CommonDialog(context).setContent("确定删除？").builder()
                    .setDialogClickListener(object : CommonDialog.OnDialogClickListener {
                        override fun cancel() {
                        }
                        override fun ok() {
                            FreeNoteDaoManager.getInstance().deleteBean(item)
                            FileUtils.deleteFile(File(FileAddress().getPathFreeNote(DateUtils.longToString(item.date))))
                            mAdapter?.remove(position)
                        }
                    })
            }
        }

        tv_date?.setOnClickListener {
            DateDialog(context).builder().setOnDateListener { dateStr, dateTim ->
                dayStartTime=dateTim
                dayEndTime=DateUtils().getEndOfDayInMillis(dateTim)
                setChangeContent()
            }
        }

        iv_up.setOnClickListener {
            dayStartTime -= 24 * 60 * 60 * 1000
            dayEndTime=DateUtils().getEndOfDayInMillis(dayStartTime)
            setChangeContent()
        }

        iv_down.setOnClickListener {
            dayStartTime += 24 * 60 * 60 * 1000
            dayEndTime=DateUtils().getEndOfDayInMillis(dayStartTime)
            setChangeContent()
        }
        setChangeContent()
        show()
        return this
    }

    private fun setChangeContent(){
        tv_date?.text=DateUtils.longToStringDataNoHour(dayStartTime)
        list=FreeNoteDaoManager.getInstance().queryList(dayStartTime,dayEndTime)
        mAdapter?.setNewData(list)
    }

    fun dismiss() {
        if (mPopupWindow != null) {
            mPopupWindow?.dismiss()
        }
    }

    fun show() {
        if (mPopupWindow != null) {
            setChangeContent()
            mPopupWindow?.showAsDropDown(view, 0, 10, Gravity.RIGHT)
        }
    }

    private var onSelectListener: OnSelectListener?=null

    fun setOnSelectListener(onSelectListener: OnSelectListener)
    {
        this.onSelectListener=onSelectListener
    }

    fun interface OnSelectListener{
        fun onSelect(item: FreeNoteBean)
    }


    private class MAdapter(layoutResId: Int, data: List<FreeNoteBean>?) :
        BaseQuickAdapter<FreeNoteBean, BaseViewHolder>(layoutResId, data) {

        override fun convert(helper: BaseViewHolder, item: FreeNoteBean) {
            helper.setText(R.id.tv_title, item.title)
            helper.addOnClickListener(R.id.iv_delete,R.id.iv_edit)
        }

    }

}