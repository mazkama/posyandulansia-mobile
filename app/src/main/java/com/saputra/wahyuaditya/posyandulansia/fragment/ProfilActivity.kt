package com.saputra.wahyuaditya.posyandulansia.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.saputra.wahyuaditya.posyandulansia.LoginActivity
import com.saputra.wahyuaditya.posyandulansia.MainActivity
import com.saputra.wahyuaditya.posyandulansia.databinding.ActivityProfilBinding

class ProfilActivity : Fragment(){

    private lateinit var b: ActivityProfilBinding
    private lateinit var v: View
    private lateinit var thisParent: MainActivity
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Initialize binding and view
        thisParent = activity as MainActivity
        b = ActivityProfilBinding.inflate(inflater, container, false)
        v = b.root

        sharedPreferences = activity?.getSharedPreferences("UserSession", Context.MODE_PRIVATE)!!

        getUserData()


        b.btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Konfirmasi Logout")
                .setMessage("Apakah Anda yakin ingin logout?")
                .setPositiveButton("Logout") { dialog, _ ->
                    logout() // Fungsi logout kamu
                    dialog.dismiss()
                }
                .setNegativeButton("Batal") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }


        return v
    }

    private fun getUserData(){
        val namaUser = sharedPreferences.getString("namaUser", "") ?: ""
        val nikUser = sharedPreferences.getString("nikUser", "") ?: ""
        val ttlUser = sharedPreferences.getString("ttlUser", "") ?: ""
        val alamatUser = sharedPreferences.getString("alamatUser", "") ?: ""
        val noHpUser = sharedPreferences.getString("noHpUser", "") ?: ""

        b.edNama.setText(namaUser)
        b.edNik.setText(nikUser)
        b.edTtl.setText(ttlUser)
        b.edAlamat.setText(alamatUser)
        b.edNoHp.setText(noHpUser)
    }

    private fun logout() {

        // Hapus data pengguna dari SharedPreferences
        sharedPreferences.edit().clear().apply()

        // Arahkan user ke halaman login
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        thisParent.finish() // Tutup dashboard agar tidak bisa kembali dengan tombol back
    }
}