package com.example.blooddonation.models

data class Users(
    val username: String,
    val bloodGroup: String,
    val phoneNumber: String,
    val email: String,
    val password: String
)