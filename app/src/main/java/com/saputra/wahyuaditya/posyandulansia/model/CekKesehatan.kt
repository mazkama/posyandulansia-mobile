package com.saputra.wahyuaditya.posyandulansia.model

import com.google.gson.annotations.SerializedName

data class CekKesehatan(
    val id: Int,
    @SerializedName("lansia_id") val lansiaId: Int,
    @SerializedName("jadwal_id") val jadwalId: Int,
    val tanggal: String,
    @SerializedName("berat_badan") val beratBadan: String,
    @SerializedName("tekanan_darah_sistolik") val tekananDarahSistolik: Int,
    @SerializedName("tekanan_darah_diastolik") val tekananDarahDiastolik: Int,
    @SerializedName("gula_darah") val gulaDarah: String,
    val kolestrol: String,
    @SerializedName("asam_urat") val asamUrat: String,
    val diagnosa: String
)

data class CekKesehatanResponse(
    val message: String,
    val data: List<CekKesehatan>
)
