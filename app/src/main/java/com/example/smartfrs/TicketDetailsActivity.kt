package com.example.smartfrs

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File

class TicketDetailsActivity : AppCompatActivity() {

    private val ticketList = mutableListOf<BookedTicket>()
    private lateinit var vpBoardingPasses: ViewPager2
    private lateinit var tvPagerIndicator: TextView
    private lateinit var btnDownloadPdf: MaterialButton
    private lateinit var btnBackToHome: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticket_details)

        vpBoardingPasses = findViewById(R.id.vpBoardingPasses)
        tvPagerIndicator = findViewById(R.id.tvPagerIndicator)
        btnDownloadPdf = findViewById(R.id.btnDownloadPdf)
        btnBackToHome = findViewById(R.id.btnBackToHome)
        val fabSupport = findViewById<FloatingActionButton>(R.id.fabSupport)

        readIntentTickets()
        setupViewPager()

        btnDownloadPdf.setOnClickListener {
            val currentPos = vpBoardingPasses.currentItem
            if (currentPos in ticketList.indices) {
                val ticketToExport = ticketList[currentPos]
                val pdfFile = TicketPdfExporter.exportTicketToPdf(this, ticketToExport)

                if (pdfFile != null && pdfFile.exists()) {
                    Toast.makeText(this, "Ticket PDF downloaded successfully! 📄\nSaved to Downloads/SmartFRS", Toast.LENGTH_LONG).show()
                    openOrSharePdf(pdfFile)
                } else {
                    Toast.makeText(this, "Failed to generate PDF. Please check storage permissions.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        fabSupport?.setOnClickListener {
            startActivity(Intent(this, SupportActivity::class.java))
        }

        btnBackToHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun readIntentTickets() {
        ticketList.clear()

        val listExtra = intent.getSerializableExtra("BOOKED_TICKET_LIST") as? ArrayList<BookedTicket>
        if (!listExtra.isNullOrEmpty()) {
            ticketList.addAll(listExtra)
        } else {
            val singleTicket = intent.getSerializableExtra("BOOKED_TICKET") as? BookedTicket
            if (singleTicket != null) {
                val baseId = singleTicket.ticketId.substringBefore("-")
                val allTickets = TicketRepository.getTickets(this)
                val siblingTickets = allTickets.filter { it.ticketId.startsWith(baseId) }
                if (siblingTickets.isNotEmpty()) {
                    ticketList.addAll(siblingTickets)
                } else {
                    ticketList.add(singleTicket)
                }
            } else {
                val trxId = intent.getStringExtra("TRX_ID") ?: "TRX475159"
                val name = intent.getStringExtra("PASSENGER_NAME") ?: "rafat arif"
                val route = intent.getStringExtra("ROUTE") ?: "Dhaka to Cox's Bazar"
                val flightClass = intent.getStringExtra("CLASS") ?: "Business"
                val day = intent.getStringExtra("TRAVEL_DAY") ?: "Wednesday (2026-08-19)"

                val fallbackTicket = BookedTicket(
                    ticketId = trxId,
                    passportNo = "P9876543",
                    passengerName = name,
                    email = "rafat@gmail.com",
                    mobile = "01712345678",
                    address = "Dhaka",
                    dob = "1998-05-10",
                    gender = "Male",
                    seatNumber = "12A",
                    source = "Dhaka",
                    destination = "Cox's Bazar",
                    flightClass = flightClass,
                    travelDate = "2026-08-19",
                    travelDay = day,
                    airlineName = "Biman Bangladesh Airlines",
                    price = 5500.0,
                    status = "PAID (Confirmed)",
                    flightNumber = "BG-202",
                    gate = "04",
                    boardingTime = "10:20 AM",
                    fromCode = "DAC",
                    toCode = "CXB"
                )
                ticketList.add(fallbackTicket)
            }
        }
    }

    private fun setupViewPager() {
        val adapter = BoardingPassAdapter(ticketList)
        vpBoardingPasses.adapter = adapter

        updateIndicator(0)

        vpBoardingPasses.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateIndicator(position)
            }
        })
    }

    private fun updateIndicator(position: Int) {
        if (ticketList.isNotEmpty() && position in ticketList.indices) {
            val ticket = ticketList[position]
            if (ticketList.size > 1) {
                tvPagerIndicator.text = "Ticket ${position + 1} of ${ticketList.size} (${ticket.passengerName} - Seat: ${ticket.seatNumber})"
                btnDownloadPdf.text = "Download Ticket #${position + 1} (PDF) 📄"
            } else {
                tvPagerIndicator.text = "Seat: ${ticket.seatNumber} | Class: ${ticket.flightClass}"
                btnDownloadPdf.text = "Download Boarding Pass (PDF) 📄"
            }
        }
    }

    private fun openOrSharePdf(pdfFile: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(this, "$packageName.provider", pdfFile)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Open or View Boarding Pass PDF"))
        } catch (e: Exception) {
            // Intent view fallback or permissions
        }
    }
}