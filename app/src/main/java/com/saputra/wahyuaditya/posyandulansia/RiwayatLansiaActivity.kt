package com.saputra.wahyuaditya.posyandulansia

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.saputra.wahyuaditya.posyandulansia.CekKesehatanActivity
import com.saputra.wahyuaditya.posyandulansia.DataLansiaActivity
import com.saputra.wahyuaditya.posyandulansia.adapter.CekKesehatanAdapter
import com.saputra.wahyuaditya.posyandulansia.adapter.LansiaAdapter
import com.saputra.wahyuaditya.posyandulansia.databinding.ActivityRiwayatLansiaBinding
import com.saputra.wahyuaditya.posyandulansia.databinding.BottomSheetLansiaDetailBinding
import com.saputra.wahyuaditya.posyandulansia.model.CekKesehatanResponse
import com.saputra.wahyuaditya.posyandulansia.model.KehadiranResponse
import com.saputra.wahyuaditya.posyandulansia.model.Lansia
import com.saputra.wahyuaditya.posyandulansia.model.LansiaResponse
import com.saputra.wahyuaditya.posyandulansia.network.ApiClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.IOException
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone


class RiwayatLansiaActivity : AppCompatActivity() {

    private lateinit var b: ActivityRiwayatLansiaBinding
    private lateinit var lansiaAdapter: LansiaAdapter
    private val lansiaList: MutableList<Lansia> = mutableListOf()

    private var currentPage = 1
    private var isLoading = false
    private var isLastPage = false
    private var jadwalId = 0

    private lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityRiwayatLansiaBinding.inflate(layoutInflater)
        setContentView(b.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        dialog = Dialog(this)
        dialog.setContentView(R.layout.progress_dialog)
        dialog.setCancelable(false)

        jadwalId = intent.getIntExtra("jadwal_id", -1)

        // Cek jika valid, lalu panggil API
        if (jadwalId != -1) {
            fetchLansiaData(jadwalId,"")
        }

        setupRecyclerView()
        setupListeners()
    }

