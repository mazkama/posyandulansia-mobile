package com.saputra.wahyuaditya.posyandulansia

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.saputra.wahyuaditya.posyandulansia.adapter.LansiaAdapter
import com.saputra.wahyuaditya.posyandulansia.databinding.ActivityDataLansiaBinding
import com.saputra.wahyuaditya.posyandulansia.model.Lansia
import com.saputra.wahyuaditya.posyandulansia.model.LansiaResponse
import com.saputra.wahyuaditya.posyandulansia.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DataLansiaActivity : AppCompatActivity() {

    private lateinit var b: ActivityDataLansiaBinding
    private lateinit var lansiaAdapter: LansiaAdapter
    private val lansiaList = mutableListOf<Lansia>()
    private var currentPage = 1
    private var isLoading = false
    private var isLastPage = false
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityDataLansiaBinding.inflate(layoutInflater)
        setContentView(b.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) // Paksa tema terang

        sharedPreferences = this.getSharedPreferences("UserSession", Context.MODE_PRIVATE)!!

        val namaDesa = sharedPreferences.getString("namaDesa", "") ?: ""
        val formattedNamaDesa = namaDesa.lowercase().split(" ").joinToString(" ") { it.capitalize() }
        b.tvHeader.text = "Data Lansia $formattedNamaDesa"

        setupRecyclerView()
        setupListeners()
        fetchLansiaData("")

        b.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun showParameterDialog(lansia: Lansia) {
        val items = arrayOf(
            "Tekanan Darah",
            "Gula Darah",
            "Kolesterol",
            "Asam Urat"
        )
        val paramKeys = arrayOf(
            "tekanan_darah",
            "gula_darah",
            "kolesterol",
            "asam_urat"
        )
        AlertDialog.Builder(this)
            .setTitle("Lihat Riwayat Kesehatan")
            .setItems(items) { _, which ->
                val intent = Intent(this, ActivityParameterKesehatan::class.java)
                intent.putExtra("user_id", lansia.user_id) // Pastikan Lansia punya field id
                intent.putExtra("parameter", paramKeys[which])
                startActivity(intent)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun setupRecyclerView() {
        lansiaAdapter = LansiaAdapter(lansiaList) { lansia ->
            showParameterDialog(lansia)
        }
        with(b.rvLansia) {
            layoutManager = LinearLayoutManager(this@DataLansiaActivity)
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

        val desaId = sharedPreferences.getString("idDesaUser", "") ?: ""

        ApiClient.instance.getLansias(keyword, currentPage, desaId).enqueue(object : Callback<LansiaResponse> {
            override fun onResponse(call: Call<LansiaResponse>, response: Response<LansiaResponse>) {
                isLoading = false
                b.swipeRefreshLayout.isRefreshing = false

                val data = response.body()?.data?.data.orEmpty()
                if (currentPage == 1) lansiaList.clear()
                lansiaList.addAll(data)
                lansiaAdapter.notifyDataSetChanged()

                isLastPage = currentPage >= (response.body()?.data?.last_page ?: 0)
                if (!isLastPage) currentPage++

                b.tvNoData.visibility = if (lansiaList.isEmpty()) View.VISIBLE else View.GONE
            }

            override fun onFailure(call: Call<LansiaResponse>, t: Throwable) {
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
        fetchLansiaData(b.etSearch.text.toString())
    }
}