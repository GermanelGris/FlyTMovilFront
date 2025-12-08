package com.example.registroflytransportation.model

import java.util.Date

data class Users (
    val name: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val phoneNumber: String = "",
    val birthday: Date? = null,
    val photoUri: String = "",
    val roles: List<String> = listOf("CLIENTE")
)