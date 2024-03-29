package com.bll.lnkcommon

import android.content.ClipData.Item
import com.bll.lnkcommon.MyApplication.Companion.mContext
import com.bll.lnkcommon.mvp.model.*
import com.bll.lnkcommon.utils.ToolUtils
import java.util.*

object DataBeanManager {

    var grades= mutableListOf<Grade>()
    var typeGrades= mutableListOf<Grade>()
    var courses= mutableListOf<ItemList>()
    var versions= mutableListOf<ItemList>()
    var students= mutableListOf<StudentBean>()
    var provinces= mutableListOf<AreaBean>()
    var friends= mutableListOf<FriendList.FriendBean>()

    val mainListTitle = arrayOf(mContext.getString(R.string.tab_home),mContext.getString(R.string.tab_bookcase),
        mContext.getString(R.string.tab_journal),mContext.getString(R.string.tab_note),mContext.getString(R.string.tab_app),
        mContext.getString(R.string.tab_teaching),mContext.getString(R.string.tab_homework))

    private val cloudListTitle = arrayOf(mContext.getString(R.string.tab_bookcase),mContext.getString(R.string.tab_teaching)
        ,mContext.getString(R.string.tab_note),mContext.getString(R.string.diary),mContext.getString(R.string.free_note)
        ,mContext.getString(R.string.screenshot))

    val homeworkType = arrayOf(mContext.getString(R.string.teacher_homework_str),mContext.getString(R.string.classGroup_exam_str),mContext.getString(R.string.school_exam_str)
        ,mContext.getString(R.string.my_homework),mContext.getString(R.string.my_homework_correct))

    val journalType =  arrayOf(mContext.getString(R.string.journal_day),mContext.getString(R.string.journal_week)
        ,mContext.getString(R.string.journal_fiction),mContext.getString(R.string.journal_comprehensive)
        ,mContext.getString(R.string.journal_specialty))

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
    fun getMainCloud(): MutableList<ItemList> {
        val list = mutableListOf<ItemList>()
        list.add(ItemList().apply {
            icon = mContext.getDrawable(R.mipmap.icon_tab_bookcase)
            icon_check = mContext.getDrawable(R.mipmap.icon_tab_bookcase_check)
            isCheck = true
            name = cloudListTitle[0]
        })
        list.add(ItemList().apply {
            icon = mContext.getDrawable(R.mipmap.icon_tab_textbook)
            icon_check = mContext.getDrawable(R.mipmap.icon_tab_textbook_check)
            name = cloudListTitle[1]
        })
        list.add(ItemList().apply {
            icon = mContext.getDrawable(R.mipmap.icon_tab_note)
            icon_check = mContext.getDrawable(R.mipmap.icon_tab_note_check)
            name = cloudListTitle[2]
        })
        list.add(ItemList().apply {
            icon = mContext.getDrawable(R.mipmap.icon_tab_diary)
            icon_check = mContext.getDrawable(R.mipmap.icon_tab_diary_check)
            name = cloudListTitle[3]
        })
        list.add(ItemList().apply {
            icon = mContext.getDrawable(R.mipmap.icon_tab_freenote)
            icon_check = mContext.getDrawable(R.mipmap.icon_tab_freenote_check)
            name = cloudListTitle[4]
        })
        list.add(ItemList().apply {
            icon = mContext.getDrawable(R.mipmap.icon_tab_screenshot)
            icon_check = mContext.getDrawable(R.mipmap.icon_tab_screenshot_check)
            name = cloudListTitle[5]
        })
        return list
    }

    /**
     * 获取index栏目
     *
     * @param context
     * @return
     */
    fun getMainData(): MutableList<ItemList> {
        val list = mutableListOf<ItemList>()
        list.add(ItemList().apply {
            icon = mContext.getDrawable(R.mipmap.icon_tab_home)
            icon_check = mContext.getDrawable(R.mipmap.icon_tab_home_check)
            isCheck = true
            name = mainListTitle[0]
        })
        list.add(ItemList().apply {
            icon = mContext.getDrawable(R.mipmap.icon_tab_bookcase)
            icon_check = mContext.getDrawable(R.mipmap.icon_tab_bookcase_check)
            name = mainListTitle[1]
        })
        list.add(ItemList().apply {
            icon = mContext.getDrawable(R.mipmap.icon_tab_qk)
            icon_check = mContext.getDrawable(R.mipmap.icon_tab_qk_check)
            name = mainListTitle[2]
        })
        list.add(ItemList().apply {
            icon = mContext.getDrawable(R.mipmap.icon_tab_note)
            icon_check = mContext.getDrawable(R.mipmap.icon_tab_note_check)
            name = mainListTitle[3]
        })
        list.add(ItemList().apply {
            icon = mContext.getDrawable(R.mipmap.icon_tab_app)
            icon_check = mContext.getDrawable(R.mipmap.icon_tab_app_check)
            name = mainListTitle[4]
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
                resContentId = R.mipmap.icon_diary_details_bg_1
            })
            list.add(ModuleBean().apply {
                name = mContext.getString(R.string.note_type_fgb)
                resId = R.mipmap.icon_note_module_bg_2
                resContentId = R.mipmap.icon_diary_details_bg_2
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
            list.add(PopupBean(1, mContext.getString(R.string.semester_last),true))
            list.add(PopupBean(2,mContext.getString(R.string.semester_next),false))
            return list
        }

    val popupSupplys: MutableList<PopupBean>
        get() {
            val list = mutableListOf<PopupBean>()
            list.add(PopupBean(1, mContext.getString(R.string.official_str),true))
            list.add(PopupBean(2,mContext.getString(R.string.thirdParty_str),false))
            return list
        }

    val bookStoreTypes: MutableList<ItemList>
        get() {
            val list = mutableListOf<ItemList>()
            list.add(ItemList(1, "古籍"))
            list.add(ItemList(2, "自然科学"))
            list.add(ItemList(3, "社会科学"))
            list.add(ItemList(4, "思维科学"))
            list.add(ItemList(5, "运动健康"))
            list.add(ItemList(6, "艺术才能"))
            return list
        }

    val weeks: MutableList<DateWeek>
        get() {
            val list= mutableListOf<DateWeek>()
            list.add(
                DateWeek("周一",  2, false)
            )
            list.add(
                DateWeek("周二",  3, false)
            )
            list.add(
                DateWeek("周三",  4, false)
            )
            list.add(
                DateWeek("周四",  5, false)
            )
            list.add(
                DateWeek("周五",  6, false)
            )
            list.add(
                DateWeek("周六",  7, false)
            )
            list.add(
                DateWeek("周日",  8, false)
            )
            return list
        }

    fun getWeekStr(week:Int):String{
        var weekStr=""
        for (item in weeks){
            if (item.week==week)
                weekStr=item.name
        }
        return weekStr
    }

}