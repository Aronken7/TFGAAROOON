package com.example.tfg_aaron.data.network.dto

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val nombre: String,
    val email: String,
    val password: String,
    val nombreEquipo: String
)

data class AuthResponse(
    val token: String,
    val entrenadorId: Long,
    val nombre: String,
    val email: String,
    val nombreEquipo: String?
)
