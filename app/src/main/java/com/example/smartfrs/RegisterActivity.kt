package com.example.smartfrs

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "সবগুলো ফিল্ড পূরণ করুন", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Regex("[^@]+@[^@]+\\.[^@]+").matches(email)) {
                Toast.makeText(this, "সঠিক ইমেইল এড্রেস প্রদান করুন", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass.length < 6) {
                Toast.makeText(this, "পাসওয়ার্ড অন্তত ৬ অক্ষরের হতে হবে", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = RegisterRequest(name, email, phone, pass)

            RetrofitClient.instance.registerUser(request).enqueue(object : Callback<RegisterResponse> {
                override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                    val registerResponse = response.body()

                    if (response.isSuccessful && registerResponse != null && registerResponse.status) {
                        val sharedPref = getSharedPreferences("SmartFRS_Pref", MODE_PRIVATE)
                        sharedPref.edit().putString("USER_NAME", name).apply()

                        // Trigger Firebase Email Verification
                        triggerFirebaseEmailVerification(name, email, pass)
                    } else {
                        // Fallback: If local Flask register passes, still trigger Firebase verification
                        triggerFirebaseEmailVerification(name, email, pass)
                    }
                }

                override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                    // Fallback to Firebase Verification
                    triggerFirebaseEmailVerification(name, email, pass)
                }
            })
        }

        tvLogin?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        val fabSupport = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabSupport)
        fabSupport?.setOnClickListener {
            val intent = Intent(this, SupportActivity::class.java)
            startActivity(intent)
        }
    }

    private fun triggerFirebaseEmailVerification(name: String, email: String, pass: String) {
        val generatedOtp = String.format("%06d", kotlin.random.Random.nextInt(100000, 999999))
        try {
            val mAuth = FirebaseAuth.getInstance()
            mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                val user = mAuth.currentUser
                user?.sendEmailVerification()

                val intent = Intent(this, OtpVerificationActivity::class.java)
                intent.putExtra("USER_NAME", name)
                intent.putExtra("USER_EMAIL", email)
                intent.putExtra("USER_PASS", pass)
                intent.putExtra("GENERATED_OTP", generatedOtp)
                startActivity(intent)
                finish()
            }
        } catch (e: Exception) {
            val intent = Intent(this, OtpVerificationActivity::class.java)
            intent.putExtra("USER_NAME", name)
            intent.putExtra("USER_EMAIL", email)
            intent.putExtra("USER_PASS", pass)
            intent.putExtra("GENERATED_OTP", generatedOtp)
            startActivity(intent)
            finish()
        }
    }
}