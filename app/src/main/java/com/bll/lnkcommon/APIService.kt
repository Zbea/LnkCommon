package com.bll.lnkcommon

import com.bll.lnkcommon.mvp.model.*
import com.bll.lnkcommon.net.BaseResult
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.*


interface APIService{

    /**
     * 获取下载token
     */
    @POST("file/token")
    fun getQiniuToken(): Observable<BaseResult<String>>
    /**
     * 公共年级接口
     */
    @GET("userTypes")
    fun getCommonGrade(): Observable<BaseResult<CommonData>>
    /**
     * 获取学校列表
     */
    @GET("school/list")
    fun getCommonSchool(): Observable<BaseResult<MutableList<SchoolBean>>>
    /**
     * 用户登录 "/login"
     */
    @POST("login")
    fun login(@Body requestBody: RequestBody): Observable<BaseResult<User>>
    /**
     * 用户个人信息 "/accounts"
     */
    @GET("accounts")
    fun accounts(): Observable<BaseResult<User>>
    /**
     * 修改姓名 "/accounts/nickname"
     */
    @PATCH("accounts/nickname")
    fun editName(@Body requestBody: RequestBody): Observable<BaseResult<Any>>
    /**
     * 绑定学生
     */
    @POST("parent/child/bind")
    fun onBindStudent(@Body requestBody: RequestBody): Observable<BaseResult<Any>>
    /**
     * 绑定学生
     */
    @POST("parent/child/unbind")
    fun onUnbindStudent(@Body requestBody: RequestBody): Observable<BaseResult<Any>>
    /**
     * 获取账户绑定学生列表
     */
    @GET("parent/child/list")
    fun onStudentList(): Observable<BaseResult<MutableList<StudentBean>>>

    /**
     * 绑定好友
     */
    @POST("add/friend/insert")
    fun onBindFriend(@Body requestBody: RequestBody): Observable<BaseResult<Any>>
    /**
     * 解绑好友
     */
    @POST("friend/delete")
    fun onUnbindFriend(@Body requestBody: RequestBody): Observable<BaseResult<Any>>
    /**
     * 获取好友列表
     */
    @GET("friend/list")
    fun onFriendList(): Observable<BaseResult<FriendList>>

    /**
     * 同意好友请求
     */
    @POST("add/friend/consent")
    fun onAgreeFriend(@Body requestBody: RequestBody): Observable<BaseResult<Any>>
    /**
     * 不同意好友
     */
    @POST("add/friend/delete")
    fun onUnAgreeFriend(@Body requestBody: RequestBody): Observable<BaseResult<Any>>
    /**
     * 请求列表
     */
    @GET("add/friend/list")
    fun onRequestFriendList(): Observable<BaseResult<FriendList>>

    /**
     * 短信信息 "/sms"
     */
    @GET("sms")
    fun getSms(@Query("telNumber") num:String): Observable<BaseResult<Any>>

    /**
     * 注册
     */
    @POST("user/createParent")
    fun register(@Body requestBody: RequestBody): Observable<BaseResult<Any>>
    /**
     * 忘记密码 "/password"
     */
    @POST("password")
    fun findPassword(@Body requestBody: RequestBody): Observable<BaseResult<Any>>
    /**
     * 修改密码 "/accounts/password"
     */
    @PATCH("accounts/password")
    fun editPassword(@Body requestBody: RequestBody): Observable<BaseResult<Any>>

    /**
     * //获取学豆列表
     */
    @GET("wallets/list")
    fun getSMoneyList(@QueryMap map: HashMap<String,String>): Observable<BaseResult<AccountXDList>>
    /**
     * 提交学豆订单
     */
    @POST("wallets/order/{id}")
    fun postOrder(@Path("id") id:String ): Observable<BaseResult<AccountOrder>>
    /**
     * 查看订单状态
     */
    @GET("wallets/order/{id}")
    fun getOrderStatus(@Path("id") id:String): Observable<BaseResult<AccountOrder>>

