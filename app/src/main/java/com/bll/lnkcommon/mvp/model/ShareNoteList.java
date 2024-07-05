package com.bll.lnkcommon.mvp.model;

import java.util.List;

public class ShareNoteList {
    public int total;
    public List<ShareNoteBean> list;


    public static class ShareNoteBean{
        public int id;
        public String title;
        public long date;
        public String paths;
        public String bgRes;
        public String nickname;
        public long time;
        public String createTime;
    }

}
