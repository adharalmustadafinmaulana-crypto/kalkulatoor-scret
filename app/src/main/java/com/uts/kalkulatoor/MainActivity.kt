package com.uts.kalkulatoor

import android.Manifest
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var tvDisplay: TextView
    private lateinit var tvExpression: TextView // Untuk menampilkan proses/riwayat hitung (tambah, kurang, dll)

    private var currentInput = ""
    private var calculationExpression = ""
    private var firstValue = 0.0
    private var currentOperator = ""
    private var isNewOperation = true

    // Sensor Manager untuk Gyroscope & Accelerometer
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Meminta izin hardware otomatis saat aplikasi dibuka
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            100
        )

        // Inisialisasi Sensor
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        tvDisplay = findViewById(R.id.tvDisplay)

        // Opsional: Jika layout Anda belum punya tvExpression, Anda bisa menambahkannya di activity_main.xml
        // Untuk amannya, kita tampilkan semua di tvDisplay atau buat teks ekspresi gabungan.

        // Inisialisasi Tombol Angka (0-9)
        val numButtons = arrayOf(
            R.id.btn0 to "0", R.id.btn1 to "1", R.id.btn2 to "2",
            R.id.btn3 to "3", R.id.btn4 to "4", R.id.btn5 to "5",
            R.id.btn6 to "6", R.id.btn7 to "7", R.id.btn8 to "8",
            R.id.btn9 to "9"
        )

        for ((id, valStr) in numButtons) {
            findViewById<Button>(id).setOnClickListener {
                if (isNewOperation) {
                    currentInput = valStr
                    isNewOperation = false
                } else {
                    currentInput += valStr
                }
                // Menampilkan angka yang sedang diketik
                tvDisplay.text = if (calculationExpression.isNotEmpty()) "$calculationExpression $currentInput" else currentInput
            }
        }

        // Tombol Operator (+, -, *, /) agar proses perhitungan terlihat jelas
        findViewById<Button>(R.id.btnAdd).setOnClickListener { setOperator("+") }
        findViewById<Button>(R.id.btnSub).setOnClickListener { setOperator("-") }
        findViewById<Button>(R.id.btnMul).setOnClickListener { setOperator("*") }
        findViewById<Button>(R.id.btnDiv).setOnClickListener { setOperator("/") }

        // Tombol Clear (C)
        findViewById<Button>(R.id.btnClear).setOnClickListener {
            currentInput = ""
            calculationExpression = ""
            firstValue = 0.0
            currentOperator = ""
            tvDisplay.text = "0"
            isNewOperation = true
        }

        // Tombol Sama Dengan (=)
        findViewById<Button>(R.id.btnEquals).setOnClickListener {
            if (currentInput.isNotEmpty() && currentOperator.isNotEmpty()) {
                val secondValue = currentInput.toDoubleOrNull() ?: 0.0
                val result = calculate(firstValue, secondValue, currentOperator)

                // Tampilkan proses lengkap di layar (Contoh: 5 + 3 = 8)
                val fullText = "$calculationExpression $currentInput = $result"
                tvDisplay.text = fullText

                isNewOperation = true
                calculationExpression = ""

                // Cek apakah hasil kalkulasi bulat
                val intResult = result.toInt()
                if (result == intResult.toDouble()) {
                    handleSecretTrigger(intResult)
                }
            }
        }
    }

    private fun setOperator(op: String) {
        if (currentInput.isNotEmpty()) {
            firstValue = currentInput.toDoubleOrNull() ?: 0.0
            currentOperator = op
            calculationExpression = "$firstValue $op"
            tvDisplay.text = calculationExpression
            currentInput = ""
            isNewOperation = false
        }
    }

    private fun calculate(v1: Double, v2: Double, op: String): Double {
        return when (op) {
            "+" -> v1 + v2
            "-" -> v1 - v2
            "*" -> v1 * v2
            "/" -> if (v2 != 0.0) v1 / v2 else 0.0
            else -> v2
        }
    }

    // Logika pemicu rahasia sesuai permintaan baru
    private fun handleSecretTrigger(result: Int) {
        when (result) {
            2 -> {
                // Hasil 2 -> KAMERA
                Toast.makeText(this, "Hasil kalkulasi 2: Membuka KAMERA!", Toast.LENGTH_SHORT).show()
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivity(intent)
            }
            4 -> {
                // Hasil 4 -> AUDIO
                Toast.makeText(this, "Hasil kalkulasi 4: Memutar AUDIO!", Toast.LENGTH_SHORT).show()
                try {
                    val mediaPlayer = MediaPlayer.create(this, R.raw.audio_secret)
                    mediaPlayer.start()
                } catch (e: Exception) {
                    Toast.makeText(this, "File audio di res/raw/audio_secret.mp3 belum ada!", Toast.LENGTH_SHORT).show()
                }
            }
            0 -> {
                // Hasil 0 -> OPEN IMAGE (Menampilkan Gambar dari drawable)
                Toast.makeText(this, "Hasil kalkulasi 0: Menampilkan GAMBAR!", Toast.LENGTH_SHORT).show()
                val imageView = ImageView(this).apply {
                    setImageResource(R.drawable.image_secret)
                    setPadding(20, 20, 20, 20)
                    adjustViewBounds = true
                }

                AlertDialog.Builder(this)
                    .setTitle("Secret Image (Hasil: 0)")
                    .setView(imageView)
                    .setPositiveButton("Tutup") { dialog, _ -> dialog.dismiss() }
                    .show()
            }
            1 -> {
                // Hasil 1 -> OPEN GPS
                Toast.makeText(this, "Hasil kalkulasi 1: Membuka GPS Maps!", Toast.LENGTH_SHORT).show()
                val gmmIntentUri = Uri.parse("geo:-7.4247,109.2300?q=STMIK+Widya+Utama+Purwokerto")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                try {
                    startActivity(mapIntent)
                } catch (e: Exception) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=STMIK+Widya+Utama+Purwokerto")))
                }
            }
            3 -> {
                // Hasil 3 -> Mengaktifkan Gyroscope dan Accelerometer
                Toast.makeText(this, "Hasil kalkulasi 3: Mengaktifkan Gyroscope & Accelerometer!", Toast.LENGTH_LONG).show()
                registerSensorListener()
            }
            else -> {
                // Kalkulator berjalan normal untuk hasil lainnya
            }
        }
    }

    private fun registerSensorListener() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        gyroscope?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            // Sensor aktif mendeteksi pergerakan
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }
}