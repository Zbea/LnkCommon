package com.bll.lnkcommon.ui.activity

import android.view.View
import com.bll.lnkcommon.R
import com.bll.lnkcommon.base.BaseActivity
import com.bll.lnkcommon.dialog.PopupOperatingGuideCatalog
import com.bll.lnkcommon.mvp.model.ItemTypeBean
import com.bll.lnkcommon.utils.GlideUtils
import kotlinx.android.synthetic.main.ac_operating_guide.iv_content
import kotlinx.android.synthetic.main.common_title.tv_ok
import java.util.Collections
import java.util.regex.Pattern


class OperatingGuideActivity :BaseActivity() {
    private var path=""
    private var baseUrl="file:///android_asset/"
    private var popCatalog: PopupOperatingGuideCatalog?=null

    override fun layoutId(): Int {
        return R.layout.ac_operating_guide
    }

    override fun initData() {
        pageSize=1
        val types= mutableListOf("主页面","管理中心","实用工具")
        val paths= mutableListOf("main","manager","tool")
        for (i in types.indices) {
            itemTabTypes.add(ItemTypeBean().apply {
                title=types[i]
                path=paths[i]
                isCheck=i==0
            })
        }

        path=paths[0]
    }

    override fun initView() {
        setPageTitle("操作手册")
        showView(tv_ok)

        tv_ok.text="目录"
        tv_ok.setOnClickListener {
            if (popCatalog==null){
                popCatalog=PopupOperatingGuideCatalog(this,tv_ok).builder()
                popCatalog?.setOnSelectListener{ position,page->
                    for (item in itemTabTypes){
                        item.isCheck=false
                    }
                    itemTabTypes[position].isCheck=true
                    path=itemTabTypes[position].path
                    mTabTypeAdapter?.setNewData(itemTabTypes)
                    pageIndex=page
                    fetchData()
                }
            }
            else{
                popCatalog?.show()
            }
        }

        mTabTypeAdapter?.setNewData(itemTabTypes)
        fetchData()
    }

    override fun onTabClickListener(view: View, position: Int) {
        path=itemTabTypes[position].path
        pageIndex=1
        fetchData()
    }


    override fun fetchData() {
        val list= assets.list(path)!!.toList()
        sort(list)
        setPageNumber(list.size)
        val images= mutableListOf<String>()
        for (name in list){
            images.add(baseUrl+"${path}/"+name)
        }
        GlideUtils.setImageNoCacheUrl(this,images[pageIndex-1],iv_content)
    }

    private fun sort(list:List<String>){
        val pattern= Pattern.compile("\\d+") // 匹配一个或多个数字的表达式
        Collections.sort(list) { s1: String, s2: String ->
            val matcher1= pattern.matcher(s1)
            val matcher2= pattern.matcher(s2)
            if (matcher1.find() && matcher2.find()) {
                return@sort matcher1.group().toInt().compareTo(matcher2.group().toInt())
            } else {
                return@sort 0 // 如果找不到数字，则可以按需决定如何处理（例如，保持原始顺序）
            }
        }
    }
}