    private fun setupRecyclerView() {
        lansiaAdapter = LansiaAdapter(lansiaList) { lansia ->
            // Panggil fungsi saat item diklik, membawa lansia_id dan jadwal_id
            getDataRiwayat(lansia.id, jadwalId)
        }
        with(b.rvLansia) {
            layoutManager = LinearLayoutManager(this@RiwayatLansiaActivity)
            adapter = lansiaAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    if (!isLoading && !isLastPage && layoutManager.findLastVisibleItemPosition() >= layoutManager.itemCount - 5) {
                        fetchLansiaData(jadwalId, b.etSearch.text.toString())
                    }
                }
            })
        }
    }

    private fun setupListeners() {
        with(b) {
            swipeRefreshLayout.setOnRefreshListener {
                swipeRefreshLayout.isRefreshing = true
                refreshData()
            }
            etSearch.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) { refreshData() }
            })
        }
    }

    private fun fetchLansiaData(jadwal_id: Int, keyword: String) {
        if (isLoading) return
        isLoading = true
        b.swipeRefreshLayout.isRefreshing = true

        ApiClient.instance.getKehadiranByJadwal(jadwal_id, keyword, currentPage).enqueue(object : Callback<KehadiranResponse> {
            override fun onResponse(call: Call<KehadiranResponse>, response: Response<KehadiranResponse>) {
                isLoading = false
                b.swipeRefreshLayout.isRefreshing = false

                if (response.isSuccessful) {
                    val responseData = response.body()
                    val paginatedData = responseData?.data

                    if (paginatedData != null) {
                        val lansiaOnly = response.body()?.data?.data?.mapNotNull { it.lansia }.orEmpty()
                        if (currentPage == 1) lansiaList.clear()
                        lansiaList.addAll(lansiaOnly)

                        lansiaAdapter.notifyDataSetChanged()

                        isLastPage = currentPage >= paginatedData.last_page
                        if (!isLastPage) currentPage++

                        b.tvNoData.visibility = if (lansiaList.isEmpty()) View.VISIBLE else View.GONE
                    } else {
                        Log.e("API", "Paginated data is null")
                        b.tvNoData.visibility = View.VISIBLE
                    }
                } else {
                    Log.e("API", "Response not successful: ${response.code()} - ${response.message()}")
                    b.tvNoData.visibility = View.VISIBLE
                }
            }


            override fun onFailure(call: Call<KehadiranResponse>, t: Throwable) {
                isLoading = false
                b.swipeRefreshLayout.isRefreshing = false
                if (currentPage == 1) b.tvNoData.visibility = View.VISIBLE
            }
        })
    }

    private fun refreshData() {
        currentPage = 1
        isLastPage = false
        b.tvNoData.visibility = View.GONE
        fetchLansiaData(jadwalId, b.etSearch.text.toString())
    }

    private fun getDataRiwayat(lansiaId: Int, idJadwal: Int) {
        dialog.show()

        // Ganti dengan URL sesuai format query string untuk GET request
        val url = "https://posyandulansia.supala.biz.id/api/cek-kesehatan?lansia_id=${lansiaId}&jadwal_id=${idJadwal}"
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(url)
            .get() // Gunakan metode GET
            .header("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                runOnUiThread {
                    dialog.dismiss()
                    Toast.makeText(this@RiwayatLansiaActivity, "Gagal mengambil data!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                runOnUiThread {
                    dialog.dismiss()

                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        responseBody?.let { jsonResponse ->
                            val jsonObject = JSONObject(jsonResponse)
                            val dataArray = jsonObject.getJSONArray("data")

                            if (dataArray.length() > 0) {
                                val data = dataArray.getJSONObject(0)
                                val diagnosa = JSONArray(data.getString("diagnosa"))
                                val kesimpulan = data.getJSONArray("kesimpulan")
                                val solusi = data.getJSONArray("solusi")

                                val tanggal = data.getString("created_at")

                                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
                                inputFormat.timeZone = TimeZone.getTimeZone("UTC")

                                val outputFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
                                val formattedDate = try {
                                    val date = inputFormat.parse(tanggal)
                                    outputFormat.format(date!!)
                                } catch (e: Exception) {
                                    "-"
                                }

                                val tanggalCek = formattedDate


                                showResultDialog(data, diagnosa, kesimpulan, solusi, tanggalCek)
                            } else {
                                Toast.makeText(this@RiwayatLansiaActivity, "Data tidak ditemukan.", Toast.LENGTH_SHORT).show()
                            }

                        }
                    } else {
                        Toast.makeText(this@RiwayatLansiaActivity, "Terjadi kesalahan pada server!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }


    private fun showResultDialog(data: JSONObject, diagnosa: JSONArray, kesimpulan: JSONArray, solusi: JSONArray, tanggalCek: String) {
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialog_hasil_cek, null)

        dialogView.findViewById<TextView>(R.id.titleTanggal).text = tanggalCek

        // Isi nilai hasil cek
        dialogView.findViewById<TextView>(R.id.txtHasilBB).text = "${data.optDouble("berat_badan", 0.0)} kg"
        dialogView.findViewById<TextView>(R.id.txtHasilTD).text = "${data.optInt("tekanan_darah_sistolik", 0)}/${data.optInt("tekanan_darah_diastolik", 0)} mmHg"
        dialogView.findViewById<TextView>(R.id.txtHasilGulaDarah).text = "${data.optInt("gula_darah", 0)} mg/dL"
        dialogView.findViewById<TextView>(R.id.txtHasilKolestrol).text = "${data.optInt("kolestrol", 0)} mg/dL"
        dialogView.findViewById<TextView>(R.id.txtHasilAsamUrat).text = "${data.optDouble("asam_urat", 0.0)} mg/dL"

        // Diagnosa (jika format string array)
        val diagnosaList = mutableListOf<String>()
        for (i in 0 until diagnosa.length()) {
            diagnosaList.add("- ${diagnosa.getString(i)}")
        }
        dialogView.findViewById<TextView>(R.id.txtDiagnosaBB).text = diagnosaList.joinToString("\n")

        // Kesimpulan (jika format string array)
        val kesimpulanList = mutableListOf<String>()
        for (i in 0 until kesimpulan.length()) {
            kesimpulanList.add("• ${kesimpulan.getString(i)}")
        }
        dialogView.findViewById<TextView>(R.id.txtKualitasKesehatan).text = kesimpulanList.joinToString("\n")

        // solusi (jika format string array)
        val solusiList = mutableListOf<String>()
        for (i in 0 until solusi.length()) {
            solusiList.add("• ${solusi.getString(i)}")
        }
        dialogView.findViewById<TextView>(R.id.txtSolusiKesehatan).text = solusiList.joinToString("\n")

        // Tampilkan dialog
        runOnUiThread {
            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("OK") { d, _ ->
                    d.dismiss()
                }
                .create()

            dialog.show()
        }
    }
}