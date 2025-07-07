package com.saputra.wahyuaditya.posyandulansia.fragment.beranda

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.saputra.wahyuaditya.posyandulansia.AntrianPeriksaActivity
import com.saputra.wahyuaditya.posyandulansia.DataJadwalActivity
import com.saputra.wahyuaditya.posyandulansia.DataLansiaActivity
import com.saputra.wahyuaditya.posyandulansia.DetailBeritaActivity
import com.saputra.wahyuaditya.posyandulansia.MainActivity
import com.saputra.wahyuaditya.posyandulansia.R
import com.saputra.wahyuaditya.posyandulansia.adapter.BeritaAdapter
import com.saputra.wahyuaditya.posyandulansia.databinding.ActivityKaderBerandaBinding
import com.saputra.wahyuaditya.posyandulansia.model.Berita
import com.saputra.wahyuaditya.posyandulansia.model.BeritaResponse
import com.saputra.wahyuaditya.posyandulansia.model.JadwalResponse
import com.saputra.wahyuaditya.posyandulansia.network.ApiClient
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.Date
import java.util.Locale
import kotlin.collections.orEmpty


class KaderActivity : Fragment(){
    private lateinit var b: ActivityKaderBerandaBinding
    private lateinit var v: View
    private lateinit var thisParent: MainActivity

    private lateinit var sharedPreferences: SharedPreferences
    private val client = OkHttpClient()
    private lateinit var dialog: Dialog

    private lateinit var beritaAdapter: BeritaAdapter
    private val beritaList = mutableListOf<Berita>()
    private var currentPage = 1
    private var isLoading = false
    private var isLastPage = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Initialize binding and view
        thisParent = activity as MainActivity
        b = ActivityKaderBerandaBinding.inflate(inflater, container, false)
        v = b.root

        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
        val tanggalHariIni = dateFormat.format(Date())

        b.txtTanggal.text = tanggalHariIni

