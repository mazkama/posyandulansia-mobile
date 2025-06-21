package com.saputra.wahyuaditya.posyandulansia.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.saputra.wahyuaditya.posyandulansia.MainActivity
import com.saputra.wahyuaditya.posyandulansia.adapter.NotifikasiAdapter
import com.saputra.wahyuaditya.posyandulansia.databinding.ActivityNotifikasiBinding
import com.saputra.wahyuaditya.posyandulansia.model.Notifikasi
import com.saputra.wahyuaditya.posyandulansia.model.NotifikasiResponse
import com.saputra.wahyuaditya.posyandulansia.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotifikasiActivity : Fragment() {
    private lateinit var b: ActivityNotifikasiBinding
    private lateinit var v: View
    private lateinit var thisParent: MainActivity
    private lateinit var notifikasiAdapter: NotifikasiAdapter
    private val notifikasiList = mutableListOf<Notifikasi>()
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
        b = ActivityNotifikasiBinding.inflate(inflater, container, false)
        v = b.root

        setupRecyclerView()
        setupListeners()
        fetchNotifikasiData()

        return v
    }

    private fun setupRecyclerView() {
        notifikasiAdapter = NotifikasiAdapter(notifikasiList) { notifikasi ->
            // Aksi saat item diklik
        }
        with(b.rvNotifikasi) {
            layoutManager = LinearLayoutManager(thisParent)
            adapter = notifikasiAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    if (!isLoading && !isLastPage && layoutManager.findLastVisibleItemPosition() >= layoutManager.itemCount - 5) {
                        fetchNotifikasiData()
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
        }
    }

    private fun fetchNotifikasiData() {
        if (isLoading) return
        isLoading = true
        b.swipeRefreshLayout.isRefreshing = true

        ApiClient.instance.getNotifikasi(currentPage).enqueue(object :
            Callback<NotifikasiResponse> {
            override fun onResponse(call: Call<NotifikasiResponse>, response: Response<NotifikasiResponse>) {
                isLoading = false
                b.swipeRefreshLayout.isRefreshing = false

                val data = response.body()?.data?.data.orEmpty()
                if (currentPage == 1) notifikasiList.clear()
                notifikasiList.addAll(data)
                notifikasiAdapter.notifyDataSetChanged()

                isLastPage = currentPage >= (response.body()?.data?.last_page ?: 0)
                if (!isLastPage) currentPage++
            }

            override fun onFailure(call: Call<NotifikasiResponse>, t: Throwable) {
                isLoading = false
                b.swipeRefreshLayout.isRefreshing = false
            }
        })
    }

    private fun refreshData() {
        currentPage = 1
        isLastPage = false
        fetchNotifikasiData()
    }
}