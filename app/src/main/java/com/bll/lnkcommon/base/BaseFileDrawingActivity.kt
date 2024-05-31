package com.bll.lnkcommon.base

import kotlinx.android.synthetic.main.ac_drawing_file.*


abstract class BaseFileDrawingActivity : BaseDrawingActivity() {

    override fun onInStanceElik() {
        if (v_content!=null){
            elik = v_content?.pwInterFace
        }
    }

}


