package com.example.battlerunner.data.model

data class User(
    val id: String,
    val password: String,
    val name: String
)

data class LoginInfo(
    val user_id: String,
    val password: String,
    val name: String
)