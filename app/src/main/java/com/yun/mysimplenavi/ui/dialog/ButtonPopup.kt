package com.yun.mysimplenavi.ui.dialog

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.Window
import com.yun.mysimplenavi.R
import com.yun.mysimplenavi.BR
import com.yun.mysimplenavi.databinding.DialogButtonBinding

class ButtonPopup {
    lateinit var myDialogListener: DialogListener

    interface DialogListener {
        fun onResultClicked(result: Boolean)
    }

    fun setDialogListener(customDialogListener: DialogListener) {
        this.myDialogListener = customDialogListener
    }

    fun showPopup(
        context: Context,
        title: String,
        message: String
    ) {
        AlertDialog.Builder(context).run {
            setCancelable(false)
            val view = View.inflate(context, R.layout.dialog_button, null)
            val binding = DialogButtonBinding.bind(view)
            binding.apply {
                setVariable(BR.title, title)
                setVariable(BR.message, message)
            }
            setView(binding.root)
            val dialog = create()
            dialog.apply {
                window?.requestFeature(Window.FEATURE_NO_TITLE)
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//                setOnDismissListener {
//                    myDialogListener.onResultClicked(false)
//                }
            }
            binding.run {

                btnCancel.setOnClickListener {
                    myDialogListener.onResultClicked(false)
                    dialog.dismiss()
                }
                btnResult.setOnClickListener {
                    myDialogListener.onResultClicked(true)
                    dialog.dismiss()
                }
            }
            dialog
        }.show()
    }
}