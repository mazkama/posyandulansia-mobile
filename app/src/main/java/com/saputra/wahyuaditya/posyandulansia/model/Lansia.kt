package com.saputra.wahyuaditya.posyandulansia.model

data class LansiaResponse(
    val status: String,
    val message: String,
    val total_lansia: Int? = 0,
    val total_lansia_cek_kesehatan: Int? = 0,
    val data: LansiaData
)

data class LansiaData(
    val current_page: Int,
    val data: List<Lansia>,
    val last_page: Int,
    val next_page_url: String?,
    val prev_page_url: String?,
    val total: Int
)

data class Lansia(
    val id: Int,
    val user_id: Int,
    val nama: String,
    val nik: String,
    val ttl: String,
    val umur: Int,
    val alamat: String,
    val no_hp: String
)
