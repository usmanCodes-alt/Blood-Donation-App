package com.example.blooddonation.utils

import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.lang.IndexOutOfBoundsException

class WrapDonorCardLinearLayoutManager(private val context: Context) :
    LinearLayoutManager(context) {

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: IndexOutOfBoundsException) {
            Log.e("TAG", "meet a I-O-O-B-E in RecyclerView");
        }
    }
}