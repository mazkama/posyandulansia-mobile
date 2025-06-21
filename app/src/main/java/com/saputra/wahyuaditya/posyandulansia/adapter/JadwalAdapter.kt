package com.saputra.wahyuaditya.posyandulansia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.saputra.wahyuaditya.posyandulansia.databinding.ItemJadwalBinding
import com.saputra.wahyuaditya.posyandulansia.model.Jadwal
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class JadwalAdapter(
    private val list: List<Jadwal>,
    private val onClick: ((Jadwal) -> Unit)? = null
) : RecyclerView.Adapter<JadwalAdapter.ViewHolder>() {

    inner class ViewHolder(val b: ItemJadwalBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: Jadwal) {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")

            val outputFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
            val formattedDate = try {
                val date = inputFormat.parse( item.created_at)
                outputFormat.format(date!!)
            } catch (e: Exception) {
                "-"
            }
            b.tvTanggal.text = formattedDate

            b.tvWaktu.text = "Pukul ${item.waktu}"
            b.tvLokasi.text = item.lokasi
            b.tvStatus.text = item.status
            b.root.setOnClickListener { onClick?.invoke(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemJadwalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }
}
