package com.saputra.wahyuaditya.posyandulansia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.saputra.wahyuaditya.posyandulansia.databinding.ItemJadwalBinding
import com.saputra.wahyuaditya.posyandulansia.model.Jadwal
import java.text.SimpleDateFormat
import java.util.Locale

class JadwalAdapter(
    private val list: List<Jadwal>,
    private val onClick: ((Jadwal) -> Unit)? = null
) : RecyclerView.Adapter<JadwalAdapter.ViewHolder>() {

    inner class ViewHolder(val b: ItemJadwalBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: Jadwal) {
            // Format input: "2025-07-08"
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
            val formattedDate = try {
                val date = inputFormat.parse(item.tanggal)
                outputFormat.format(date!!)
            } catch (e: Exception) {
                "-"
            }
            b.tvTanggal.text = formattedDate.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("id", "ID")) else it.toString() }

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
