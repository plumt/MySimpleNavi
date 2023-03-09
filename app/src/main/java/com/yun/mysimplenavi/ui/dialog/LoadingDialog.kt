package com.yun.mysimplenavi.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.yun.mysimplenavi.R

class LoadingDialog constructor(context: Context) : Dialog(context) {
    init {
        setCanceledOnTouchOutside(false)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_loading)
        setCancelable(false)
    }
}