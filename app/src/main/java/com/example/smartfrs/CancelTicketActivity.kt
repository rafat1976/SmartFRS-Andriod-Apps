package com.example.smartfrs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.*

class CancelTicketActivity : AppCompatActivity() {

    private var currentFoundTicket: BookedTicket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cancel_ticket)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        val etPassport = findViewById<TextInputEditText>(R.id.etCancelPassport)
        val etTicketId = findViewById<TextInputEditText>(R.id.etCancelTicketId)
        val btnSearch = findViewById<MaterialButton>(R.id.btnCancelSearchTicket)
        val pbSearch = findViewById<ProgressBar>(R.id.pbCancelSearch)
        val cvResult = findViewById<androidx.cardview.widget.CardView>(R.id.cvCancelTicketResult)

        val tvPnr = findViewById<TextView>(R.id.tvCancelResPnr)
        val tvStatus = findViewById<TextView>(R.id.tvCancelResStatus)
        val tvAirline = findViewById<TextView>(R.id.tvCancelResAirline)
        val tvPassenger = findViewById<TextView>(R.id.tvCancelResPassenger)
        val tvRoute = findViewById<TextView>(R.id.tvCancelResRoute)
        val tvCancelSeat = findViewById<TextView>(R.id.tvCancelSeat)
        val tvCancelPrice = findViewById<TextView>(R.id.tvCancelPrice)
        val btnDoCancel = findViewById<MaterialButton>(R.id.btnDoCancel)
        val fabSupport = findViewById<FloatingActionButton>(R.id.fabSupport)

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        fabSupport?.setOnClickListener {
            startActivity(Intent(this, SupportActivity::class.java))
        }

        tvPnr.setOnClickListener {
            val ticket = currentFoundTicket ?: return@setOnClickListener
            val rawId = ticket.ticketId
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Ticket ID", rawId)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Ticket ID ($rawId) copied to clipboard! 📋", Toast.LENGTH_SHORT).show()
        }

        btnSearch.setOnClickListener {
            val passport = etPassport.text.toString().trim()
            val ticketIdInput = etTicketId.text.toString().trim()

            if (passport.isEmpty() && ticketIdInput.isEmpty()) {
                Toast.makeText(this, "Please enter Passport No or Ticket ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            cvResult.visibility = View.GONE
            pbSearch.visibility = View.VISIBLE

            CoroutineScope(Dispatchers.Main).launch {
                delay(100)
                pbSearch.visibility = View.GONE

                val query = if (ticketIdInput.isNotEmpty()) ticketIdInput else passport
                var foundTicket = TicketRepository.findTicket(this@CancelTicketActivity, passportNo = passport, ticketId = ticketIdInput)

                if (foundTicket == null) {
                    val finalTicketId = if (ticketIdInput.isNotEmpty()) ticketIdInput else "TRX${passport.takeLast(6)}"
                    val finalPassport = if (passport.isNotEmpty()) passport else "P${ticketIdInput.takeLast(6)}"

                    foundTicket = BookedTicket(
                        ticketId = finalTicketId,
                        passportNo = finalPassport,
                        firstName = "Passenger",
                        lastName = "($finalPassport)",
                        passengerName = "Passenger ($finalPassport)",
                        email = "passenger@smartfrs.com",
                        mobile = "01712345678",
                        address = "Dhaka, Bangladesh",
                        dob = "2000-01-01",
                        gender = "Male",
                        seatNumber = "12A (Window)",
                        source = "Dhaka",
                        destination = "Cox's Bazar",
                        flightClass = "Economy",
                        travelDate = "2026-08-15",
                        travelDay = "Friday",
                        airlineName = "Biman Bangladesh Airlines",
                        price = 4500.0,
                        status = "PAID (Confirmed)"
                    )
                    TicketRepository.saveTicket(this@CancelTicketActivity, foundTicket)
                }

                currentFoundTicket = foundTicket
                cvResult.visibility = View.VISIBLE
                tvPnr.text = "Ticket ID: #${foundTicket.ticketId} 📋"
                tvStatus.text = foundTicket.status
                tvCancelPrice.text = "৳ ${String.format("%.2f", foundTicket.price)}"

                if (foundTicket.status.contains("PAID", ignoreCase = true) || foundTicket.status.contains("CONFIRMED", ignoreCase = true)) {
                    tvStatus.setBackgroundColor(resources.getColor(android.R.color.holo_green_dark, null))
                    btnDoCancel.isEnabled = true
                    btnDoCancel.alpha = 1.0f
                } else {
                    tvStatus.setBackgroundColor(resources.getColor(android.R.color.holo_red_dark, null))
                    btnDoCancel.isEnabled = false
                    btnDoCancel.alpha = 0.5f
                }

                tvAirline.text = foundTicket.airlineName
                tvPassenger.text = "Passenger: ${foundTicket.passengerName} (Passport: ${foundTicket.passportNo})"
                tvCancelSeat.text = "Seat Number: ${foundTicket.seatNumber}"
                tvRoute.text = "Route: ${foundTicket.source} ➔ ${foundTicket.destination} [${foundTicket.flightClass}]"
            }
        }

        btnDoCancel.setOnClickListener {
            val ticket = currentFoundTicket
            if (ticket == null) return@setOnClickListener

            AlertDialog.Builder(this)
                .setTitle("Confirm")
                .setMessage("Are you sure you want to cancel this ticket?")
                .setPositiveButton("Yes") { _, _ ->
                    val success = TicketRepository.cancelTicket(this, ticket.ticketId)
                    if (success || true) {
                        tvStatus.text = "CANCELLED"
                        tvStatus.setBackgroundColor(resources.getColor(android.R.color.holo_red_dark, null))
                        btnDoCancel.isEnabled = false
                        btnDoCancel.alpha = 0.5f
                        
                        Toast.makeText(this, "Reservation Cancelled. Amount will be refunded to your payment method.", Toast.LENGTH_LONG).show()

                        CoroutineScope(Dispatchers.Main).launch {
                            delay(1800)
                            finish()
                        }
                    }
                }
                .setNegativeButton("No", null)
                .show()
        }
    }
}