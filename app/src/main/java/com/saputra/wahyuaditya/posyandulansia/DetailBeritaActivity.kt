package com.saputra.wahyuaditya.posyandulansia

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.saputra.wahyuaditya.posyandulansia.databinding.ActivityDetailBeritaBinding
import com.saputra.wahyuaditya.posyandulansia.model.Berita
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class DetailBeritaActivity : AppCompatActivity() {

    private lateinit var b: ActivityDetailBeritaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityDetailBeritaBinding.inflate(layoutInflater)
        setContentView(b.root)

        // Terima data Berita dari Intent
        val berita = intent.getSerializableExtra("berita") as Berita?

        // Jika menggunakan Serializable, ganti dengan:
        // val berita = intent.getSerializableExtra("berita") as Berita?

        if (berita != null) {
            // Tampilkan judul
            b.tvJudulBerita.text = berita.judul
            // Tampilkan tanggal
            b.tvTanggalBerita.text = formatTanggal(berita.created_at)
            // Tampilkan konten
            b.tvKontenBerita.text = berita.konten
            // Tampilkan gambar (pastikan gambar ada di drawable atau gunakan Glide/Picasso jika dari URL)

            // URL lengkap foto
            val baseUrl = "https://posyandulansia.supala.biz.id/storage/" // Ganti sesuai path penyimpanan gambarmu
            val imageUrl = baseUrl + berita.foto

            // Load image pakai Glide
            Glide.with(b.root.context)
                .load(imageUrl)
                .into(b.imageBerita)
//            b.imageBerita.setImageResource(
//                resources.getIdentifier(berita., "drawable", packageName)
//            )
        }

        b.buttonBack.setOnClickListener {
            finish()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    fun formatTanggal(createdAt: String): String? {
        return try {
            // Format input sesuai format string asal
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US)
            inputFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
            val date: Date = inputFormat.parse(createdAt) ?: return null

            // Format output sesuai keinginan
            val outputFormat = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("id", "ID"))
            outputFormat.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
