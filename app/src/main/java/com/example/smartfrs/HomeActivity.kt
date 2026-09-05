package com.example.smartfrs

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        val mainView = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.main)
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        val tvWelcome = findViewById<android.widget.TextView>(R.id.tvWelcome)
        val btnSearchFlight = findViewById<MaterialButton>(R.id.btnSearchFlight)
        val btnBookingFlight = findViewById<MaterialButton>(R.id.btnBookingFlight)
        val btnCancelTicket = findViewById<MaterialButton>(R.id.btnCancelTicket)
        val btnLogout = findViewById<android.widget.ImageButton>(R.id.btnLogout)
        val fabSupport = findViewById<FloatingActionButton>(R.id.fabSupport)

        val sharedPref = getSharedPreferences("SmartFRS_Pref", MODE_PRIVATE)
        val rawName = sharedPref.getString("USER_NAME", "User") ?: "User"
        val cleanName = if (rawName.contains("@")) {
            rawName.substringBefore("@")
        } else {
            rawName
        }.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }

        tvWelcome?.text = "Welcome, $cleanName 👋"

        btnLogout?.setOnClickListener {
            sharedPref.edit().clear().apply()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        }

        fabSupport?.setOnClickListener {
            val intent = Intent(this, SupportActivity::class.java)
            startActivity(intent)
        }

        // 1. Search Flight: Opens Search Window (From, To, Date selection)
        btnSearchFlight?.setOnClickListener {
            val intent = Intent(this@HomeActivity, SearchFlightActivity::class.java)
            startActivity(intent)
        }

        // 2. Booking Flight: Opens Available Flights List for ALL Destinations!
        btnBookingFlight?.setOnClickListener {
            val allFlights = arrayListOf(
                // Dhaka ➔ Cox's Bazar
                Flight("BG-101", "Biman Bangladesh Airlines", "08:00 AM", "09:00 AM", 5200.0, "Business", source = "Dhaka", destination = "Cox's Bazar"),
                Flight("US-202", "US-Bangla Airlines", "10:30 AM", "11:30 AM", 6800.0, "Business", source = "Dhaka", destination = "Cox's Bazar"),
                Flight("VQ-303", "NOVOAIR", "02:00 PM", "03:00 PM", 4800.0, "Economy", source = "Dhaka", destination = "Cox's Bazar"),
                Flight("2A-404", "Air Astra", "05:00 PM", "06:00 PM", 4600.0, "Economy", source = "Dhaka", destination = "Cox's Bazar"),

                // Dhaka ➔ Sylhet
                Flight("BG-201", "Biman Bangladesh Airlines", "09:15 AM", "10:05 AM", 4200.0, "Economy", source = "Dhaka", destination = "Sylhet"),
                Flight("US-302", "US-Bangla Airlines", "01:00 PM", "01:50 PM", 5800.0, "Business", source = "Dhaka", destination = "Sylhet"),
                Flight("VQ-403", "NOVOAIR", "06:30 PM", "07:20 PM", 4400.0, "Economy", source = "Dhaka", destination = "Sylhet"),

                // Dhaka ➔ Chittagong
                Flight("BG-301", "Biman Bangladesh Airlines", "07:30 AM", "08:25 AM", 4500.0, "Economy", source = "Dhaka", destination = "Chittagong"),
                Flight("US-402", "US-Bangla Airlines", "11:00 AM", "11:55 AM", 6500.0, "Business", source = "Dhaka", destination = "Chittagong"),
                Flight("VQ-503", "NOVOAIR", "04:15 PM", "05:10 PM", 4600.0, "Economy", source = "Dhaka", destination = "Chittagong"),

                // Dhaka ➔ Saidpur
                Flight("BG-401", "Biman Bangladesh Airlines", "10:00 AM", "11:00 AM", 4300.0, "Economy", source = "Dhaka", destination = "Saidpur"),
                Flight("2A-502", "Air Astra", "03:30 PM", "04:30 PM", 4700.0, "Economy", source = "Dhaka", destination = "Saidpur"),

                // Dhaka ➔ Jessore
                Flight("US-501", "US-Bangla Airlines", "08:30 AM", "09:20 AM", 5600.0, "Business", source = "Dhaka", destination = "Jessore"),
                Flight("VQ-602", "NOVOAIR", "02:45 PM", "03:35 PM", 4100.0, "Economy", source = "Dhaka", destination = "Jessore"),

                // Dhaka ➔ Rajshahi
                Flight("BG-501", "Biman Bangladesh Airlines", "11:45 AM", "12:40 PM", 4000.0, "Economy", source = "Dhaka", destination = "Rajshahi"),

                // Dhaka ➔ Barisal
                Flight("BG-601", "Biman Bangladesh Airlines", "03:00 PM", "03:45 PM", 3800.0, "Economy", source = "Dhaka", destination = "Barisal")
            )

            val intent = Intent(this@HomeActivity, FlightListActivity::class.java)
            intent.putExtra("FLIGHT_LIST", allFlights)
            intent.putExtra("FROM_CITY", "Dhaka")
            intent.putExtra("TO_CITY", "All Destinations")
            intent.putExtra("TRAVEL_DATE", "2026-08-20")
            intent.putExtra("FLIGHT_CLASS", "Economy")
            startActivity(intent)
        }

        // 3. Cancel Booking: Opens Cancellation Window
        btnCancelTicket?.setOnClickListener {
            val intent = Intent(this@HomeActivity, CancelTicketActivity::class.java)
            startActivity(intent)
        }
    }
}