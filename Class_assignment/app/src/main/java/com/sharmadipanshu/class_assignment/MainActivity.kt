package com.sharmadipanshu.class_assignment

import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Locale

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var imgbulb: ImageView
    private var lightSensor: Sensor? = null
    private lateinit var tvLightValue: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        imgbulb = findViewById(R.id.imgbulb)
        tvLightValue = findViewById(R.id.tvLightValue)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        if (lightSensor == null) {
            tvLightValue.text = "Light sensor not available on this device"
        }
    }

    override fun onResume() {
        super.onResume()
        lightSensor?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val lux = event.values[0]
            tvLightValue.text = String.format(Locale.US, "Light: %.2f lx", lux)
            updateBulbBrightness(lux)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun updateBulbBrightness(lux: Float) {

        val maxLux = 1000f
        val brightness = (lux / maxLux).coerceIn(0f, 1f)

        imgbulb.animate()
            .alpha(0.2f + brightness * 0.8f)
            .scaleX(1f + brightness * 0.3f)
            .scaleY(1f + brightness * 0.3f)
            .setDuration(300)
            .start()
        val glowIntensity = (brightness * 255).toInt()
        imgbulb.setColorFilter(
            Color.argb(glowIntensity, 255, 223, 0)
        )

    }
}