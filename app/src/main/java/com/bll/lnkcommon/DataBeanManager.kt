package com.bll.lnkcommon

import com.bll.lnkcommon.MyApplication.Companion.mContext
import com.bll.lnkcommon.mvp.model.*
import com.bll.lnkcommon.utils.ToolUtils
import java.util.*

object DataBeanManager {

    var grades= mutableListOf<Grade>()
    var typeGrades= mutableListOf<Grade>()
    var courses= mutableListOf<ItemList>()
    var students= mutableListOf<StudentBean>()
    var provinces= mutableListOf<AreaBean>()
    var friends= mutableListOf<FriendList.FriendBean>()

    val mainListTitle = arrayOf("首页","书架","笔记","应用","教材","作业")

    val homeworkType = arrayOf("老师作业","学校考试","我的作业","我的批改")


    val textbookType = arrayOf(
        mContext.getString(R.string.textbook_tab_text),mContext.getString(R.string.textbook_tab_course),
        mContext.getString(R.string.textbook_tab_homework),mContext.getString(R.string.textbook_tab_homework_other)
    )

    val popupGrades: MutableList<PopupBean>
        get() {
            val list= mutableListOf<PopupBean>()
            for (i in grades.indices){
                list.add(PopupBean(grades[i].type, grades[i].desc, i == 0))
            }
            return list
        }

    val popupTypeGrades: MutableList<PopupBean>
        get() {
            val list= mutableListOf<PopupBean>()
            for (i in typeGrades.indices){
                list.add(PopupBean(typeGrades[i].type, typeGrades[i].desc, i == 0))
            }
            return list
        }

    val popupCourses: MutableList<PopupBean>
        get() {
            val list= mutableListOf<PopupBean>()
            for (i in courses.indices){
                list.add(PopupBean(courses[i].type, courses[i].desc, false))
            }
            return list
        }

    /**
     * 获取index栏目
     *
     * @param context
     * @return
     */
    fun getMainData(): MutableList<MainListBean> {
        val list = mutableListOf<MainListBean>()
        list.add(MainListBean().apply {
            icon = mContext.getDrawable(R.mipmap.icon_main_home)
            icon_check = mContext.getDrawable(R.mipmap.icon_main_home_check)
            checked = true
            name = mainListTitle[0]
        })
        list.add(MainListBean().apply {
            icon = mContext.getDrawable(R.mipmap.icon_main_bookcase)
            icon_check = mContext.getDrawable(R.mipmap.icon_main_bookcase_check)
            checked = false
            name = mainListTitle[1]
        })
        list.add(MainListBean().apply {
            icon = mContext.getDrawable(R.mipmap.icon_main_note)
            icon_check = mContext.getDrawable(R.mipmap.icon_main_note_check)
            checked = false
            name = mainListTitle[2]
        })
        list.add(MainListBean().apply {
            icon = mContext.getDrawable(R.mipmap.icon_main_app)
            icon_check = mContext.getDrawable(R.mipmap.icon_main_app_check)
            checked = false
            name = mainListTitle[3]
        })
        return list
    }

    //日记内容选择
    val noteModuleDiary: MutableList<ModuleBean>
        get() {
            val list= mutableListOf<ModuleBean>()
            list.add(ModuleBean().apply {
                name = mContext.getString(R.string.note_type_hgb)
                resId = R.mipmap.icon_note_module_bg_1
                resContentId = R.mipmap.icon_note_details_bg_6
            })
            list.add(ModuleBean().apply {
                name = mContext.getString(R.string.note_type_fgb)
                resId = R.mipmap.icon_note_module_bg_2
                resContentId = R.mipmap.icon_note_details_bg_7
            })
            return list
        }

    //笔记本内容选择
    val noteModuleBook: MutableList<ModuleBean>
        get() {
            val list= mutableListOf<ModuleBean>()
            list.add(ModuleBean().apply {
                name = mContext.getString(R.string.note_type_kbb)
                resId = R.drawable.bg_gray_stroke_10dp_corner
                resContentId = 0
                resFreenoteBg=R.mipmap.icon_freenote_bg_0
            })
            list.add(ModuleBean().apply {
                name = mContext.getString(R.string.note_type_hgb)
                resId = R.mipmap.icon_note_module_bg_1
                resContentId = R.mipmap.icon_note_details_bg_1
                resFreenoteBg=R.mipmap.icon_freenote_bg_1
            })
            list.add(ModuleBean().apply {
                name = mContext.getString(R.string.note_type_fgb)
                resId = R.mipmap.icon_note_module_bg_2
                resContentId = R.mipmap.icon_note_details_bg_2
                resFreenoteBg=R.mipmap.icon_freenote_bg_2
            })
            list.add(ModuleBean().apply {
                name = mContext.getString(R.string.note_type_yyb)
                resId = R.mipmap.icon_note_module_bg_3
                resContentId = R.mipmap.icon_note_details_bg_3
                resFreenoteBg=R.mipmap.icon_freenote_bg_3
            })
            list.add(ModuleBean().apply {
                name = mContext.getString(R.string.note_type_tzb)
                resId = R.mipmap.icon_note_module_bg_4
                resContentId = R.mipmap.icon_note_details_bg_4
                resFreenoteBg=R.mipmap.icon_freenote_bg_4
            })
            list.add(ModuleBean().apply {
                name = mContext.getString(R.string.note_type_wxp)
                resId = R.mipmap.icon_note_module_bg_5
                resContentId = R.mipmap.icon_note_details_bg_5
                resFreenoteBg=R.mipmap.icon_freenote_bg_5
            })
            return list
        }

    //学期选择
    val popupSemesters: MutableList<PopupBean>
        get() {
            val list = mutableListOf<PopupBean>()
            list.add(PopupBean(1, "上学期",true))
            list.add(PopupBean(2,"下学期",false))
            return list
        }

    val bookStoreTypes: MutableList<ItemList>
        get() {
            val list = mutableListOf<ItemList>()
            list.add(ItemList(1,"古籍"))
            list.add(ItemList(2, "自然科学"))
            list.add(ItemList(3, "社会科学"))
            list.add(ItemList(4, "思维科学"))
            list.add(ItemList(5, "运动才艺"))
            return list
        }

    //封面
    fun homeworkCoverStr(): String{
        val list= mutableListOf<ModuleBean>()
        val moduleBean = ModuleBean()
        moduleBean.resId = R.mipmap.icon_homework_cover_1
        val moduleBean1 = ModuleBean()
        moduleBean1.resId = R.mipmap.icon_homework_cover_2
        val moduleBean2 = ModuleBean()
        moduleBean2.resId = R.mipmap.icon_homework_cover_3
        val moduleBean3 = ModuleBean()
        moduleBean3.resId = R.mipmap.icon_homework_cover_4
        list.add(moduleBean)
        list.add(moduleBean1)
        list.add(moduleBean2)
        list.add(moduleBean3)
        val index= Random().nextInt(list.size)
        return ToolUtils.getImageResStr(mContext,list[index].resId)
    }

}