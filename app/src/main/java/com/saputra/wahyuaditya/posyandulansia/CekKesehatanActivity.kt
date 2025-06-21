package com.saputra.wahyuaditya.posyandulansia

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.saputra.wahyuaditya.posyandulansia.databinding.ActivityCekKesehatanBinding
import com.saputra.wahyuaditya.posyandulansia.databinding.DialogHasilCekBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okio.IOException
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class CekKesehatanActivity : AppCompatActivity() {

    lateinit var b: ActivityCekKesehatanBinding
    private lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityCekKesehatanBinding.inflate(layoutInflater)
        setContentView(b.root)

        // Paksa tema terang
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        val idJadwal = intent.getStringExtra("idJadwal").toString()
        val lansiaId = intent.getStringExtra("lansiaId").toString()
        val nama = intent.getStringExtra("nama").toString()
        val nik = intent.getStringExtra("nik").toString()

        b.txtItemNamaLansia.text = nik
        b.txtItemNIKLansia.text = nama

        b.btnSimpanCekKesehatan.setOnClickListener {
            if (!validateInputs()) return@setOnClickListener

            val beratBadan = b.edBeratBadan.text.toString()
            val tekananDarahSistolik = b.edTekananDarahSistolik.text.toString()
            val tekananDarahDiastolik = b.edTekananDarahDiastolik.text.toString()
            val gulaDarah = b.edGulaDarah.text.toString()
            val kolestrol = b.edKolestrol.text.toString()
            val asamUrat = b.edAsamUrat.text.toString()

            dialog = Dialog(this)
            dialog.setContentView(R.layout.progress_dialog)
            dialog.setCancelable(false)

            sendDataToApi(
                idJadwal,
                lansiaId,
                beratBadan,
                tekananDarahSistolik,
                tekananDarahDiastolik,
                gulaDarah,
                kolestrol,
                asamUrat
            )
        }



        b.buttonBack.setOnClickListener{
            finish()
        }
    }

    private fun validateInputs(): Boolean {
        val beratBadan = b.edBeratBadan.text.toString()
        val tekananDarahSistolik = b.edTekananDarahSistolik.text.toString()
        val tekananDarahDiastolik = b.edTekananDarahDiastolik.text.toString()
        val gulaDarah = b.edGulaDarah.text.toString()
        val kolestrol = b.edKolestrol.text.toString()
        val asamUrat = b.edAsamUrat.text.toString()

        // Validasi kosong
        if (beratBadan.isEmpty() || tekananDarahSistolik.isEmpty() || tekananDarahDiastolik.isEmpty()
            || gulaDarah.isEmpty() || kolestrol.isEmpty() || asamUrat.isEmpty()
        ) {
            Toast.makeText(this, "Semua data harus diisi", Toast.LENGTH_SHORT).show()
            return false
        }

        val beratValue = beratBadan.toIntOrNull()
        val sistolikValue = tekananDarahSistolik.toIntOrNull()
        val diastolikValue = tekananDarahDiastolik.toIntOrNull()
        val gulaValue = gulaDarah.toIntOrNull()
        val kolestrolValue = kolestrol.toIntOrNull()
        val asamUratValue = asamUrat.toDoubleOrNull()

        if (beratValue == null) {
            Toast.makeText(this, "Berat badan tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return false
        }
        if (sistolikValue == null) {
            Toast.makeText(this, "Tekanan darah sistolik tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return false
        }
        if (diastolikValue == null) {
            Toast.makeText(this, "Tekanan darah diastolik tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return false
        }
        if (gulaValue == null) {
            Toast.makeText(this, "Gula darah tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return false
        }
        if (kolestrolValue == null) {
            Toast.makeText(this, "Kolesterol tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return false
        }
        if (asamUratValue == null) {
            Toast.makeText(this, "Asam urat tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }


    private fun sendDataToApi(
        idJadwal: String, lansiaId: String, beratBadan: String, tekananDarahSistolik: String,
        tekananDarahDiastolik: String, gulaDarah: String, kolestrol: String, asamUrat: String
    ) {
        dialog.show()

        val url = "https://posyandulansia.supala.biz.id/api/cek-kesehatan"
        val client = OkHttpClient()

        val jsonObject = JSONObject().apply {
            put("lansia_id", lansiaId.toInt())
            put("jadwal_id", idJadwal.toInt())
            put("berat_badan", beratBadan.toInt())
            put("tekanan_darah_sistolik", tekananDarahSistolik.toInt())
            put("tekanan_darah_diastolik", tekananDarahDiastolik.toInt())
            put("gula_darah", gulaDarah.toInt())
            put("kolestrol", kolestrol.toInt())
            put("asam_urat", asamUrat.toDouble())
        }

        val requestBody = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), jsonObject.toString())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .header("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    dialog.dismiss()
                    Toast.makeText(this@CekKesehatanActivity, "Gagal mengirim data!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    // Hanya dismiss dialog jika dialog sudah ada
                    dialog?.dismiss() // Pastikan dialog tidak null sebelum dismiss

                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        responseBody?.let { jsonResponse ->
                            val jsonObject = JSONObject(jsonResponse)
                            val data = jsonObject.getJSONObject("data")
                            val diagnosa = JSONArray(data.getString("diagnosa"))
                            val kesimpulan = jsonObject.getJSONArray("kesimpulan")
                            val solusi = jsonObject.getJSONArray("solusi")

                            val tanggal = data.getString("created_at")

                            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
                            inputFormat.timeZone = TimeZone.getTimeZone("UTC")

                            val outputFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
                            val formattedDate = try {
                                val date = inputFormat.parse(tanggal)
                                outputFormat.format(date!!)
                            } catch (e: Exception) {
                                "-"
                            }

                            val tanggalCek = formattedDate


                            showResultDialog(data, diagnosa, kesimpulan, solusi, tanggalCek)
                        }
                    } else {
                        Toast.makeText(this@CekKesehatanActivity, "Terjadi kesalahan!", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        })
    }

    private fun showResultDialog(data: JSONObject, diagnosa: JSONArray, kesimpulan: JSONArray, solusi: JSONArray, tanggalCek: String) {
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialog_hasil_cek, null)

        dialogView.findViewById<TextView>(R.id.titleTanggal).text = tanggalCek

        // Isi nilai hasil cek
        dialogView.findViewById<TextView>(R.id.txtHasilBB).text = "${data.optDouble("berat_badan", 0.0)} kg"
        dialogView.findViewById<TextView>(R.id.txtHasilTD).text = "${data.optInt("tekanan_darah_sistolik", 0)}/${data.optInt("tekanan_darah_diastolik", 0)} mmHg"
        dialogView.findViewById<TextView>(R.id.txtHasilGulaDarah).text = "${data.optInt("gula_darah", 0)} mg/dL"
        dialogView.findViewById<TextView>(R.id.txtHasilKolestrol).text = "${data.optInt("kolestrol", 0)} mg/dL"
        dialogView.findViewById<TextView>(R.id.txtHasilAsamUrat).text = "${data.optDouble("asam_urat", 0.0)} mg/dL"

        // Diagnosa (jika format string array)
        val diagnosaList = mutableListOf<String>()
        for (i in 0 until diagnosa.length()) {
            diagnosaList.add("- ${diagnosa.getString(i)}")
        }
        dialogView.findViewById<TextView>(R.id.txtDiagnosaBB).text = diagnosaList.joinToString("\n")

        // Kesimpulan (jika format string array)
        val kesimpulanList = mutableListOf<String>()
        for (i in 0 until kesimpulan.length()) {
            kesimpulanList.add("• ${kesimpulan.getString(i)}")
        }
        dialogView.findViewById<TextView>(R.id.txtKualitasKesehatan).text = kesimpulanList.joinToString("\n")

        // solusi (jika format string array)
        val solusiList = mutableListOf<String>()
        for (i in 0 until solusi.length()) {
            solusiList.add("• ${solusi.getString(i)}")
        }
        dialogView.findViewById<TextView>(R.id.txtSolusiKesehatan).text = solusiList.joinToString("\n")

        // Tampilkan dialog
        runOnUiThread {
            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("OK") { d, _ ->
                    d.dismiss()
                    finish()
                }
                .create()

            dialog.show()
        }
    }
}
