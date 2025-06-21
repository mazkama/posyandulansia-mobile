package com.saputra.wahyuaditya.posyandulansia.model

data class JadwalResponse(
    val status: Boolean,
    val message: String,
    val data: JadwalData
)
data class JadwalData(
    val current_page: Int,
    val data: List<Jadwal>,
    val last_page: Int,
    val next_page_url: String?,
    val prev_page_url: String?,
    val total: Int
)
data class Jadwal(
    val id: Int,
    val tanggal: String,
    val lokasi: String,
    val waktu: String,
    val status: String,
    val created_at: String
)