    /**
     * 教材分类
     */
    @GET("book/types")
    fun getBookType(): Observable<BaseResult<BookStoreType>>
    /**
     * 教材列表
     */
    @GET("textbook/list")
    fun getTextBooks(@QueryMap map: HashMap<String,Any>): Observable<BaseResult<BookStore>>
    /**
     * 教材参考列表
     */
    @GET("book/list")
    fun getHomeworkBooks(@QueryMap map: HashMap<String,Any>): Observable<BaseResult<BookStore>>
    /**
     * 教材参考列表
     */
    @GET("book/lib/list")
    fun getTeachingBooks(@QueryMap map: HashMap<String,Any>): Observable<BaseResult<BookStore>>
    /**
     * 书城列表
     */
    @GET("book/plus/list")
    fun getBooks(@QueryMap map: HashMap<String,Any>): Observable<BaseResult<BookStore>>

    /**
     * 购买书籍
     */
    @POST("buy/book/createOrder")
    fun buyBooks(@Body requestBody: RequestBody): Observable<BaseResult<Any>>

    /**
     * 应用列表
     */
    @GET("application/list")
    fun getApks(@QueryMap map: HashMap<String,Any>): Observable<BaseResult<AppList>>
    /**
     * 应用列表
     */
    @GET("application/types")
    fun getApkTypes(): Observable<BaseResult<CommonData>>
    /**
     * 购买
     */
    @POST("buy/book/createOrder")
    fun onBuy(@Body requestBody: RequestBody): Observable<BaseResult<Any>>

    /**
     * 台历列表
     */
    @GET("calendar/list")
    fun getCalenderList(@QueryMap map: HashMap<String,Any>): Observable<BaseResult<CalenderList>>

    /**
     * 作业列表
     */
    @GET("homework/inform/list")
    fun getHomeworks(@QueryMap map: HashMap<String,Any>): Observable<BaseResult<TeacherHomeworkList>>
    /**
     * 删除
     */
    @POST("homework/inform/delete")
    fun deleteHomeworks(@Body requestBody: RequestBody): Observable<BaseResult<Any>>
    /**
     * 成绩
     */
    @GET("task/group/oneByStudentTaskId")
    fun getScore(@QueryMap map: HashMap<String,Any>): Observable<BaseResult<MutableList<Score>>>


    /**
     * 作业列表
     */
    @GET("parent/homework/list")
    fun getHomeworkTypes(@QueryMap map: HashMap<String,Any>): Observable<BaseResult<HomeworkTypeList>>
    /**
     * 添加作业列表
     */
    @POST("parent/homework/insert")
    fun createHomeworkType(@Body requestBody: RequestBody): Observable<BaseResult<Any>>
    /**
     * 添加作业列表
     */
    @POST("parent/homework/delete")
    fun deleteHomeworkType(@Body requestBody: RequestBody): Observable<BaseResult<Any>>
    /**
     * 布置作业
     */
    @POST("student/job/sendJob")
    fun sendHomework(@Body requestBody: RequestBody): Observable<BaseResult<Any>>
    /**
     * 批改列表
     */
    @GET("student/job/list")
    fun getHomeworkCorrects(@QueryMap map: HashMap<String,Any>): Observable<BaseResult<HomeworkCorrectList>>
    /**
     * 删除批改
     */
    @POST("student/job/delete")
    fun deleteCorrect(@Body requestBody: RequestBody): Observable<BaseResult<Any>>
    /**
     * 提交批改
     */
    @POST("student/job/parentChange")
    fun commitPaperStudent(@Body requestBody: RequestBody): Observable<BaseResult<Any>>


    /**
     * 获取分享列表
     */
    @GET("friend/message/list")
    fun getShareList(@QueryMap map: HashMap<String,Any>): Observable<BaseResult<ShareNoteList>>
    /**
     * 删除列表
     */
    @POST("friend/message/delete")
    fun deleteShare(@Body requestBody: RequestBody): Observable<BaseResult<Any>>
    /**
     * 分享随笔
     */
    @POST("friend/message/send")
    fun shareFreeNote(@Body requestBody: RequestBody): Observable<BaseResult<Any>>
    /**
     * 壁纸
     */
    @GET("font/draw/list")
    fun getWallpaperList(@QueryMap map: HashMap<String,Any>): Observable<BaseResult<WallpaperList>>

    /**
     * 期刊列表
     */
    @GET("magazine/list")
    fun getJournalList(@QueryMap map: HashMap<String,Any>): Observable<BaseResult<JournalList>>
}