package com.saputra.wahyuaditya.posyandulansia.network

import com.saputra.wahyuaditya.posyandulansia.model.BeritaResponse
import com.saputra.wahyuaditya.posyandulansia.model.CekKesehatanResponse
import com.saputra.wahyuaditya.posyandulansia.model.JadwalResponse
import com.saputra.wahyuaditya.posyandulansia.model.KehadiranResponse
import com.saputra.wahyuaditya.posyandulansia.model.LansiaResponse
import com.saputra.wahyuaditya.posyandulansia.model.NotifikasiResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("lansiaSearch")
    fun getLansias(
        @Query("keyword") keyword: String,
        @Query("page") page: Int,
        @Query("desa_id") desaId: String // parameter baru
    ): Call<LansiaResponse>

    @GET("cek-kesehatan/{id}")
    fun getCekKesehatan(@Path("id") lansiaId: Int): Call<CekKesehatanResponse>

    @GET("getNotifikasi")
    fun getNotifikasi(
        @Query("page") page: Int,
        @Query("desa_id") desaId: String // parameter baru
    ): Call<NotifikasiResponse>

    @GET("jadwal")
    fun getJadwal(
        @Query("page") page: Int,
        @Query("tanggal") tanggal: String?,
        @Query("lokasi") lokasi: String?,
        @Query("desa_id") desaId: String // parameter baru
    ): Call<JadwalResponse>

    @GET("kehadiran/jadwal/{jadwal_id}")
    fun getKehadiranByJadwal(
        @Path("jadwal_id") jadwalId: Int,
        @Query("keyword") keyword: String?,
        @Query("page") page: Int
    ): Call<KehadiranResponse>

    @GET("berita")
    fun getBerita(
        @Query("page") page: Int,
    ): Call<BeritaResponse>

    @GET("lansia-by-jadwal-desa/{jadwal_id}/{desa_id}")
    fun getLansiaByJadwalDesa(
        @Path("jadwal_id") jadwalId: Int,
        @Path("desa_id") desaId: Int,
        @Query("keyword") keyword: String?
    ): Call<LansiaResponse>


}
