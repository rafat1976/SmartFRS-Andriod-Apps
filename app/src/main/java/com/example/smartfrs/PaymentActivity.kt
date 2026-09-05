package com.example.smartfrs

import android.app.ProgressDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.*
import kotlin.random.Random

class PaymentActivity : AppCompatActivity() {

    private lateinit var rgPaymentMethods: RadioGroup
    private lateinit var rbBkash: RadioButton
    private lateinit var rbNagad: RadioButton
    private lateinit var rbBank: RadioButton

    private lateinit var layoutBkashForm: LinearLayout
    private lateinit var layoutNagadForm: LinearLayout
    private lateinit var layoutBankForm: LinearLayout

    private lateinit var etBkashNumber: TextInputEditText
    private lateinit var etBkashPin: TextInputEditText

    private lateinit var etNagadNumber: TextInputEditText
    private lateinit var etNagadPin: TextInputEditText

    private lateinit var etCardNumber: TextInputEditText
    private lateinit var etCvcNumber: TextInputEditText
    private lateinit var etCardHolderName: TextInputEditText
    private lateinit var etBankPin: TextInputEditText
    private lateinit var tvCardTypeDetector: TextView

    private lateinit var btnPayNow: MaterialButton
    private lateinit var tvPnrDisplay: TextView
    private lateinit var tvTotalAmount: TextView

    private var pnr: String = ""
    private var firstName: String = ""
    private var lastName: String = ""
    private var passengerName: String = ""
    private var email: String = ""
    private var mobile: String = ""
    private var passportNo: String = ""
    private var address: String = ""
    private var dob: String = ""
    private var gender: String = "Male"
    private var seatNumber: String = "12A (Window)"
    private var fromCity: String = ""
    private var toCity: String = ""
    private var travelDate: String = ""
    private var flightClass: String = ""
    private var amount: Double = 5200.0
    private var airlineName: String = "Biman Bangladesh Airlines"

    private var detectedCardType: String = "Bank Card"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        val fabSupport = findViewById<FloatingActionButton>(R.id.fabSupport)

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        fabSupport?.setOnClickListener {
            startActivity(Intent(this, SupportActivity::class.java))
        }

        initViews()
        readIntentData()
        setupMethodToggling()
        setupCardAutoDetector()

