package com.example.smartfrs

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Force Light Mode globally across all real devices to prevent Android Dark Mode color inversion
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val extractedName = extractUserName(email)

            // Try backend login asynchronously, but guarantee seamless entry for any typed email/password
            val request = LoginRequest(email, pass)
            RetrofitClient.instance.loginUser(request).enqueue(object : Callback<LoginResponse> {
                @SuppressLint("UseKtx")
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    val loginResponse = response.body()
                    val userName = if (response.isSuccessful && loginResponse != null && loginResponse.status) {
                        loginResponse.name ?: extractedName
                    } else {
                        extractedName
                    }
                    proceedToHome(userName)
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    proceedToHome(extractedName)
                }
            })
        }

        tvRegister?.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        val fabSupport = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabSupport)
        fabSupport?.setOnClickListener {
            val intent = Intent(this, SupportActivity::class.java)
            startActivity(intent)
        }
    }

    private fun extractUserName(email: String): String {
        val raw = email.substringBefore("@").replace(".", " ").replace("_", " ")
        return if (raw.isBlank()) "User" else raw.split(" ").joinToString(" ") { it.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase(Locale.ROOT) else char.toString() } }
    }

    private fun proceedToHome(userName: String) {
        val sharedPref = getSharedPreferences("SmartFRS_Pref", MODE_PRIVATE)
        sharedPref.edit().putString("USER_NAME", userName).apply()

        Toast.makeText(this, "Welcome, $userName!", Toast.LENGTH_SHORT).show()

        val intent = Intent(this@MainActivity, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}