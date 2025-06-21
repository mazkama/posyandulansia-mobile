package com.saputra.wahyuaditya.posyandulansia.model

data class Notifikasi(
    val id: Int,
    val pesan: String,
    val tanggal_kirim: String
)

data class NotifikasiData(
    val current_page: Int,
    val data: List<Notifikasi>,
    val last_page: Int,
    val next_page_url: String?,
    val prev_page_url: String?,
    val total: Int
)

data class NotifikasiResponse(
    val message: String,
    val data: NotifikasiData
)

