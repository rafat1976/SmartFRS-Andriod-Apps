package com.example.smartfrs

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*

class CheckoutActivity : AppCompatActivity() {

    private lateinit var flight: Flight

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        flight = intent.getSerializableExtra("FLIGHT_DATA") as Flight
        val pnr = intent.getStringExtra("PNR") ?: ""
        val passengerName = intent.getStringExtra("PASSENGER_NAME") ?: ""
        val passengerMobile = intent.getStringExtra("PASSENGER_MOBILE") ?: ""

        val tvPnrHeader = findViewById<TextView>(R.id.tvPnrHeader)
        val tvPassengerSummary = findViewById<TextView>(R.id.tvPassengerSummary)
        val tvFlightSummary = findViewById<TextView>(R.id.tvFlightSummary)
        val tvTotalAmount = findViewById<TextView>(R.id.tvTotalAmount)
        val rgPaymentMethod = findViewById<RadioGroup>(R.id.rgPaymentMethod)
        val btnConfirmBooking = findViewById<Button>(R.id.btnConfirmBooking)

        tvPnrHeader.text = "Booking ID: #$pnr"
        tvPassengerSummary.text = "Passenger: $passengerName ($passengerMobile)"
        tvFlightSummary.text = "${flight.airlineName}: ${flight.departureTime} - ${flight.arrivalTime}"
        tvTotalAmount.text = "Total Amount: ৳ ${flight.price}"

        btnConfirmBooking.setOnClickListener {
            val selectedId = rgPaymentMethod.checkedRadioButtonId
            if (selectedId == -1) {
                Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show()
            } else {
                val radioButton = findViewById<RadioButton>(selectedId)
                processPayment(radioButton.text.toString(), pnr)
            }
        }
    }

    private fun processPayment(method: String, pnr: String) {
        val progressDialog = AlertDialog.Builder(this)
            .setTitle("$method Payment")
            .setMessage("Securely connecting to $method payment gateway...")
            .setCancelable(false)
            .create()

        progressDialog.show()

        CoroutineScope(Dispatchers.Main).launch {
            delay(3000) // Dummy 3-second delay for realistic feel
            progressDialog.dismiss()

            // Success Dialog
            val dialogView = layoutInflater.inflate(R.layout.dialog_booking_success, null)
            val tvPnr = dialogView.findViewById<TextView>(R.id.tvSuccessPnr)
            tvPnr.text = "Ticket ID: $pnr"

            val successDialog = AlertDialog.Builder(this@CheckoutActivity)
                .setView(dialogView)
                .setCancelable(false)
                .create()

            dialogView.findViewById<Button>(R.id.btnHome).setOnClickListener {
                successDialog.dismiss()
                val intent = Intent(this@CheckoutActivity, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }

            successDialog.show()
        }
    }
}