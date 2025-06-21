package com.saputra.wahyuaditya.posyandulansia.model

data class KehadiranResponse(
    val message: String,
    val data: KehadiranPaginatedData
)

data class KehadiranPaginatedData(
    val current_page: Int,
    val data: List<Kehadiran>,
    val last_page: Int,
    val per_page: Int,
    val total: Int
)

data class Kehadiran(
    val id: Int,
    val lansia_id: Int,
    val jadwal_id: Int,
    val status: String,
    val lansia: Lansia?
)
