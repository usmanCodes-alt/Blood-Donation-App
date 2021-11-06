package com.example.blooddonation.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.blooddonation.models.Donation

class DonationViewModel : ViewModel() {

    /**
     * Mutable means it can be changed
     * LiveData means that when this data is changed,
     * a listener is run.
     * hence MutableLiveData
     *
     * when we use 'by lazy' we mean lazy initialization,
     * what that means is that the object will be created
     * when it is needed to be used anywhere and not before that.
     */
    val donationModel: MutableLiveData<Donation> by lazy {
        MutableLiveData<Donation>()
    }

}