        btnPayNow.setOnClickListener {
            processPayment()
        }
    }

    private fun initViews() {
        rgPaymentMethods = findViewById(R.id.rgPaymentMethods)
        rbBkash = findViewById(R.id.rbBkash)
        rbNagad = findViewById(R.id.rbNagad)
        rbBank = findViewById(R.id.rbBank)

        layoutBkashForm = findViewById(R.id.layoutBkashForm)
        layoutNagadForm = findViewById(R.id.layoutNagadForm)
        layoutBankForm = findViewById(R.id.layoutBankForm)

        etBkashNumber = findViewById(R.id.etBkashNumber)
        etBkashPin = findViewById(R.id.etBkashPin)

        etNagadNumber = findViewById(R.id.etNagadNumber)
        etNagadPin = findViewById(R.id.etNagadPin)

        etCardNumber = findViewById(R.id.etCardNumber)
        etCvcNumber = findViewById(R.id.etCvcNumber)
        etCardHolderName = findViewById(R.id.etCardHolderName)
        etBankPin = findViewById(R.id.etBankPin)
        tvCardTypeDetector = findViewById(R.id.tvCardTypeDetector)

        btnPayNow = findViewById(R.id.btnPayNow)
        tvPnrDisplay = findViewById(R.id.tvPnrDisplay)
        tvTotalAmount = findViewById(R.id.tvTotalAmount)
    }

    private fun readIntentData() {
        val flightData = intent.getSerializableExtra("FLIGHT_DATA") as? Flight
        pnr = intent.getStringExtra("PNR") ?: generateRandomPnr()
        firstName = intent.getStringExtra("FIRST_NAME") ?: ""
        lastName = intent.getStringExtra("LAST_NAME") ?: ""
        passengerName = intent.getStringExtra("PASSENGER_NAME") ?: "$firstName $lastName".trim().ifEmpty { "Passenger" }
        email = intent.getStringExtra("PASSENGER_EMAIL") ?: ""
        mobile = intent.getStringExtra("PASSENGER_MOBILE") ?: ""
        passportNo = intent.getStringExtra("PASSPORT_NO") ?: "P1234567"
        address = intent.getStringExtra("PASSENGER_ADDRESS") ?: ""
        dob = intent.getStringExtra("PASSENGER_DOB") ?: ""
        gender = intent.getStringExtra("GENDER") ?: "Male"
        seatNumber = intent.getStringExtra("SEAT_NUMBER") ?: "12A (Window)"
        fromCity = intent.getStringExtra("FROM_CITY") ?: "Dhaka"
        toCity = intent.getStringExtra("TO_CITY") ?: "Sylhet"
        travelDate = intent.getStringExtra("TRAVEL_DATE") ?: "2026-08-10"
        flightClass = intent.getStringExtra("FLIGHT_CLASS") ?: flightData?.flightClass ?: "Economy"
        amount = intent.getDoubleExtra("AMOUNT", flightData?.price ?: 5200.0)
        airlineName = flightData?.airlineName ?: "Biman Bangladesh Airlines"

        tvPnrDisplay.text = "Booking PNR: #$pnr | Seat: $seatNumber"
        tvTotalAmount.text = "৳ ${String.format("%.2f", amount)}"
    }

    private fun setupMethodToggling() {
        rgPaymentMethods.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbBkash -> {
                    layoutBkashForm.visibility = View.VISIBLE
                    layoutNagadForm.visibility = View.GONE
                    layoutBankForm.visibility = View.GONE
                    btnPayNow.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E2136E"))
                    btnPayNow.text = "Pay ৳${String.format("%.2f", amount)} via bKash"
                }
                R.id.rbNagad -> {
                    layoutBkashForm.visibility = View.GONE
                    layoutNagadForm.visibility = View.VISIBLE
                    layoutBankForm.visibility = View.GONE
                    btnPayNow.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F7941D"))
                    btnPayNow.text = "Pay ৳${String.format("%.2f", amount)} via Nagad"
                }
                R.id.rbBank -> {
                    layoutBkashForm.visibility = View.GONE
                    layoutNagadForm.visibility = View.GONE
                    layoutBankForm.visibility = View.VISIBLE
                    btnPayNow.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#1A1A2E"))
                    btnPayNow.text = "Pay ৳${String.format("%.2f", amount)} via Bank Card"
                }
            }
        }

        rbBkash.isChecked = true
        btnPayNow.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E2136E"))
        btnPayNow.text = "Pay ৳${String.format("%.2f", amount)} via bKash"
    }

    private fun setupCardAutoDetector() {
        etCardNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val cardNo = s?.toString()?.trim() ?: ""
                when {
                    cardNo.startsWith("4") || cardNo.startsWith("41787") -> {
                        detectedCardType = "VISA"
                        tvCardTypeDetector.text = "💳 VISA Card"
                        tvCardTypeDetector.setBackgroundColor(Color.parseColor("#1976D2"))
                    }
                    cardNo.startsWith("5") || cardNo.startsWith("521") || cardNo.startsWith("51") || cardNo.startsWith("52") || cardNo.startsWith("53") || cardNo.startsWith("54") || cardNo.startsWith("55") -> {
                        detectedCardType = "MasterCard"
                        tvCardTypeDetector.text = "💳 MasterCard"
                        tvCardTypeDetector.setBackgroundColor(Color.parseColor("#FF6F00"))
                    }
                    else -> {
                        detectedCardType = "Bank Card"
                        tvCardTypeDetector.text = "💳 Bank Card"
                        tvCardTypeDetector.setBackgroundColor(Color.parseColor("#3F51B5"))
                    }
                }
            }
        })
    }

    private fun generateRandomPnr(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }

    private fun processPayment() {
        val checkedMethodId = rgPaymentMethods.checkedRadioButtonId
        var selectedMethod = "bKash"

        when (checkedMethodId) {
            R.id.rbBkash -> {
                selectedMethod = "bKash"
                val bKashNo = etBkashNumber.text.toString().trim()
                val bKashPin = etBkashPin.text.toString().trim()

                if (bKashNo.isEmpty() || bKashPin.isEmpty()) {
                    Toast.makeText(this, "Please enter bKash Mobile Number and PIN", Toast.LENGTH_SHORT).show()
                    return
                }

                if (!Regex("^01[3-9]\\d{8}$").matches(bKashNo)) {
                    Toast.makeText(this, "Invalid bKash BD Mobile Number", Toast.LENGTH_SHORT).show()
                    return
                }
            }
            R.id.rbNagad -> {
                selectedMethod = "Nagad"
                val nagadNo = etNagadNumber.text.toString().trim()
                val nagadPin = etNagadPin.text.toString().trim()

                if (nagadNo.isEmpty() || nagadPin.isEmpty()) {
                    Toast.makeText(this, "Please enter Nagad Mobile Number and PIN", Toast.LENGTH_SHORT).show()
                    return
                }

                if (!Regex("^01[3-9]\\d{8}$").matches(nagadNo)) {
                    Toast.makeText(this, "Invalid Nagad BD Mobile Number", Toast.LENGTH_SHORT).show()
                    return
                }
            }
            R.id.rbBank -> {
                selectedMethod = "Bank ($detectedCardType)"
                val cardNo = etCardNumber.text.toString().trim()
                val cvc = etCvcNumber.text.toString().trim()
                val cardHolder = etCardHolderName.text.toString().trim()
                val bankPin = etBankPin.text.toString().trim()

                if (cardNo.isEmpty() || cvc.isEmpty() || cardHolder.isEmpty() || bankPin.isEmpty()) {
                    Toast.makeText(this, "Please fill Card Number, CVC, Cardholder Name, and PIN", Toast.LENGTH_SHORT).show()
                    return
                }

                if (cardNo.length < 4) {
                    Toast.makeText(this, "Please enter a valid Card Number", Toast.LENGTH_SHORT).show()
                    return
                }
            }
        }

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Processing $selectedMethod payment of ৳$amount...\nPlease do not close the app.")
        progressDialog.setCancelable(false)
        progressDialog.show()

        CoroutineScope(Dispatchers.Main).launch {
            delay(300)
            progressDialog.dismiss()

            val selectedSeatsList = intent.getStringArrayListExtra("SELECTED_SEATS_ARRAY") ?: arrayListOf()
            val passengerNames = intent.getStringArrayListExtra("PASSENGER_NAMES_LIST") ?: arrayListOf()
            val passengerNids = intent.getStringArrayListExtra("PASSENGER_NIDS_LIST") ?: arrayListOf()
            val passengerGenders = intent.getStringArrayListExtra("PASSENGER_GENDERS_LIST") ?: arrayListOf()
            val passengerDobs = intent.getStringArrayListExtra("PASSENGER_DOBS_LIST") ?: arrayListOf()

            val seatsToProcess = if (selectedSeatsList.isNotEmpty()) selectedSeatsList else seatNumber.split(",").map { it.trim() }

            val baseTrxId = "TRX" + Random.nextInt(100000, 999999)
            val isMfsOfferEligible = seatsToProcess.size >= 4 && (selectedMethod.contains("bKash", ignoreCase = true) || selectedMethod.contains("Nagad", ignoreCase = true))
            val finalAmount = if (isMfsOfferEligible) amount * 0.90 else amount
            val unitPrice = if (seatsToProcess.isNotEmpty()) finalAmount / seatsToProcess.size else finalAmount

            val createdTickets = mutableListOf<BookedTicket>()

            for ((index, seat) in seatsToProcess.withIndex()) {
                val ticketId = if (seatsToProcess.size > 1) "$baseTrxId-${index + 1}" else baseTrxId
                val cleanSeat = seat.replace("(Window)", "").replace("(Aisle)", "").trim()

                val currentPassengerName = passengerNames.getOrNull(index) ?: passengerName
                val currentNid = passengerNids.getOrNull(index) ?: passportNo
                val currentGender = passengerGenders.getOrNull(index) ?: gender
                val currentDob = passengerDobs.getOrNull(index) ?: dob
                val currentFirstName = currentPassengerName.substringBefore(" ", firstName)
                val currentLastName = currentPassengerName.substringAfter(" ", lastName)

                val ticket = BookedTicket(
                    ticketId = ticketId,
                    passportNo = currentNid,
                    firstName = currentFirstName,
                    lastName = currentLastName,
                    passengerName = currentPassengerName,
                    email = email,
                    mobile = mobile,
                    address = address,
                    dob = currentDob,
                    gender = currentGender,
                    seatNumber = cleanSeat,
                    source = fromCity,
                    destination = toCity,
                    flightClass = flightClass,
                    travelDate = travelDate,
                    travelDay = getDayOfWeek(travelDate),
                    airlineName = airlineName,
                    price = unitPrice,
                    status = "PAID (Confirmed)",
                    flightNumber = "BG-${Random.nextInt(100, 999)}",
                    gate = String.format("%02d", Random.nextInt(1, 12)),
                    boardingTime = "10:20 AM",
                    fromCode = getCityCode(fromCity),
                    toCode = getCityCode(toCity)
                )
                createdTickets.add(ticket)
            }

            TicketRepository.saveTickets(this@PaymentActivity, createdTickets)

            var summaryMsg = if (createdTickets.size > 1) {
                "Total ${createdTickets.size} Tickets Issued! ✅\nBase Booking ID: #$baseTrxId\nPassenger: $passengerName\nSeats: ${seatsToProcess.joinToString(", ")}\nPayment Method: $selectedMethod\nTotal Paid: ৳${String.format("%.2f", finalAmount)}"
            } else {
                "Transaction ID: ${createdTickets.first().ticketId}\nPassenger: $passengerName\nSeat: ${createdTickets.first().seatNumber}\nPayment Method: $selectedMethod\nAmount Paid: ৳${String.format("%.2f", finalAmount)}\n\nStatus: Confirmed"
            }

            if (isMfsOfferEligible) {
                summaryMsg += "\n\n🎉 10% Instant Discount Applied ($selectedMethod 4-Seat Offer)!"
            }

            if (selectedMethod.contains("Bank", ignoreCase = true) || selectedMethod.contains("Card", ignoreCase = true)) {
                summaryMsg += "\n\n💳 Complimentary Airport Lounge Access Pass Unlocked!"
            }

            AlertDialog.Builder(this@PaymentActivity)
                .setTitle("Payment Successful! ✅")
                .setMessage(summaryMsg)
                .setPositiveButton("View Boarding Passes") { _, _ ->
                    val intent = Intent(this@PaymentActivity, TicketDetailsActivity::class.java)
                    intent.putExtra("BOOKED_TICKET_LIST", ArrayList(createdTickets))
                    intent.putExtra("BOOKED_TICKET", createdTickets.first())
                    startActivity(intent)
                    finish()
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun getCityCode(cityName: String): String {
        return when (cityName.trim().lowercase()) {
            "dhaka" -> "DAC"
            "sylhet" -> "ZYL"
            "chittagong", "chattogram" -> "CGP"
            "cox's bazar", "coxs bazar", "cox" -> "CXB"
            "saidpur" -> "SPD"
            "jessore" -> "JSR"
            "rajshahi" -> "RJH"
            "barisal" -> "BZL"
            else -> cityName.take(3).uppercase()
        }
    }

    private fun getDayOfWeek(dateStr: String): String {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            val date = sdf.parse(dateStr)
            val daySdf = java.text.SimpleDateFormat("EEEE", java.util.Locale.US)
            if (date != null) daySdf.format(date) else "Monday"
        } catch (e: Exception) {
            "Monday"
        }
    }
}