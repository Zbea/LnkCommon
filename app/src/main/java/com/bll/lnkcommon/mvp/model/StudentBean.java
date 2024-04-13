package com.bll.lnkcommon.mvp.model;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class StudentBean implements Serializable {
    public String account;
    public int accountId;
    public String nickname;
    public int grade;
    public String schoolName;
    public boolean isCheck;
    public boolean isAllowMoney;
    public boolean isAllowBook;
    public List<PermissionTimeBean> bookList;
    public boolean isAllowVideo;
    public List<PermissionTimeBean>  videoList;

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj==null)
            return false;
        if (!(obj instanceof StudentBean))
            return false;
        if (this==obj)
            return true;
        StudentBean item=(StudentBean) obj;
        return Objects.equals(this.account, item.account)&&this.accountId==item.accountId && Objects.equals(this.nickname, item.nickname)
                &&this.grade==item.grade;
    }

}
