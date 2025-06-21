package com.saputra.wahyuaditya.posyandulansia

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONObject

class SplashActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mengunci tampilan ke potret
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_splash)

        // Tambahkan animasi fade-in pada logo
        val splashImage: ImageView = findViewById(R.id.splash_image)
        splashImage.setImageResource(R.drawable.logo)
        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 2000
            fillAfter = true
        }
        splashImage.startAnimation(fadeIn)

        // Inisialisasi SharedPreferences
        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)
        val token = sharedPreferences.getString("access_token", null)

        Log.d("SplashActivity", "Token: $token") // Debugging

        // Gunakan Handler untuk tetap pindah layar meskipun token null
        Handler(Looper.getMainLooper()).postDelayed({
            if (token != null) {
                validateToken(token)
            } else {
                moveToLogin()
            }
        }, 2500) // Tunggu 2.5 detik sebelum berpindah
    }

    private fun validateToken(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = Request.Builder()
                    .url("https://posyandulansia.supala.biz.id/api/check-token")
                    .header("Authorization", "Bearer $token")
                    .get()
                    .build()

                val response = client.newCall(request).execute()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        moveToDashboard()
                    } else {
                        sharedPreferences.edit().clear().apply()
                        Toast.makeText(this@SplashActivity, "Sesi berakhir, silakan login ulang", Toast.LENGTH_SHORT).show()
                        moveToLogin()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("SplashActivity", "Error Validasi Token: ${e.message}")
                    Toast.makeText(this@SplashActivity, "Gagal memverifikasi token", Toast.LENGTH_SHORT).show()
                    moveToLogin()
                }
            }
        }
    }

    private fun moveToDashboard() {
        val role = sharedPreferences.getString("role", "") ?: ""
        val intent = when (role) {
            "kader", "lansia" -> Intent(this, MainActivity::class.java)
            else -> {
                Toast.makeText(this, "Gagal login, role tidak tersedia", Toast.LENGTH_SHORT).show()
                moveToLogin()
                return
            }
        }
        startActivityWithAnimation(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun moveToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivityWithAnimation(intent)
    }

    private fun startActivityWithAnimation(intent: Intent) {
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
