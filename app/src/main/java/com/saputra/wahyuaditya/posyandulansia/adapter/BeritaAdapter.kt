package com.saputra.wahyuaditya.posyandulansia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.saputra.wahyuaditya.posyandulansia.databinding.ItemBeritaBinding
import com.saputra.wahyuaditya.posyandulansia.model.Berita

class BeritaAdapter (
    private val list: List<Berita>,
    private val onClick: ((Berita) -> Unit)? = null
) : RecyclerView.Adapter<BeritaAdapter.ViewHolder>() {

    inner class ViewHolder(val b: ItemBeritaBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: Berita) {
            b.tvTanggal.text = item.tanggal_publish
            b.tvJudul.text = item.judul
            b.tvKonten.text = item.konten

            // URL lengkap foto
            val baseUrl = "https://posyandulansia.supala.biz.id/storage/" // Ganti sesuai path penyimpanan gambarmu
            val imageUrl = baseUrl + item.foto

            // Load image pakai Glide
            Glide.with(b.root.context)
                .load(imageUrl)
                .into(b.ivFotoBerita)
            b.root.setOnClickListener { onClick?.invoke(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BeritaAdapter.ViewHolder {
        val binding = ItemBeritaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: BeritaAdapter.ViewHolder, position: Int) {
        holder.bind(list[position])
    }
}