package com.saputra.wahyuaditya.posyandulansia

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ListAdapter
import android.widget.SimpleAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.saputra.wahyuaditya.posyandulansia.databinding.ActivityAntrianPeriksaBinding


class AntrianPeriksaActivity : AppCompatActivity() {

    private lateinit var b: ActivityAntrianPeriksaBinding
    private lateinit var database: DatabaseReference
    private lateinit var adapter: ListAdapter
    private var allLansia = ArrayList<HashMap<String, String>>()

    // Tambahkan variabel untuk menyimpan idJadwal
    private lateinit var idJadwal: String
    private lateinit var jadwalListener: ValueEventListener
    private lateinit var lansiaListener: ValueEventListener



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityAntrianPeriksaBinding.inflate(layoutInflater)
        setContentView(b.root)

        // Paksa tema terang
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Simpan idJadwal dalam variabel global
        idJadwal = intent.getIntExtra("id", -1).toString()
        val tanggal = intent.getStringExtra("tanggal") ?: "Tidak ada data"
        val waktu = intent.getStringExtra("waktu") ?: "Tidak ada data"
        val lokasi = intent.getStringExtra("lokasi") ?: "Tidak ada data"
        val keterangan = intent.getStringExtra("keterangan") ?: "Tidak ada keterangan"

        // Debugging log
        Log.d("AntrianPeriksaActivity", "ID: $idJadwal, Tanggal: $tanggal, Waktu: $waktu, Lokasi: $lokasi, Keterangan: $keterangan")

        // Inisialisasi Firebase Database
        database = FirebaseDatabase.getInstance().reference

        // Ambil data
        retrieveData()
        showData()

        b.buttonBack.setOnClickListener {
            finish()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish() // Menutup activity saat tombol kembali ditekan
            }
        })
    }

    private fun retrieveData() {
        jadwalListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val totalHadir = snapshot.child("totalHadir").getValue(Int::class.java) ?: 0
                    val totalBelumCek = snapshot.child("totalBelumCek").getValue(Int::class.java) ?: 0
                    val totalSudahCek = snapshot.child("totalSudahCek").getValue(Int::class.java) ?: 0

                    b.txtTotalHadir.text = totalHadir.toString()
                    b.txtTotalBelumCek.text = totalBelumCek.toString()
                    b.txtTotalSudahCek.text = totalSudahCek.toString()
                } else {
                    Toast.makeText(this@AntrianPeriksaActivity, "Belum Ada Antrian", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AntrianPeriksaActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }

        database.child("jadwal/$idJadwal").addValueEventListener(jadwalListener)
    }

    private fun showData() {
        lansiaListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allLansia.clear()
                for (childSnapshot in snapshot.children) {
                    val nama = childSnapshot.child("nama").getValue(String::class.java) ?: "Unknown"
                    val nik = childSnapshot.child("nik").getValue(String::class.java) ?: "Unknown"
                    val lansia_id = childSnapshot.child("lansia_id").getValue(Int::class.java)?.toString() ?: "Unknown"

                    val hm = hashMapOf("nama" to nama, "nik" to nik, "lansia_id" to lansia_id)
                    allLansia.add(hm)
                }

                adapter = SimpleAdapter(
                    this@AntrianPeriksaActivity,
                    allLansia,
                    R.layout.item_antrian,
                    arrayOf("nama", "nik"),
                    intArrayOf(R.id.txtItemNamaLansia, R.id.txtItemNIKLansia)
                )

                b.lvLansiaPeriksa.adapter = adapter

                // Set item click listener to show details
                b.lvLansiaPeriksa.setOnItemClickListener { parent, view, position, id ->
                    val selectedLansia = allLansia[position]  // Mengambil data lansia yang dipilih
                    val idJadwal = idJadwal // ID Jadwal yang dikirim
                    val lansiaId = selectedLansia["lansia_id"] // Lansia ID yang dikirim
                    val nama = selectedLansia["nama"]
                    val nik = selectedLansia["nik"]

                    // Membuat Intent untuk berpindah ke Activity lain
                    val intent = Intent(this@AntrianPeriksaActivity, CekKesehatanActivity::class.java)

                    // Mengirimkan data ID Jadwal dan Lansia ID ke Activity tujuan
                    intent.putExtra("idJadwal", idJadwal)
                    intent.putExtra("lansiaId", lansiaId)
                    intent.putExtra("nama", nama)
                    intent.putExtra("nik", nik)

                    // Menjalankan Intent untuk berpindah ke Activity lain
                    startActivity(intent)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AntrianPeriksaActivity, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }

        database.child("jadwal/$idJadwal/lansias").addValueEventListener(lansiaListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Hapus event listener saat activity dihancurkan untuk mencegah memory leaks
        database.child("jadwal/$idJadwal").removeEventListener(jadwalListener)
        database.child("jadwal/$idJadwal/lansias").removeEventListener(lansiaListener)
    }
}
