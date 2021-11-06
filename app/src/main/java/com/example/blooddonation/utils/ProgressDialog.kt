package com.example.blooddonation.utils

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import com.example.blooddonation.R

class ProgressDialog {
    companion object {
        fun progressDialog(context: Context): Dialog {
            val dialog = Dialog(context)
            val layout = LayoutInflater.from(context).inflate(R.layout.progress, null)
            dialog.setContentView(layout)
            dialog.setCancelable(false)
            return dialog
        }
    }
}