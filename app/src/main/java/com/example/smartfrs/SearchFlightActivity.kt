package com.example.smartfrs

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SearchFlightActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_search_flight)

        // System Bars Padding Adjustment
        val mainView = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.main)
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        // XML Views
        val etFrom = findViewById<AutoCompleteTextView>(R.id.etFrom)
        val etTo = findViewById<AutoCompleteTextView>(R.id.etTo)
        val etDate = findViewById<TextInputEditText>(R.id.etDate)
        val etClass = findViewById<AutoCompleteTextView>(R.id.etClass)
        val btnSearchNow = findViewById<MaterialButton>(R.id.btnSearchNow)
        val btnBack = findViewById<android.widget.ImageButton>(R.id.btnBack)

        btnBack?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Bangladeshi Places
        val cities = arrayOf("Dhaka", "Cox's Bazar", "Chattogram", "Sylhet", "Rajshahi", "Barishal", "Jashore", "Saidpur")
        val cityAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cities)
        
        etFrom.setAdapter(cityAdapter)
        etTo.setAdapter(cityAdapter)

        // Flight Classes (Economy / Business)
        val classes = arrayOf("Economy", "Business")
        val classAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, classes)
        etClass.setAdapter(classAdapter)
        etClass.setText(classes[0], false)

        // DatePicker setup
        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val calendarSelected = Calendar.getInstance()
                calendarSelected.set(selectedYear, selectedMonth, selectedDay)
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                etDate.setText(sdf.format(calendarSelected.time))
            }, year, month, day)

            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            datePickerDialog.show()
        }

        btnSearchNow?.setOnClickListener {
            val fromCity = etFrom.text.toString().trim()
            val toCity = etTo.text.toString().trim()
            val date = etDate.text.toString().trim()
            val flightClass = etClass.text.toString().trim()

            // Python Logic Validations
            if (fromCity.isEmpty() || toCity.isEmpty() || date.isEmpty() || flightClass.isEmpty()) {
                Toast.makeText(this, "Please select source, destination, date and class.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (fromCity.equals(toCity, ignoreCase = true)) {
                Toast.makeText(this, "Source and Destination cannot be the same.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            searchFlights(fromCity, toCity, date, flightClass)
        }
    }

    private fun searchFlights(fromCity: String, toCity: String, date: String, flightClass: String) {
        RetrofitClient.instance.searchFlights(fromCity, toCity, date, flightClass)
            .enqueue(object : Callback<SearchFlightResponse> {
                override fun onResponse(
                    call: Call<SearchFlightResponse>,
                    response: Response<SearchFlightResponse>
                ) {
                    val searchResponse = response.body()
                    if (response.isSuccessful && searchResponse != null && searchResponse.status && !searchResponse.flights.isNullOrEmpty()) {
                        val isEconomy = flightClass.equals("Economy", ignoreCase = true)
                        val processedFlights = searchResponse.flights.map { flight ->
                            val finalPrice = if (isEconomy) {
                                (flight.price - 2000.0).coerceAtLeast(1500.0)
                            } else {
                                flight.price
                            }
                            flight.copy(price = finalPrice, flightClass = flightClass)
                        }
                        launchFlightList(fromCity, toCity, date, flightClass, ArrayList(processedFlights))
                    } else {
                        // Fallback local flight generator matching route
                        showLocalFlights(fromCity, toCity, date, flightClass)
                    }
                }

                override fun onFailure(call: Call<SearchFlightResponse>, t: Throwable) {
                    showLocalFlights(fromCity, toCity, date, flightClass)
                }
            })
    }

    private fun showLocalFlights(fromCity: String, toCity: String, date: String, flightClass: String) {
        val isEconomy = flightClass.equals("Economy", ignoreCase = true)
        val localFlights = arrayListOf(
            Flight("BG101", "Biman Bangladesh", "08:00 AM", "09:00 AM", if (isEconomy) 2500.0 else 4500.0, flightClass),
            Flight("US202", "US-Bangla Airlines", "11:30 AM", "12:30 PM", if (isEconomy) 3200.0 else 5200.0, flightClass),
            Flight("VQ303", "Novoair", "02:00 PM", "03:00 PM", if (isEconomy) 3000.0 else 5000.0, flightClass),
            Flight("BG105", "Biman Bangladesh", "07:45 PM", "08:45 PM", if (isEconomy) 3500.0 else 5500.0, flightClass)
        )
        launchFlightList(fromCity, toCity, date, flightClass, localFlights)
    }

    private fun launchFlightList(fromCity: String, toCity: String, date: String, flightClass: String, flights: ArrayList<Flight>) {
        val intent = Intent(this@SearchFlightActivity, FlightListActivity::class.java)
        intent.putExtra("FLIGHT_LIST", flights)
        intent.putExtra("FROM_CITY", fromCity)
        intent.putExtra("TO_CITY", toCity)
        intent.putExtra("TRAVEL_DATE", date)
        intent.putExtra("FLIGHT_CLASS", flightClass)
        startActivity(intent)
    }
}