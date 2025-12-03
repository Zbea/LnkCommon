package com.bll.lnkcommon

import android.graphics.Bitmap
import com.bll.lnkcommon.MyApplication.Companion.mContext
import com.bll.lnkcommon.mvp.model.*
import com.bll.lnkcommon.mvp.model.catalog.CatalogChildBean
import com.bll.lnkcommon.mvp.model.catalog.CatalogParentBean
import com.bll.lnkcommon.mvp.model.teaching.ResultStandardItem
import com.chad.library.adapter.base.entity.MultiItemEntity
import java.util.*

object DataBeanManager {

    var copyBitmap: Bitmap?=null//剪切、复制保存bitmap
    var isSystemUpdateShow=false//是否可以更新系统应用 true正在更新

    var grades= mutableListOf<ItemList>()
    var typeGrades= mutableListOf<ItemList>()
    var courses= mutableListOf<ItemList>()
    var versions= mutableListOf<ItemList>()
    var students= mutableListOf<StudentBean>()

    val bookType = arrayOf(
        "诗经楚辞", "唐诗宋词", "古代经典",
        "四大名著", "中国科技", "小说散文",
        "外国原著", "历史地理", "政治经济",
        "军事战略", "科学技术", "运动才艺"
    )

    val mainListTitle = arrayOf(mContext.getString(R.string.tab_home),mContext.getString(R.string.tab_bookcase),"文档",mContext.getString(R.string.tab_note),mContext.getString(R.string.tab_app),
        mContext.getString(R.string.tab_teaching),mContext.getString(R.string.tab_homework))

    private val cloudListTitle = arrayOf(mContext.getString(R.string.tab_bookcase),mContext.getString(R.string.tab_teaching)
        ,mContext.getString(R.string.tab_note),mContext.getString(R.string.diary)
        ,mContext.getString(R.string.screenshot))

    val homeworkType = arrayOf(mContext.getString(R.string.teacher_homework_str),mContext.getString(R.string.classGroup_exam_str),mContext.getString(R.string.school_exam_str)
        ,mContext.getString(R.string.my_homework),mContext.getString(R.string.my_homework_correct))

    var resources = arrayOf("应用中心","实用工具","锁屏壁纸","跳页日历")

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

    fun popupCourses(type:Int): MutableList<PopupBean>{
        val list= mutableListOf<PopupBean>()
        for (i in courses.indices){
            list.add(PopupBean(courses[i].type, courses[i].desc, courses[i].type==type))
        }
        return list
    }

