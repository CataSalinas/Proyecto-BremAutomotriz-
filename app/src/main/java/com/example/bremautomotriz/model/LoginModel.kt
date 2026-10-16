package com.example.bremautomotriz.model

data class LoginModel(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val errorGeneral: String? = null,
    val cargando: Boolean = false
)