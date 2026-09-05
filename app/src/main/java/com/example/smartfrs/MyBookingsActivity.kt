package com.example.smartfrs

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MyBookingsActivity : AppCompatActivity() {

    private lateinit var adapter: RealBookingAdapter
    private val bookedTicketsList = mutableListOf<BookedTicket>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_bookings)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        val etSearchPnr = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etSearchPnr)
        val btnSearchTicket = findViewById<MaterialButton>(R.id.btnSearchTicket)
        val cvTicketResult = findViewById<androidx.cardview.widget.CardView>(R.id.cvTicketResult)
        val tvResPnr = findViewById<TextView>(R.id.tvResPnr)
        val tvResFlight = findViewById<TextView>(R.id.tvResFlight)
        val btnCancelSearch = findViewById<MaterialButton>(R.id.btnCancelSearch)
        val fabSupport = findViewById<FloatingActionButton>(R.id.fabSupport)
        val rvBookings = findViewById<RecyclerView>(R.id.rvBookings)

        toolbar?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        fabSupport?.setOnClickListener {
            startActivity(Intent(this, SupportActivity::class.java))
        }

        loadSavedTickets()

        adapter = RealBookingAdapter(bookedTicketsList,
            onItemClick = { ticket ->
                val intent = Intent(this, TicketDetailsActivity::class.java)
                intent.putExtra("BOOKED_TICKET", ticket)
                startActivity(intent)
            },
            onCancelClick = { ticket, position ->
                showCancellationDialog(ticket, position)
            }
        )

        rvBookings.layoutManager = LinearLayoutManager(this)
        rvBookings.adapter = adapter

        btnSearchTicket.setOnClickListener {
            val query = etSearchPnr.text.toString().trim()
            val foundTicket = TicketRepository.findTicket(this, passportNo = query, ticketId = query)

            if (foundTicket != null) {
                cvTicketResult.visibility = View.VISIBLE
                tvResPnr.text = "Ticket ID: #${foundTicket.ticketId} | Seat: ${foundTicket.seatNumber}"
                tvResFlight.text = "${foundTicket.airlineName}: ${foundTicket.source} ➔ ${foundTicket.destination}"

                btnCancelSearch.setOnClickListener {
                    showCancellationDialog(foundTicket, bookedTicketsList.indexOf(foundTicket))
                }
            } else {
                cvTicketResult.visibility = View.GONE
                Toast.makeText(this, "Ticket not found with Ticket ID or Passport!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadSavedTickets() {
        bookedTicketsList.clear()
        val ticketsFromRepo = TicketRepository.getTickets(this)

        if (ticketsFromRepo.isNotEmpty()) {
            bookedTicketsList.addAll(ticketsFromRepo)
        } else {
            // Default sample ticket
            val sampleTicket = BookedTicket(
                ticketId = "TRX475159",
                passportNo = "P9876543",
                passengerName = "rafat arif",
                email = "rafat@gmail.com",
                mobile = "01712345678",
                address = "Dhaka",
                dob = "1998-05-10",
                gender = "Male",
                seatNumber = "12A",
                source = "Dhaka",
                destination = "Cox's Bazar",
                flightClass = "Business",
                travelDate = "2026-08-19",
                travelDay = "Wednesday",
                airlineName = "Biman Bangladesh Airlines",
                price = 5500.0,
                status = "PAID (Confirmed)",
                flightNumber = "BG-202",
                gate = "04",
                boardingTime = "10:20 AM",
                fromCode = "DAC",
                toCode = "CXB"
            )
            bookedTicketsList.add(sampleTicket)
            TicketRepository.saveTicket(this, sampleTicket)
        }
    }

    private fun showCancellationDialog(ticket: BookedTicket, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Cancel Ticket")
            .setMessage("Are you sure you want to cancel ticket #${ticket.ticketId} (${ticket.passengerName}, Seat: ${ticket.seatNumber})?")
            .setPositiveButton("Yes, Cancel") { _, _ ->
                val success = TicketRepository.cancelTicket(this, ticket.ticketId)
                if (success || true) {
                    if (position in bookedTicketsList.indices) {
                        bookedTicketsList[position].status = "CANCELLED"
                        adapter.notifyItemChanged(position)
                    }
                    findViewById<androidx.cardview.widget.CardView>(R.id.cvTicketResult).visibility = View.GONE
                    Toast.makeText(this, "Ticket #${ticket.ticketId} Cancelled Successfully", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("No", null)
            .show()
    }
}