        sharedPreferences = activity?.getSharedPreferences("UserSession", Context.MODE_PRIVATE)!!

        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.progress_dialog)
        dialog.setCancelable(false) // Tidak bisa ditutup manual

        val namaUser = sharedPreferences.getString("namaUser", "") ?: ""
        val namaDesa = sharedPreferences.getString("namaDesa", "") ?: ""
        val formattedNamaDesa = namaDesa.lowercase().split(" ").joinToString(" ") { it.capitalize() }
        // Cek apakah nilai namaUser yang didapat sesuai
        Log.d("SharedPreferences", "Nama User: $namaUser")

        b.txtRole.text = "Hi, Kader ${formattedNamaDesa}"
        b.txtNamaKader.text = namaUser

        b.btnPeriksa.setOnClickListener {
            cekJadwal()
        }

        b.btnDataLansia.setOnClickListener {
            dataLansia()
        }

        b.btnRiwayat.setOnClickListener {
            dataRiwayat()
        }

        setupRecyclerView()
        setupListeners()

        // Ambil data awal

        fetchBeritaData()


        return v
    }

    private fun dataRiwayat(){
        dialog.show()
        // Membuat Intent untuk berpindah ke Activity lain
        val intent = Intent(requireContext(), DataJadwalActivity::class.java)
        // Menjalankan Intent untuk berpindah ke Activity lain
        startActivity(intent)
        dialog.dismiss()
    }

    private fun dataLansia(){
        dialog.show()
        // Membuat Intent untuk berpindah ke Activity lain
        val intent = Intent(requireContext(),  DataLansiaActivity::class.java)
        // Menjalankan Intent untuk berpindah ke Activity lain
        startActivity(intent)
        dialog.dismiss()
    }

    private fun cekJadwal() {

        // Tampilkan loading dialog sebelum proses dimulai
        dialog.show()

        val idDesaUser = sharedPreferences.getString("idDesaUser", "") ?: ""

        val request = Request.Builder()
            .url("https://posyandulansia.supala.biz.id/api/cekJadwal?desa_id=$idDesaUser")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                thisParent.runOnUiThread {
                    dialog.dismiss() // Tutup loading jika gagal
                    Toast.makeText(thisParent, "Gagal terhubung ke server", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()

                // Tambahkan log di sini
                Log.d("CekJadwalResponse", "HTTP ${response.code}: $responseBody")

                dialog.dismiss() // Tutup loading jika gagal

                if (response.isSuccessful && responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    val data = jsonResponse.getJSONArray("data")

                    if (data.length() > 0) {
                        val firstJadwal = data.getJSONObject(0)
                        val intent = Intent(requireContext(), AntrianPeriksaActivity::class.java).apply {
                            putExtra("id", firstJadwal.getInt("id"))
                            putExtra("tanggal", firstJadwal.getString("tanggal"))
                            putExtra("waktu", firstJadwal.getString("waktu"))
                            putExtra("lokasi", firstJadwal.getString("lokasi"))
                            putExtra("keterangan", firstJadwal.optString("keterangan", ""))
                        }
                        startActivity(intent)

                    } else {
                        thisParent.runOnUiThread {
                            Toast.makeText(thisParent, "Tidak ada jadwal untuk hari ini", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    thisParent.runOnUiThread {
                        Toast.makeText(thisParent, "Gagal mendapatkan data jadwal", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    fun fetchUserDataFromServer() {
        val request = Request.Builder()
            .url("https://posyandulansia.supala.biz.id/api/get_user_data") // URL API untuk mendapatkan data pengguna
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                thisParent.runOnUiThread {
                    showToast("Gagal terhubung ke server: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
               thisParent.runOnUiThread {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        if (responseBody != null) {
                            // Mengupdate SharedPreferences dengan data terbaru dari server
//                            updateSharedPreferences(responseBody)
                        }
                    } else {
                        showToast("Gagal mengambil data dari server.")
                    }
                }
            }
        })
    }

    private fun setupRecyclerView() {
        beritaAdapter = BeritaAdapter(beritaList) { berita ->
            val intent = Intent(thisParent, DetailBeritaActivity::class.java).apply {
                putExtra("berita", berita)
            }
            startActivity(intent)
        }

        with(b.rvBerita) {
            layoutManager = LinearLayoutManager(thisParent)
            adapter = beritaAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                    val totalItemCount = layoutManager.itemCount

                    if (!isLoading && !isLastPage && lastVisibleItemPosition >= totalItemCount - 5) {
                        fetchBeritaData(
//                            getValidInput(b.tvTanggal.text.toString()),
//                            getValidInput(b.etLokasi.text.toString())
                        )
                    }
                }
            })
        }
    }

    private fun setupListeners() {
        with(b) {
            swipeRefreshLayout.setOnRefreshListener {
                refreshData()
            }
        }
    }

    private fun fetchBeritaData() {
        if (isLoading) return
        isLoading = true
        b.swipeRefreshLayout.isRefreshing = true

//        Log.d("Berita", "Fetching page $currentPage with filter tanggal='$tanggal', lokasi='$lokasi'")

        ApiClient.instance.getBerita(currentPage)
            .enqueue(object : retrofit2.Callback<BeritaResponse> {
                override fun onResponse(call: retrofit2.Call<BeritaResponse>, response: retrofit2.Response<BeritaResponse>) {
                    isLoading = false
                    b.swipeRefreshLayout.isRefreshing = false

                    if (response.isSuccessful) {
                        val data = response.body()?.data?.data.orEmpty()

                        if (currentPage == 1) beritaList.clear()
                        beritaList.addAll(data)
                        beritaAdapter.notifyDataSetChanged()

                        val lastPage = response.body()?.data?.last_page ?: 1
                        isLastPage = currentPage >= lastPage
                        if (!isLastPage) currentPage++

                        b.tvNoData.visibility = if (beritaList.isEmpty()) View.VISIBLE else View.GONE
                    } else {
//                        Log.e("Berita", "Response not successful: ${response.errorBody()?.string()}")
                        if (currentPage == 1) b.tvNoData.visibility = View.VISIBLE
                    }
                }

                override fun onFailure(call: retrofit2.Call<BeritaResponse>, t: Throwable) {
                    isLoading = false
                    b.swipeRefreshLayout.isRefreshing = false
                    Log.e("Berita", "API call failed: ${t.message}")
                    if (currentPage == 1) b.tvNoData.visibility = View.VISIBLE
                }
            })
    }

    private fun refreshData() {
        currentPage = 1
        isLastPage = false
        b.tvNoData.visibility = View.GONE
        fetchBeritaData()

    }

    private fun showToast(message: String) {
        Toast.makeText(thisParent, message, Toast.LENGTH_SHORT).show()
    }
}