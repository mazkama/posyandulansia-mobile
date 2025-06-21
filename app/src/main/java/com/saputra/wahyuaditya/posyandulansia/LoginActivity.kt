package com.saputra.wahyuaditya.posyandulansia

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.saputra.wahyuaditya.posyandulansia.databinding.ActivityLoginBinding
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private lateinit var b: ActivityLoginBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(b.root)

        // Paksa tema terang
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)

        b.btnLogin.setOnClickListener { handleLogin() }
    }

    private fun handleLogin() {
        val username = b.edUsername.text.toString().trim()
        val password = b.edPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            showToast("Username dan password wajib diisi!")
            return
        }

        b.progressBar.visibility = View.VISIBLE
        loginToServer(username, password)
    }

    private fun loginToServer(username: String, password: String) {
        val body = FormBody.Builder()
            .add("username", username)
            .add("password", password)
            .build()

        val request = Request.Builder()
            .url("https://posyandulansia.supala.biz.id/api/login")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    b.progressBar.visibility = View.GONE
                    showToast("Gagal terhubung ke server: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread { b.progressBar.visibility = View.GONE }
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    handleLoginSuccess(responseBody)
                } else {
                    runOnUiThread { showToast("Login gagal! Periksa username dan password.") }
                }
            }
        })
    }

    private fun handleLoginSuccess(responseBody: String) {
        val jsonResponse = JSONObject(responseBody)
        val accessToken = jsonResponse.getString("access_token")
        val user = jsonResponse.getJSONObject("user")
        val role = user.getString("role")

        sharedPreferences.edit().apply {
            putString("access_token", accessToken)
            putString("idUser", user.getString("id"))
            putString("username", user.getString("username"))
            putString("role", user.getString("role"))
            apply()
        }

        val dataUser = if (role == "kader") user.getJSONObject("kader") else user.getJSONObject("lansia")
        sharedPreferences.edit().apply {
            putString("namaUser", dataUser.getString("nama"))
            putString("nikUser", dataUser.getString("nik"))
            putString("ttlUser", dataUser.getString("ttl"))
            putInt("umurUser", dataUser.getInt("umur"))
            putString("alamatUser", dataUser.getString("alamat"))
            putString("noHpUser", dataUser.getString("no_hp"))
            apply()
        }

        redirectToDashboard()

    }

    private fun redirectToDashboard() {
        val role = sharedPreferences.getString("role", "") ?: ""
        val intent = when (role) {
            "kader", "lansia" -> Intent(this, MainActivity::class.java)
            else -> {
                showToast("Gagal login, role tidak tersedia")
                return
            }
        }
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