    val popupStudents: MutableList<PopupBean>
        get() {
            val list= mutableListOf<PopupBean>()
            for (i in students.indices) {
                list.add(PopupBean(students[i].accountId, students[i].nickname, i == 0))
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
            icon = mContext.getDrawable(R.mipmap.icon_tab_screenshot)
            icon_check = mContext.getDrawable(R.mipmap.icon_tab_screenshot_check)
            name = cloudListTitle[4]
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
            icon = mContext.getDrawable(R.mipmap.icon_tab_document)
            icon_check = mContext.getDrawable(R.mipmap.icon_tab_document_check)
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

    val textBookTypes: MutableList<ItemTypeBean>
        get() {
            val list= mutableListOf<ItemTypeBean>()
            list.add(ItemTypeBean().apply {
                title = "我的课本"
                isCheck=true
            })
            list.add(ItemTypeBean().apply {
                title = "参考课本"
                isCheck=false
            })
            list.add(ItemTypeBean().apply {
                title = "我的教辅"
                isCheck=false
            })
            list.add(ItemTypeBean().apply {
                title = "参考教辅"
                isCheck=false
            })
            return list
        }

    //日记内容选择
    val diaryModules: MutableList<ModuleBean>
        get() {
            val list= mutableListOf<ModuleBean>()
            list.add(ModuleBean().apply {
                name = "横格本-11mm"
                resId = R.mipmap.icon_diary_module_bg_1
                resContentId = R.mipmap.icon_diary_details_bg_1
            })
            list.add(ModuleBean().apply {
                name = "横格本-9mm"
                resId = R.mipmap.icon_diary_module_bg_3
                resContentId = R.mipmap.icon_diary_details_bg_3
            })
            list.add(ModuleBean().apply {
                name = "方格本-10mm"
                resId = R.mipmap.icon_diary_module_bg_2
                resContentId = R.mipmap.icon_diary_details_bg_2
            })
            list.add(ModuleBean().apply {
                name ="方格本-8.5mm"
                resId = R.mipmap.icon_diary_module_bg_4
                resContentId = R.mipmap.icon_diary_details_bg_4
            })
            return list
        }

    val freenoteModules: MutableList<ModuleBean>
        get() {
            val list= mutableListOf<ModuleBean>()
            list.add(ModuleBean().apply {
                name = mContext.getString(R.string.note_type_kbb)
                resId = R.drawable.bg_black_stroke_10dp_corner
                resContentId = 0
            })
            list.add(ModuleBean().apply {
                name = mContext.getString(R.string.note_type_hgb)
                resId = R.mipmap.icon_note_module_hg_11
                resContentId = R.mipmap.icon_freenote_bg_1
            })
            return list
        }

    //笔记本内容选择
    val noteModules: MutableList<ModuleBean>
        get() {
            val list= mutableListOf<ModuleBean>()
            list.add(ModuleBean().apply {
                name = mContext.getString(R.string.diary_type_hgb_11)
                resId = R.mipmap.icon_note_module_hg_11
                resContentId = R.mipmap.icon_note_content_hg_11
            })
            list.add(ModuleBean().apply {
                name = mContext.getString(R.string.diary_type_hgb_9)
                resId = R.mipmap.icon_note_module_hg_9
                resContentId = R.mipmap.icon_note_content_hg_9
            })
            list.add(ModuleBean().apply {
                name = mContext.getString(R.string.diary_type_hgb_7)
                resId = R.mipmap.icon_note_module_hg_7
                resContentId = R.mipmap.icon_note_content_hg_7
            })
            list.add(ModuleBean().apply {
                name = mContext.getString(R.string.diary_type_fgb_10)
                resId = R.mipmap.icon_note_module_fg_10
                resContentId = R.mipmap.icon_note_content_fg_10
            })
            list.add(ModuleBean().apply {
                name = mContext.getString(R.string.diary_type_fgb_8_5)
                resId = R.mipmap.icon_note_module_fg_8_5
                resContentId = R.mipmap.icon_note_content_fg_8_5
            })
            list.add(ModuleBean().apply {
                name = mContext.getString(R.string.diary_type_fgb_7)
                resId = R.mipmap.icon_note_module_fg_7
                resContentId = R.mipmap.icon_note_content_fg_7
            })
            list.add(ModuleBean().apply {
                name = mContext.getString(R.string.note_type_yyb_3_5)
                resId = R.mipmap.icon_note_module_yy_3_5
                resContentId = R.mipmap.icon_note_content_yy_3_5
            })
            list.add(ModuleBean().apply {
                name = mContext.getString(R.string.note_type_yyb_3)
                resId = R.mipmap.icon_note_module_yy_3
                resContentId = R.mipmap.icon_note_content_yy_3
            })
            list.add(ModuleBean().apply {
                name = mContext.getString(R.string.note_type_wxp)
                resId = R.mipmap.icon_note_module_wxp
                resContentId = R.mipmap.icon_note_content_wxp
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
            list.add(ItemList(5, "艺术才能"))
            list.add(ItemList(6, "运动健康"))
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

    fun getCourseId(courseStr:String):Int{
        var courseId=0
        for (item in courses){
            if (item.desc==courseStr)
                courseId=item.type
        }
        return courseId
    }

    fun getCourseStr(courseId:Int):String{
        var courseStr=""
        for (item in courses){
            if (item.type==courseId)
                courseStr=item.desc
        }
        return courseStr
    }

    fun getBookVersionStr(version: Int): String {
        var cls=""
        for (item in versions) {
            if (item.type == version){
                cls=item.desc
            }
        }
        return cls
    }


    fun getResultChildItems():MutableList<ResultStandardItem.ResultChildItem>{
        val items= mutableListOf<ResultStandardItem.ResultChildItem>()
        items.add(ResultStandardItem.ResultChildItem().apply {
            sort=1
            sortStr="A"
            score=92.5
            isCheck=false
        })
        items.add(ResultStandardItem.ResultChildItem().apply {
            sort=2
            sortStr="B"
            score=77.5
            isCheck=false
        })
        items.add(ResultStandardItem.ResultChildItem().apply {
            sort=3
            sortStr="C"
            score=62.5
            isCheck=false
        })
        return items
    }

    private fun getResultChildHighItems():MutableList<ResultStandardItem.ResultChildItem>{
        val items= mutableListOf<ResultStandardItem.ResultChildItem>()
        items.add(ResultStandardItem.ResultChildItem().apply {
            sort=1
            sortStr="A+"
            score=97.5
            isCheck=false
        })
        items.add(ResultStandardItem.ResultChildItem().apply {
            sort=2
            sortStr="A "
            score=92.5
            isCheck=false
        })
        items.add(ResultStandardItem.ResultChildItem().apply {
            sort=3
            sortStr="A-"
            score=87.5
            isCheck=false
        })
        items.add(ResultStandardItem.ResultChildItem().apply {
            sort=4
            sortStr="B+"
            score=82.5
            isCheck=false
        })
        items.add(ResultStandardItem.ResultChildItem().apply {
            sort=5
            sortStr="B "
            score=77.5
            isCheck=false
        })
        items.add(ResultStandardItem.ResultChildItem().apply {
            sort=6
            sortStr="B-"
            score=72.5
            isCheck=false
        })
        items.add(ResultStandardItem.ResultChildItem().apply {
            sort=7
            sortStr="C+"
            score=67.5
            isCheck=false
        })
        items.add(ResultStandardItem.ResultChildItem().apply {
            sort=8
            sortStr="C "
            score=62.5
            isCheck=false
        })
        items.add(ResultStandardItem.ResultChildItem().apply {
            sort=9
            sortStr="C-"
            score=57.5
            isCheck=false
        })
        return items
    }

    /**
     * 练字评分
     */
    private fun getResultStandardItem6s(correctModule:Int):MutableList<ResultStandardItem>{
        val items= mutableListOf<ResultStandardItem>()
        items.add(ResultStandardItem().apply {
            title="比例匀称"
            list= if (correctModule==2) getResultChildHighItems() else getResultChildItems()
        })
        items.add(ResultStandardItem().apply {
            title="字迹工整"
            list= if (correctModule==2) getResultChildHighItems() else getResultChildItems()
        })
        items.add(ResultStandardItem().apply {
            title="卷面整洁"
            list= if (correctModule==2) getResultChildHighItems() else getResultChildItems()
        })
        return items
    }

    /**
     * 朗读评分
     */
    private fun getResultStandardItem3s(correctModule:Int):MutableList<ResultStandardItem>{
        val items= mutableListOf<ResultStandardItem>()
        items.add(ResultStandardItem().apply {
            title="语言标准"
            list= if (correctModule==2) getResultChildHighItems() else getResultChildItems()
        })
        items.add(ResultStandardItem().apply {
            title="词汇语法"
            list= if (correctModule==2) getResultChildHighItems() else getResultChildItems()
        })
        items.add(ResultStandardItem().apply {
            title="流畅程度"
            list= if (correctModule==2) getResultChildHighItems() else getResultChildItems()
        })
        return items
    }

    /**
     * 阅读评分
     */
    private fun getResultStandardItem8s(correctModule:Int):MutableList<ResultStandardItem>{
        val items= mutableListOf<ResultStandardItem>()
        items.add(ResultStandardItem().apply {
            title="词句摘抄"
            list= if (correctModule==2) getResultChildHighItems() else getResultChildItems()
        })
        items.add(ResultStandardItem().apply {
            title="阅读感想"
            list= if (correctModule==2) getResultChildHighItems() else getResultChildItems()
        })
        items.add(ResultStandardItem().apply {
            title="卷面整洁"
            list= if (correctModule==2) getResultChildHighItems() else getResultChildItems()
        })
        return items
    }

    /**
     * 作文评分
     */
    private fun getResultStandardItem11s(correctModule:Int):MutableList<ResultStandardItem>{
        val items= mutableListOf<ResultStandardItem>()
        items.add(ResultStandardItem().apply {
            title="思想内容"
            list= if (correctModule==2) getResultChildHighItems() else getResultChildItems()
        })
        items.add(ResultStandardItem().apply {
            title="语言文字"
            list= if (correctModule==2) getResultChildHighItems() else getResultChildItems()
        })
        items.add(ResultStandardItem().apply {
            title="层次结构"
            list= if (correctModule==2) getResultChildHighItems() else getResultChildItems()
        })
        items.add(ResultStandardItem().apply {
            title="卷面书写"
            list= if (correctModule==2) getResultChildHighItems() else getResultChildItems()
        })
        return items
    }

    /**
     * 手写
     */
    private fun getResultStandardItem0s(correctModule:Int):MutableList<ResultStandardItem>{
        val items= mutableListOf<ResultStandardItem>()
        items.add(ResultStandardItem().apply {
            title="标准评分"
            list= if (correctModule==2) getResultChildHighItems() else getResultChildItems()
        })
        return items
    }

    fun getScoreStandardStr(score: Double,questionType:Int):String{
        if (questionType>0){
            return score.toString()
        }
        else {
            return when (score) {
                1.0 -> {
                    "A"
                }
                2.0 -> {
                    "B"
                }
                3.0 -> {
                    "C"
                }
                else -> {
                    score.toString()
                }
            }
        }
    }

    /**
     * 返回标准评分
     * @param score
     * @return
     */
    fun getResultStandardStr(score: Double,questionType:Int): String {
        if (questionType==2){
            return when (score) {
                1.0 -> {
                    "A+"
                }
                2.0 -> {
                    "A "
                }
                3.0 -> {
                    "A-"
                }
                4.0 -> {
                    "B+"
                }
                5.0 -> {
                    "B "
                }
                6.0 -> {
                    "B-"
                }
                7.0 -> {
                    "C+"
                }
                8.0 -> {
                    "C "
                }
                9.0 -> {
                    "C-"
                }
                else->{
                    score.toString()
                }
            }
        }
        else{
            return when (score) {
                1.0 -> {
                    "A"
                }
                2.0 -> {
                    "B"
                }
                3.0 -> {
                    "C"
                }
                else->{
                    score.toString()
                }
            }
        }
    }


    /**
     * 获取评分列表
     */
    fun getResultStandardItems(state:Int, name: String, correctModule:Int):MutableList<ResultStandardItem>{
        return when(state){
            3->{
                getResultStandardItem3s(correctModule)
            }
            6->{
                getResultStandardItem6s(correctModule)
            }
            8->{
                getResultStandardItem8s(correctModule)
            }
            11->{
                getResultStandardItem11s(correctModule)
            }
            else->{
                if (name=="作文作业本"){
                    getResultStandardItem11s(correctModule)
                }
                else{
                    getResultStandardItem0s(correctModule)
                }
            }
        }
    }

    fun operatingGuideInfo():List<MultiItemEntity>{
        val list= mutableListOf<MultiItemEntity>()
        val types= mutableListOf("一、主页面","二、管理中心","三、实用工具")
        val mainStrs= mutableListOf("注册","按键/接口","状态栏按钮","首页","书架","文档","笔记","应用","随笔","日记","规划","截图","消息")
        val managerStrs= mutableListOf("管理中心")
        val toolStrs= mutableListOf("我的工具","我的日历","截屏","几何绘图")
        val childTypes= mutableListOf(mainStrs,managerStrs,toolStrs)
        types.forEachIndexed { index, s ->
            val catalogParentBean = CatalogParentBean()
            catalogParentBean.title=s
            childTypes[index].forEachIndexed { childIndex, childStr ->
                val catalogChildBean = CatalogChildBean()
                catalogChildBean.title = childStr
                catalogChildBean.parentPosition=index
                catalogChildBean.pageNumber = childIndex+1
                catalogParentBean.addSubItem(catalogChildBean)
            }
            list.add(catalogParentBean)
        }
        return list
    }

}