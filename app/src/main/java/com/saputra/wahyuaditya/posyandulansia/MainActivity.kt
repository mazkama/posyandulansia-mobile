package com.saputra.wahyuaditya.posyandulansia

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.saputra.wahyuaditya.posyandulansia.databinding.ActivityMainBinding
import com.saputra.wahyuaditya.posyandulansia.fragment.NotifikasiActivity
import com.saputra.wahyuaditya.posyandulansia.fragment.ProfilActivity
import com.saputra.wahyuaditya.posyandulansia.fragment.beranda.KaderActivity
import com.saputra.wahyuaditya.posyandulansia.fragment.beranda.LansiaActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject

class MainActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener {

    lateinit var b: ActivityMainBinding
    lateinit var sharedPreferences: SharedPreferences

    lateinit var fberandaKader : KaderActivity
    lateinit var fberandaLansia : LansiaActivity
    lateinit var fnotifikasi : NotifikasiActivity
    lateinit var fprofil : ProfilActivity
    lateinit var ft : FragmentTransaction
    lateinit var role : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        // Paksa tema terang
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)
        role = sharedPreferences.getString("role", "") ?: ""

        b.bottomNavigationView.setOnItemSelectedListener(this)

        fberandaKader =  KaderActivity()
        fberandaLansia = LansiaActivity()
        fnotifikasi = NotifikasiActivity()
        fprofil = ProfilActivity()
        
        b.bottomNavigationView.setSelectedItemId(R.id.itemBeranda)

        ft = supportFragmentManager.beginTransaction()
        val selectedFragment = if (role == "kader") fberandaKader else fberandaLansia
        ft.replace(R.id.frameLayout, selectedFragment).commit()
        b.frameLayout.setBackgroundColor(Color.argb(255,255,255,255))
        b.frameLayout.visibility = View.VISIBLE

        FirebaseMessaging.getInstance().subscribeToTopic("PosyanduLansia")
            .addOnCompleteListener { task ->
                var msg = "Subscribed"
                if (!task.isSuccessful) {
                    msg = "Subscription failed"
                }
                Log.d("FCM", msg)
//                Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
            }

    }

    override fun onStart() {
        super.onStart()
        // Memeriksa izin untuk notifikasi hanya di Android 13 dan lebih baru
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                // Jika izin belum diberikan, minta izin
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            }
        }
    }

    // Menangani hasil dari permintaan izin
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Izin diterima, Anda bisa menampilkan notifikasi
                Toast.makeText(this, "Izin notifikasi diterima!", Toast.LENGTH_SHORT).show()
            } else {
                // Izin ditolak, beri tahu pengguna
                Toast.makeText(this, "Izin notifikasi ditolak.", Toast.LENGTH_SHORT).show()
                Toast.makeText(this, " Aplikasi tidak dapat menampilkan notifikasi.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.itemNotifikasi -> {
                ft = supportFragmentManager.beginTransaction()
                ft.replace(R.id.frameLayout,fnotifikasi).commit()
                b.frameLayout.setBackgroundColor(Color.argb(255,255,255,255))
                b.frameLayout.visibility = View.VISIBLE
            }
            R.id.itemBeranda -> {
                ft = supportFragmentManager.beginTransaction()
                val selectedFragment = if (role == "kader") fberandaKader else fberandaLansia
                ft.replace(R.id.frameLayout, selectedFragment).commit()
                b.frameLayout.setBackgroundColor(Color.argb(255,255,255,255))
                b.frameLayout.visibility = View.VISIBLE
            }
            R.id.itemProfil -> {
                ft = supportFragmentManager.beginTransaction()
                ft.replace(R.id.frameLayout,fprofil).commit()
                b.frameLayout.setBackgroundColor(Color.argb(255,255,255,255))
                b.frameLayout.visibility = View.VISIBLE
            }
        }
        return true
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}