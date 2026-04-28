package com.example.sencsu.domain.repository

import com.example.sencsu.data.remote.dto.LoginResponse

interface IAuthRepository {
    suspend fun login(email: String, password: String): LoginResponse
    suspend fun adherentLogin(matricule: String, password: String): LoginResponse
}
