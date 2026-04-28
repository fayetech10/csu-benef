package com.example.sencsu.domain.repository

import android.content.Context
import android.net.Uri

interface IFileRepository {
    suspend fun uploadImage(context: Context, uri: Uri): Result<String>
}
