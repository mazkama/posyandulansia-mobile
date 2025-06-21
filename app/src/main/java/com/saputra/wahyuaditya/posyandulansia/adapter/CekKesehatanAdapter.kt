package com.saputra.wahyuaditya.posyandulansia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.saputra.wahyuaditya.posyandulansia.R
import com.saputra.wahyuaditya.posyandulansia.model.CekKesehatan
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class CekKesehatanAdapter(
    private var listKesehatan: MutableList<CekKesehatan> = mutableListOf()) :
    RecyclerView.Adapter<CekKesehatanAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTanggal: TextView = view.findViewById(R.id.tvTanggal)
        val tvBeratBadan: TextView = view.findViewById(R.id.tvBeratBadan)
        val tvTekananDarah: TextView = view.findViewById(R.id.tvTekananDarah)
        val tvGulaDarah: TextView = view.findViewById(R.id.tvGulaDarah)
        val tvKolesterol: TextView = view.findViewById(R.id.tvKolesterol)
        val tvAsamUrat: TextView = view.findViewById(R.id.tvAsamUrat)
        val tvDiagnosa: TextView = view.findViewById(R.id.tvDiagnosa)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cek_kesehatan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val kesehatan = listKesehatan[position]
        holder.tvTanggal.text = "Tanggal: ${kesehatan.tanggal}"
        holder.tvBeratBadan.text = "Berat Badan: ${kesehatan.beratBadan} kg"
        holder.tvTekananDarah.text = "Tekanan Darah: ${kesehatan.tekananDarahSistolik}/${kesehatan.tekananDarahDiastolik} mmHg"
        holder.tvGulaDarah.text = "Gula Darah: ${kesehatan.gulaDarah} mg/dL"
        holder.tvKolesterol.text = "Kolesterol: ${kesehatan.kolestrol} mg/dL"
        holder.tvAsamUrat.text = "Asam Urat: ${kesehatan.asamUrat} mg/dL"

        val diagnosaText = formatDiagnosa(kesehatan.diagnosa)
        holder.tvDiagnosa.text = "Diagnosa: \n${diagnosaText}"
    }

    override fun getItemCount(): Int = listKesehatan.size

    fun updateData(newData: List<CekKesehatan>) {
        listKesehatan.clear() // Kosongkan daftar lama
        listKesehatan.addAll(newData) // Tambahkan daftar baru
        notifyDataSetChanged() // Perbarui tampilan
    }

    fun formatDiagnosa(diagnosaJson: String): String {
        return try {
            val type = object : TypeToken<Map<String, String>>() {}.type
            val diagnosaMap: Map<String, String> = Gson().fromJson(diagnosaJson, type)

            val formattedDiagnosa = StringBuilder()
            diagnosaMap.forEach { (key, value) ->
                formattedDiagnosa.append("${key.replace("_", " ").capitalize()} -> $value\n")
            }
            formattedDiagnosa.toString().trim()
        } catch (e: Exception) {
            "Diagnosa tidak tersedia."
        }
    }
}

