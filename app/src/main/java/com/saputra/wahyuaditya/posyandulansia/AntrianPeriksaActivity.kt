package com.saputra.wahyuaditya.posyandulansia

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.saputra.wahyuaditya.posyandulansia.adapter.LansiaAdapter
import com.saputra.wahyuaditya.posyandulansia.databinding.ActivityAntrianPeriksaBinding
import com.saputra.wahyuaditya.posyandulansia.model.Lansia
import com.saputra.wahyuaditya.posyandulansia.model.LansiaResponse
import com.saputra.wahyuaditya.posyandulansia.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AntrianPeriksaActivity : AppCompatActivity() {

    private lateinit var b: ActivityAntrianPeriksaBinding
    private lateinit var lansiaAdapter: LansiaAdapter
    private val lansiaList = mutableListOf<Lansia>()
    private var currentPage = 1
    private var isLoading = false
    private var isLastPage = false
    private lateinit var sharedPreferences: SharedPreferences

    private var idJadwal: Int = -1
    private var desaId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityAntrianPeriksaBinding.inflate(layoutInflater)
        setContentView(b.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)

        // Ambil idJadwal dari intent dan desaId dari sharedPreferences
        idJadwal = intent.getIntExtra("id", -1)
        desaId = sharedPreferences.getString("idDesaUser", "")?.toIntOrNull() ?: -1

        val tanggal = intent.getStringExtra("tanggal")
        val waktu = intent.getStringExtra("waktu")
        val lokasi = intent.getStringExtra("lokasi")
        val keterangan = intent.getStringExtra("keterangan")

        android.util.Log.d("IntentAntrianPeriksa", "id: $idJadwal, tanggal: $tanggal, waktu: $waktu, lokasi: $lokasi, keterangan: $keterangan")

        val namaDesa = sharedPreferences.getString("namaDesa", "") ?: ""
        val formattedNamaDesa = namaDesa.lowercase().split(" ").joinToString(" ") { it.capitalize() }
        b.tvHead.text = "Pemeriksaan di $formattedNamaDesa"

        setupRecyclerView()
        setupListeners()
        refreshData()

        b.buttonBack.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        lansiaAdapter = LansiaAdapter(lansiaList) { lansia ->
            val intent = Intent(this, CekKesehatanActivity::class.java)
            intent.putExtra("lansiaId", lansia.id)
            intent.putExtra("idJadwal", idJadwal)
            intent.putExtra("nama", lansia.nama)
            intent.putExtra("nik", lansia.nik)
            startActivity(intent)
        }
        with(b.rvLansia) {
            layoutManager = LinearLayoutManager(this@AntrianPeriksaActivity)
            adapter = lansiaAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    if (!isLoading && !isLastPage && layoutManager.findLastVisibleItemPosition() >= layoutManager.itemCount - 5) {
                        fetchLansiaData(b.etSearch.text.toString())
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

    private fun fetchLansiaData(keyword: String) {
        if (isLoading) return
        isLoading = true
        b.swipeRefreshLayout.isRefreshing = true

        // Panggil endpoint baru
        ApiClient.instance.getLansiaByJadwalDesa(idJadwal, desaId, keyword).enqueue(object : Callback<LansiaResponse> {
            override fun onResponse(call: Call<LansiaResponse>, response: Response<LansiaResponse>) {
                isLoading = false
                b.swipeRefreshLayout.isRefreshing = false

                val body = response.body()
                val data = body?.data?.data.orEmpty()
                if (currentPage == 1) lansiaList.clear()
                lansiaList.addAll(data)
                lansiaAdapter.notifyDataSetChanged()

                isLastPage = currentPage >= (body?.data?.last_page ?: 0)
                if (!isLastPage) currentPage++

                b.tvNoData.visibility = if (lansiaList.isEmpty()) View.VISIBLE else View.GONE

                // Update cardStats dari response root
                b.txtTotalSelesai.text = (body?.total_lansia ?: 0).toString()
                b.txtTotalSudahCek.text = (body?.total_lansia_cek_kesehatan ?: 0).toString()
            }

            override fun onFailure(call: Call<LansiaResponse>, t: Throwable) {
                isLoading = false
                b.swipeRefreshLayout.isRefreshing = false
                if (currentPage == 1) b.tvNoData.visibility = View.VISIBLE
            }
        })
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun refreshData() {
        currentPage = 1
        isLastPage = false
        b.tvNoData.visibility = View.GONE
        fetchLansiaData(b.etSearch.text.toString())
    }
}
