package com.saputra.wahyuaditya.posyandulansia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.saputra.wahyuaditya.posyandulansia.R
import com.saputra.wahyuaditya.posyandulansia.model.Lansia

class LansiaAdapter(
    private var lansiaList: MutableList<Lansia>,
    private val onItemClick: (Lansia) -> Unit
) : RecyclerView.Adapter<LansiaAdapter.LansiaViewHolder>() {

    class LansiaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nama: TextView = itemView.findViewById(R.id.tvNama)
        val nik: TextView = itemView.findViewById(R.id.tvNIK)
        val ttl: TextView = itemView.findViewById(R.id.tvTtl)
        val alamat: TextView = itemView.findViewById(R.id.tvAlamat)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LansiaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_lansia, parent, false)
        return LansiaViewHolder(view)
    }

    override fun onBindViewHolder(holder: LansiaViewHolder, position: Int) {
        val lansia = lansiaList[position]
        holder.nama.text = lansia.nama
        holder.nik.text = lansia.nik
        holder.ttl.text = lansia.ttl
        holder.alamat.text = lansia.alamat

        // Event klik item
        holder.itemView.setOnClickListener {
            onItemClick(lansia)  // Panggil listener klik
        }
    }

    override fun getItemCount(): Int = lansiaList.size

}
