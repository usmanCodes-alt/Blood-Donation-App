package com.example.blooddonation.models

data class Donation(
    val donorUsername: String = "",
    val donorEmail: String = "",
    val donorPhoneNumber: String = "",
    val donorBloodGroup: String = "",
    val unitsDonated: Int = 0
)