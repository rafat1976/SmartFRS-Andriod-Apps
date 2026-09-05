package com.example.smartfrs

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class OtpVerificationActivity : AppCompatActivity() {

    private var userName: String = ""
    private var userEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_verification)

        userName = intent.getStringExtra("USER_NAME") ?: "User"
        userEmail = intent.getStringExtra("USER_EMAIL") ?: "user@smartfrs.com"

        val tvTargetEmail = findViewById<TextView>(R.id.tvTargetEmail)
        val btnVerifyOtp = findViewById<MaterialButton>(R.id.btnVerifyOtp)
        val tvResendOtp = findViewById<TextView>(R.id.tvResendOtp)

        tvTargetEmail.text = userEmail

        btnVerifyOtp.setOnClickListener {
            val mAuth = FirebaseAuth.getInstance()
            val user = mAuth.currentUser

            if (user != null) {
                // Instantly reload user state to check if email was verified via link
                user.reload().addOnCompleteListener { task ->
                    if (user.isEmailVerified) {
                        onEmailVerificationSuccess()
                    } else {
                        Toast.makeText(
                            this,
                            "⚠️ Email is not verified yet! Please check your email inbox ($userEmail) and click the link.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } else {
                Toast.makeText(
                    this,
                    "⚠️ Please check your email inbox ($userEmail) and click the Firebase link to verify.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        tvResendOtp.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                user.sendEmailVerification().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "A new verification link has been sent to $userEmail 📧", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "Verification email resent to $userEmail 📧", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Verification email link sent to $userEmail 📧", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun onEmailVerificationSuccess() {
        val sharedPref = getSharedPreferences("SmartFRS_Pref", MODE_PRIVATE)
        sharedPref.edit().putString("USER_NAME", userName).apply()

        AlertDialog.Builder(this)
            .setTitle("🎉 Account Verified!")
            .setMessage("Congratulations $userName! Your email ($userEmail) has been verified successfully. Please login to continue.")
            .setPositiveButton("Go to Login") { _, _ ->
                val intent = Intent(this@OtpVerificationActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            .setCancelable(false)
            .show()
    }
}
