package com.saputra.wahyuaditya.posyandulansia

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.saputra.wahyuaditya.posyandulansia.adapter.JadwalAdapter
import com.saputra.wahyuaditya.posyandulansia.databinding.ActivityDataJadwalBinding
import com.saputra.wahyuaditya.posyandulansia.model.Jadwal
import com.saputra.wahyuaditya.posyandulansia.model.JadwalResponse
import com.saputra.wahyuaditya.posyandulansia.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DataJadwalActivity : AppCompatActivity() {

    private lateinit var b: ActivityDataJadwalBinding
    private lateinit var jadwalAdapter: JadwalAdapter
    private val jadwalList = mutableListOf<Jadwal>()
    private var currentPage = 1
    private var isLoading = false
    private var isLastPage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityDataJadwalBinding.inflate(layoutInflater)
        setContentView(b.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Menambahkan listener untuk membuka DatePickerDialog
        b.tvTanggal.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                    val selectedDate = Calendar.getInstance().apply {
                        set(selectedYear, selectedMonth, selectedDay)
                    }
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val dateString = dateFormat.format(selectedDate.time)
                    b.tvTanggal.text = dateString

                    // Tampilkan ikon clear
                    b.ivClearTanggal.visibility = View.VISIBLE
                    b.ivTanggal.visibility = View.GONE
                },
                year, month, day
            )
            datePickerDialog.show()
        }

        b.ivClearTanggal.setOnClickListener {
            b.tvTanggal.text = "Pilih Tanggal"
            it.visibility = View.GONE
            b.ivTanggal.visibility = View.VISIBLE
        }

        setupRecyclerView()
        setupListeners()

        // Ambil data awal
        fetchJadwalData("", "")

        b.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        jadwalAdapter = JadwalAdapter(jadwalList) { jadwal ->
            val intent = Intent(this, RiwayatLansiaActivity::class.java).apply {
                putExtra("jadwal_id", jadwal.id)
            }
            startActivity(intent)
        }

        with(b.rvJadwal) {
            layoutManager = LinearLayoutManager(this@DataJadwalActivity)
            adapter = jadwalAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                    val totalItemCount = layoutManager.itemCount

                    if (!isLoading && !isLastPage && lastVisibleItemPosition >= totalItemCount - 5) {
                        fetchJadwalData(
                            getValidInput(b.tvTanggal.text.toString()),
                            getValidInput(b.etLokasi.text.toString())
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

            tvTanggal.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) = refreshData()
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            etLokasi.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) = refreshData()
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }
    }

    private fun fetchJadwalData(tanggal: String, lokasi: String) {
        if (isLoading) return
        isLoading = true
        b.swipeRefreshLayout.isRefreshing = true

        Log.d("Jadwal", "Fetching page $currentPage with filter tanggal='$tanggal', lokasi='$lokasi'")

        ApiClient.instance.getJadwal(currentPage, tanggal, lokasi)
            .enqueue(object : Callback<JadwalResponse> {
                override fun onResponse(call: Call<JadwalResponse>, response: Response<JadwalResponse>) {
                    isLoading = false
                    b.swipeRefreshLayout.isRefreshing = false

                    if (response.isSuccessful) {
                        val data = response.body()?.data?.data.orEmpty()

                        Log.d("Jadwal", "Page $currentPage loaded: ${data.size} items")

                        if (currentPage == 1) jadwalList.clear()
                        jadwalList.addAll(data)
                        jadwalAdapter.notifyDataSetChanged()

                        val lastPage = response.body()?.data?.last_page ?: 1
                        isLastPage = currentPage >= lastPage
                        if (!isLastPage) currentPage++

                        b.tvNoData.visibility = if (jadwalList.isEmpty()) View.VISIBLE else View.GONE
                    } else {
                        Log.e("Jadwal", "Response not successful: ${response.errorBody()?.string()}")
                        if (currentPage == 1) b.tvNoData.visibility = View.VISIBLE
                    }
                }

                override fun onFailure(call: Call<JadwalResponse>, t: Throwable) {
                    isLoading = false
                    b.swipeRefreshLayout.isRefreshing = false
                    Log.e("Jadwal", "API call failed: ${t.message}")
                    if (currentPage == 1) b.tvNoData.visibility = View.VISIBLE
                }
            })
    }

    private fun refreshData() {
        currentPage = 1
        isLastPage = false
        b.tvNoData.visibility = View.GONE

        val tanggal = if (b.tvTanggal.text.toString() == "Pilih Tanggal") "" else getValidInput(b.tvTanggal.text.toString())
        val lokasi = getValidInput(b.etLokasi.text.toString())

        fetchJadwalData(tanggal, lokasi)

    }

    private fun getValidInput(input: String): String {
        return input.trim()
    }
}
