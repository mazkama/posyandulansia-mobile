package com.saputra.wahyuaditya.posyandulansia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.saputra.wahyuaditya.posyandulansia.R
import com.saputra.wahyuaditya.posyandulansia.model.Notifikasi

class NotifikasiAdapter(
    private var notifikasiList: MutableList<Notifikasi>,
    private val onItemClick: (Notifikasi) -> Unit
) : RecyclerView.Adapter<NotifikasiAdapter.NotifikasiViewHolder>() {

    class NotifikasiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pesan: TextView = itemView.findViewById(R.id.tvPesan)
        val tanggal: TextView = itemView.findViewById(R.id.tvTanggalKirim)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotifikasiViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notifikasi, parent, false)
        return NotifikasiViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotifikasiViewHolder, position: Int) {
        val notifikasi = notifikasiList[position]
        holder.pesan.text = notifikasi.pesan
        holder.tanggal.text = notifikasi.tanggal_kirim

        // Event klik item
        holder.itemView.setOnClickListener {
            onItemClick(notifikasi)  // Panggil listener klik
        }
    }

    override fun getItemCount(): Int = notifikasiList.size
}
