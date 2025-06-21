package com.saputra.wahyuaditya.posyandulansia.model

import java.io.Serializable

data class BeritaResponse(
    val status: Boolean,
    val message: String,
    val data: BeritaData
)
data class BeritaData(
    val current_page: Int,
    val data: List<Berita>,
    val last_page: Int,
    val next_page_url: String?,
    val prev_page_url: String?,
    val total: Int
)

data class Berita(
    val id: Int,
    val judul: String,
    val konten: String,
    val tanggal_publish: String,
    val foto: String,
    val created_at: String,
    val updated_at: String
) : Serializable

