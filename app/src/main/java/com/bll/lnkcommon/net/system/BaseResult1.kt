package com.bll.lnkcommon.net.system

import java.io.Serializable


class BaseResult1<T> : Serializable {
    var Error: String=""
    var Code = 0
    var Data: T? = null
}