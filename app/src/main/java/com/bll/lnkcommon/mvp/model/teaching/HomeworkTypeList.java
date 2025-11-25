package com.bll.lnkcommon.mvp.model.teaching;

import java.util.List;

public class HomeworkTypeList {

    public int total;
    public List<HomeworkTypeBean> list;

    public class HomeworkTypeBean{
        public int id;
        public String name;
        public int subject;
        public int subjectId;
        public int childId;
        public int type;
        public int bookId;
        public String imageUrl;
    }
}
