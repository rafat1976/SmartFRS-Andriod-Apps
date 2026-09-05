package com.example.smartfrs

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Locale

class FlightListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_flight_list)

        val rvFlights = findViewById<RecyclerView>(R.id.rvFlights)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val fabSupport = findViewById<FloatingActionButton>(R.id.fabSupport)
        val btnWeatherStatus = findViewById<MaterialButton>(R.id.btnWeatherStatus)
        val cardWeatherAlert = findViewById<MaterialCardView>(R.id.cardWeatherAlert)
        val tvWeatherIcon = findViewById<TextView>(R.id.tvWeatherIcon)
        val tvWeatherTitle = findViewById<TextView>(R.id.tvWeatherTitle)
        val tvWeatherDesc = findViewById<TextView>(R.id.tvWeatherDesc)

        rvFlights.layoutManager = LinearLayoutManager(this)

        btnBack?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        fabSupport?.setOnClickListener {
            startActivity(Intent(this, SupportActivity::class.java))
        }

        @Suppress("UNCHECKED_CAST")
        val flightList = intent.getSerializableExtra("FLIGHT_LIST") as? ArrayList<Flight>
        val fromCity = intent.getStringExtra("FROM_CITY") ?: "Dhaka"
        val toCity = intent.getStringExtra("TO_CITY") ?: "Cox's Bazar"
        val travelDate = intent.getStringExtra("TRAVEL_DATE") ?: "2026-08-10"
        val flightClass = intent.getStringExtra("FLIGHT_CLASS") ?: "Economy"

        // Update Weather Alert Banner based on Destination City
        updateWeatherBanner(toCity, tvWeatherIcon, tvWeatherTitle, tvWeatherDesc, cardWeatherAlert)

        val openWeatherAction = {
            showBangladeshWeatherDialog(toCity)
        }

        btnWeatherStatus?.setOnClickListener { openWeatherAction() }
        cardWeatherAlert?.setOnClickListener { openWeatherAction() }

        if (flightList != null && flightList.isNotEmpty()) {
            val adapter = FlightAdapter(flightList, fromCity, toCity, travelDate, flightClass)
            rvFlights.adapter = adapter
        } else {
            Toast.makeText(this, "No flights available for this route.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateWeatherBanner(
        toCity: String,
        tvIcon: TextView?,
        tvTitle: TextView?,
        tvDesc: TextView?,
        card: MaterialCardView?
    ) {
        when (toCity.lowercase(Locale.ROOT)) {
            "cox's bazar", "chattogram" -> {
                tvIcon?.text = "🌧️"
                tvTitle?.text = "Weather Alert: $toCity (Monsoon Rain)"
                tvDesc?.text = "Heavy rain expected. Moderate delay risk (15-20 mins)."
                card?.setCardBackgroundColor(Color.parseColor("#FFF3CD"))
            }
            "sylhet" -> {
                tvIcon?.text = "⛈️"
                tvTitle?.text = "Weather Alert: $toCity (Thunderstorm)"
                tvDesc?.text = "Storm warning! High delay risk (30+ mins expected)."
                card?.setCardBackgroundColor(Color.parseColor("#F8D7DA"))
            }
            else -> {
                tvIcon?.text = "☀️"
                tvTitle?.text = "Weather Update: $toCity (Clear Sky)"
                tvDesc?.text = "Sky is clear! All flights running on schedule."
                card?.setCardBackgroundColor(Color.parseColor("#D4EDDA"))
            }
        }
    }

    private fun showBangladeshWeatherDialog(selectedCity: String) {
        val weatherInfo = """
            🇧🇩 BANGLADESH AIRPORT WEATHER STATUS:

            📍 Dhaka (DAC): ☀️ 31°C | Clear Sky
               ➔ Status: Flights Operating Normally ✅

            📍 Cox's Bazar (CXB): 🌧️ 28°C | Heavy Rain
               ➔ Status: Potential Delay (15-20 Mins) ⚠️

            📍 Chattogram (CGP): ☁️ 29°C | Mostly Cloudy
               ➔ Status: On Time ✅

            📍 Sylhet (ZYL): ⚡ 26°C | Thunderstorm Alert
               ➔ Status: High Delay Risk (30+ Mins) 🚨

            📍 Saidpur (SPD): 🌤️ 30°C | Partly Cloudy
               ➔ Status: On Time ✅

            📍 Rajshahi / Barishal: ☀️ 32°C | Sunny
               ➔ Status: Normal Operations ✅
            
            ------------------------------------------
            🎯 Your Destination: $selectedCity
        """.trimIndent()

        MaterialAlertDialogBuilder(this)
            .setTitle("🌤️ Live BD Airport Weather Intelligence")
            .setMessage(weatherInfo)
            .setPositiveButton("OK, Got It") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}