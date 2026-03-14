package com.sharmadipanshu.aistudybuddy

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.sharmadipanshu.aistudybuddy.network.RetrofitClient
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val statusText = findViewById<TextView>(R.id.ServerStatus)

        RetrofitClient.apiService.getHealth()
            .enqueue(object : Callback<Map<String, String>> {

                override fun onResponse(
                    call: Call<Map<String, String>>,
                    response: Response<Map<String, String>>
                ) {
                    val status = response.body()?.get("status")
                    statusText.text = status
                }

                override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                    statusText.text = "Server not reachable"
                }
            })
    }
}