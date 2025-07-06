package com.saputra.wahyuaditya.posyandulansia

import android.app.Dialog
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.LargeValueFormatter
import com.saputra.wahyuaditya.posyandulansia.databinding.ActivityParameterKesehatanBinding
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ActivityParameterKesehatan : AppCompatActivity() {

    private lateinit var b: ActivityParameterKesehatanBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val client = OkHttpClient()
    private lateinit var dialog: Dialog
    private lateinit var chart: BarChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityParameterKesehatanBinding.inflate(layoutInflater)
        setContentView(b.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        dialog = Dialog(this)
        dialog.setContentView(R.layout.progress_dialog)
        dialog.setCancelable(false)

        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)
        chart = b.barChart
        setupChart()

        val userId = sharedPreferences.getString("idUser", "0")?.toInt() ?: 0
        val parameter = intent.getStringExtra("parameter") ?: "tekanan_darah"

        val labelParameter = when (parameter) {
            "tekanan_darah" -> "Tekanan Darah"
            "gula_darah" -> "Gula Darah"
            "kolesterol" -> "Kolesterol"
            "asam_urat" -> "Asam Urat"
            else -> parameter.replace("_", " ").replaceFirstChar { it.uppercase() }
        }

        val labelBatasNormal = when (parameter) {
            "tekanan_darah" -> "Batas Normal \nTinggi: 90-120 mmHg \nRendah: 60-80 mmHg"
            "gula_darah"    -> "Batas Normal \nTinggi: 100 mg/dL \nRendah: 70 mg/dL"
            "kolesterol"    -> "Batas Normal \nTinggi: 200 mg/dL \nRendah: 120 mg/dL"
            "asam_urat"     -> "Batas Normal \nTinggi: 7 mg/dL \nRendah: 2 mg/dL"
            else -> ""
        }

        b.batasnormal.text = labelBatasNormal
        b.tvParameter.text = labelParameter

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (2019..currentYear).map { it.toString() }.reversed()

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        b.spinnerYear.adapter = adapter

        b.spinnerYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedYear = parent.getItemAtPosition(position).toString()
                fetchData(userId, parameter, selectedYear)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        b.btnBack.setOnClickListener { finish() }

        b.spinnerYear.setSelection(0)
        fetchData(userId, parameter, years.first())
    }

    private fun setupChart() {
        chart.setTouchEnabled(true)
        chart.description.isEnabled = false
        chart.setDrawGridBackground(false)
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.granularity = 1f
        chart.axisRight.isEnabled = false
        chart.setPinchZoom(true)
        chart.xAxis.labelRotationAngle = -45f
    }

    private fun getKategoriNilai(parameter: String, nilai: Float): String {
        return when (parameter) {
            "gula_darah" -> when {
                nilai < 70 -> "rendah"
                nilai > 100 -> "tinggi"
                else -> "normal"
            }
            "kolesterol" -> when {
                nilai < 120 -> "rendah"
                nilai > 200 -> "tinggi"
                else -> "normal"
            }
            "asam_urat" -> when {
                nilai < 2 -> "rendah"
                nilai > 7 -> "tinggi"
                else -> "normal"
            }
            else -> "normal"
        }
    }

    private fun fetchData(idUser: Int, parameter: String, year: String) {
        val url = "https://posyandulansia.supala.biz.id/api/cek-kesehatan/$idUser/parameter/$parameter?year=$year"
        val request = Request.Builder().url(url).build()

        dialog.show()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                dialog.dismiss()
                Log.e("HTTP", "Failed: $e")
            }

            override fun onResponse(call: Call, response: Response) {
                dialog.dismiss()
                val responseBody = response.body?.string()
                if (!response.isSuccessful || responseBody == null) return

                val json = JSONObject(responseBody)
                val dataArray = json.getJSONArray("data")

                if (dataArray.length() == 0) {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(this@ActivityParameterKesehatan, "Tidak ada riwayat pemeriksaan untuk tahun $year", Toast.LENGTH_SHORT).show()
                        b.tvJadwal.text = "Jadwal Pemeriksaan"
                        b.tvKesimpulan.text = "-"
                        b.tvSolusi.text = "-"
                        chart.clear()
                        chart.invalidate()
                    }
                    return
                }

                val latestObj = dataArray.getJSONObject(dataArray.length() - 1)
                val tanggal = latestObj.getString("tanggal")
                val kesimpulanArray = latestObj.getJSONArray("kesimpulan")
                val solusiArray = latestObj.getJSONArray("solusi")

                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
                val formattedDate = try {
                    outputFormat.format(inputFormat.parse(tanggal)!!)
                } catch (e: Exception) {
                    tanggal
                }

                val kesimpulanText = StringBuilder()
                for (i in 0 until kesimpulanArray.length()) {
                    kesimpulanText.append("• ").append(kesimpulanArray.getString(i)).append("\n")
                }

                val solusiText = StringBuilder()
                for (i in 0 until solusiArray.length()) {
                    solusiText.append("• ").append(solusiArray.getString(i)).append("\n")
                }

                Handler(Looper.getMainLooper()).post {
                    b.tvJadwal.text = "Jadwal Pemeriksaan\n$formattedDate"
                    b.tvKesimpulan.text = kesimpulanText.toString().trim()
                    b.tvSolusi.text = solusiText.toString().trim()
                }

                val entries1 = ArrayList<BarEntry>()
                val entries2 = ArrayList<BarEntry>()
                val colors1 = ArrayList<Int>()
                val colors2 = ArrayList<Int>()
                val tanggalList = ArrayList<String>()

                val outputMonth = SimpleDateFormat("MMMM", Locale("id", "ID"))

                for (i in 0 until dataArray.length()) {
                    val obj = dataArray.getJSONObject(i)
                    val tanggalString = obj.getString("tanggal")
                    val nilaiString = obj.optString(parameter, "")
                    val bulan = try {
                        outputMonth.format(inputFormat.parse(tanggalString)!!)
                    } catch (e: Exception) {
                        "Unknown"
                    }

                    if (parameter == "tekanan_darah" && nilaiString.contains("/")) {
                        val parts = nilaiString.split("/")
                        val sistolik = parts[0].toFloatOrNull()
                        val diastolik = parts[1].toFloatOrNull()
                        if (sistolik != null && diastolik != null) {
                            entries1.add(BarEntry(i.toFloat(), sistolik))
                            entries2.add(BarEntry(i.toFloat(), diastolik))

                            val kategori = when {
                                sistolik < 90 || diastolik < 60 -> "rendah"
                                sistolik > 120 || diastolik > 80 -> "tinggi"
                                else -> "normal"
                            }

                            val color = if (kategori == "normal") Color.GREEN else Color.RED
                            colors1.add(color)
                            colors2.add(color)
                            tanggalList.add(bulan)
                        }
                    } else {
                        val nilai = nilaiString.toFloatOrNull()
                        if (nilai != null) {
                            entries1.add(BarEntry(i.toFloat(), nilai))
                            val kategori = getKategoriNilai(parameter, nilai)
                            val color = if (kategori == "normal") Color.GREEN else Color.RED
                            colors1.add(color)
                            tanggalList.add(bulan)
                        }
                    }
                }

                runOnUiThread {
                    val data: BarData
                    chart.clear()
                    chart.fitScreen()

                    if (parameter == "tekanan_darah") {
                        val dataSet1 = BarDataSet(entries1, "Sistolik").apply {
                            setColors(colors1)
                            valueTextSize = 12f
                            valueTextColor = Color.BLACK
                        }

                        val dataSet2 = BarDataSet(entries2, "Diastolik").apply {
                            setColors(colors2)
                            valueTextSize = 12f
                            valueTextColor = Color.BLACK
                        }

                        data = BarData(dataSet1, dataSet2)
                        data.barWidth = 0.25f

                        chart.data = data

                        val xAxis = chart.xAxis
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.setDrawGridLines(false)
                        xAxis.labelRotationAngle = -45f
                        xAxis.textSize = 10f
                        xAxis.granularity = 1f
                        xAxis.isGranularityEnabled = true
                        xAxis.valueFormatter = IndexAxisValueFormatter(tanggalList)
                        xAxis.setCenterAxisLabels(true)
                        xAxis.axisMinimum = -0.8f
                        xAxis.axisMaximum = (entries1.size - 1) + 0.8f

                        chart.groupBars(0f, 0.4f, 0.05f)
                        chart.setVisibleXRangeMaximum(6f)
                        chart.moveViewToX((entries1.size - 1).toFloat())
                    } else {
                        val label = parameter.replace("_", " ").replaceFirstChar { it.uppercaseChar() }
                        val dataSet = BarDataSet(entries1, label).apply {
                            setColors(colors1)
                            valueTextSize = 12f
                            valueTextColor = Color.BLACK
                        }

                        data = BarData(dataSet)
                        data.barWidth = 0.7f
                        chart.data = data

                        val xAxis = chart.xAxis
                        xAxis.setCenterAxisLabels(false)
                        xAxis.valueFormatter = IndexAxisValueFormatter(tanggalList)
                        xAxis.axisMinimum = -0.5f
                        xAxis.axisMaximum = (entries1.size - 1) + 0.5f
                        xAxis.setLabelCount(entries1.size, false)

                        chart.setVisibleXRangeMaximum(6f)
                        chart.moveViewToX((entries1.size - 1).toFloat())
                    }

                    chart.animateY(1000)
                    chart.invalidate()
                }
            }
        })
    }
}
