package com.example.smartfrs

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

import androidx.appcompat.app.AlertDialog

data class PassengerFormHolder(
    val index: Int,
    val typeLabel: String,
    val etFirstName: TextInputEditText,
    val etLastName: TextInputEditText,
    val etNid: TextInputEditText,
    val etDob: TextInputEditText,
    val etGender: TextInputEditText,
    val etEmail: TextInputEditText? = null,
    val etMobile: TextInputEditText? = null,
    val etAddress: TextInputEditText? = null
)

class PassengerInfoActivity : AppCompatActivity() {

    private lateinit var flight: Flight
    private var fromCity: String = ""
    private var toCity: String = ""
    private var travelDate: String = ""
    private var flightClass: String = ""

    // Passenger Count Management (Max 4 Total per booking)
    private var adultCount: Int = 1
    private var childCount: Int = 0
    private var infantCount: Int = 0
    private val maxPassengers: Int = 4

    private lateinit var tvAdultCount: TextView
    private lateinit var tvChildCount: TextView
    private lateinit var tvInfantCount: TextView
    private lateinit var tvSelectedSeatsSummary: TextView
    private lateinit var tvCalculatedFare: TextView
    private lateinit var layoutPassengersContainer: LinearLayout
    private lateinit var etClass: AutoCompleteTextView

    private val passengerHolders = mutableListOf<PassengerFormHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_passenger_info)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        val fabSupport = findViewById<FloatingActionButton>(R.id.fabSupport)

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        fabSupport?.setOnClickListener {
            startActivity(Intent(this, SupportActivity::class.java))
        }

        flight = intent.getSerializableExtra("FLIGHT_DATA") as Flight
        fromCity = intent.getStringExtra("FROM_CITY") ?: "Dhaka"
        toCity = intent.getStringExtra("TO_CITY") ?: "Sylhet"
        travelDate = intent.getStringExtra("TRAVEL_DATE") ?: "2026-08-10"
        flightClass = intent.getStringExtra("FLIGHT_CLASS") ?: flight.flightClass ?: "Economy"

        tvAdultCount = findViewById(R.id.tvAdultCount)
        tvChildCount = findViewById(R.id.tvChildCount)
        tvInfantCount = findViewById(R.id.tvInfantCount)
        tvSelectedSeatsSummary = findViewById(R.id.tvSelectedSeatsSummary)
        tvCalculatedFare = findViewById(R.id.tvCalculatedFare)
        layoutPassengersContainer = findViewById(R.id.layoutPassengersContainer)
        etClass = findViewById(R.id.etClass)

        val btnProceed = findViewById<MaterialButton>(R.id.btnProceedToPayment)

        val classes = arrayOf("Economy", "Business")
        val classAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, classes)
        etClass.setAdapter(classAdapter)
        etClass.setText(flightClass, false)
        etClass.setOnItemClickListener { _, _, _, _ ->
            updateFareSummary()
        }

        setupPassengerCounterButtons()
        renderPassengerForms()
        updateFareSummary()

        btnProceed.setOnClickListener {
            val totalPassengers = getTotalPassengers()
            if (totalPassengers <= 0) {
                Toast.makeText(this, "Please select at least 1 passenger.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (passengerHolders.isEmpty()) {
                Toast.makeText(this, "Passenger forms not loaded.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val passengerNamesList = ArrayList<String>()
            val passengerNidsList = ArrayList<String>()
            val passengerGendersList = ArrayList<String>()
            val passengerDobsList = ArrayList<String>()

            var leadEmail = ""
            var leadMobile = ""
            var leadAddress = ""
            var leadFirstName = ""
            var leadLastName = ""

            for (holder in passengerHolders) {
                val fName = holder.etFirstName.text.toString().trim()
                val lName = holder.etLastName.text.toString().trim()
                val nid = holder.etNid.text.toString().trim()
                val dob = holder.etDob.text.toString().trim()
                val gender = holder.etGender.text.toString().trim()

                if (fName.isEmpty() || lName.isEmpty() || nid.isEmpty() || dob.isEmpty() || gender.isEmpty()) {
                    Toast.makeText(this, "Please fill out all details for Passenger ${holder.index + 1} (${holder.typeLabel})", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                val fullName = "$fName $lName".trim()
                passengerNamesList.add(fullName)
                passengerNidsList.add(nid)
                passengerGendersList.add(gender)
                passengerDobsList.add(dob)

                if (holder.index == 0) {
                    leadFirstName = fName
                    leadLastName = lName
                    leadEmail = holder.etEmail?.text?.toString()?.trim() ?: ""
                    leadMobile = holder.etMobile?.text?.toString()?.trim() ?: ""
                    leadAddress = holder.etAddress?.text?.toString()?.trim() ?: ""

                    if (leadEmail.isEmpty() || leadMobile.isEmpty() || leadAddress.isEmpty()) {
                        Toast.makeText(this, "Contact details (Email, Mobile, Address) are required for Lead Passenger 1.", Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }

                    if (!Regex("[^@]+@[^@]+\\.[^@]+").matches(leadEmail)) {
                        Toast.makeText(this, "Invalid Lead Passenger Email Address", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    if (!Regex("^01[3-9]\\d{8}$").matches(leadMobile)) {
                        Toast.makeText(this, "Invalid Lead Passenger Mobile Number (e.g. 01712345678)", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                }
            }

            val pnr = generatePNR()
            val chosenClass = etClass.text.toString().trim()
            val unitPrice = getUnitPriceForChosenClass(chosenClass)
            val totalAmount = (adultCount * unitPrice) + (childCount * unitPrice * 0.75) + (infantCount * unitPrice * 0.20)

            // Auto-assign seat numbers for all passengers (Adults, Children, Infants)
            val totalPassengerCount = getTotalPassengers()
            val generatedSeats = autoGenerateSeatNumbers(totalPassengerCount, infantCount)
            val seatString = generatedSeats.joinToString(", ")

            val updatedFlight = flight.copy(price = unitPrice, flightClass = chosenClass)
            val mainPassengerName = if (passengerNamesList.size > 1) {
                "${passengerNamesList.first()} & ${passengerNamesList.size - 1} others"
            } else {
                passengerNamesList.firstOrNull() ?: "Passenger"
            }

            val intent = Intent(this, PaymentActivity::class.java)
            intent.putExtra("FLIGHT_DATA", updatedFlight)
            intent.putExtra("PNR", pnr)
            intent.putExtra("FIRST_NAME", leadFirstName)
            intent.putExtra("LAST_NAME", leadLastName)
            intent.putExtra("PASSENGER_NAME", mainPassengerName)
            intent.putExtra("PASSENGER_EMAIL", leadEmail)
            intent.putExtra("PASSENGER_MOBILE", leadMobile)
            intent.putExtra("PASSPORT_NO", passengerNidsList.firstOrNull() ?: "")
            intent.putExtra("PASSENGER_ADDRESS", leadAddress)
            intent.putExtra("PASSENGER_DOB", passengerDobsList.firstOrNull() ?: "")
            intent.putExtra("GENDER", passengerGendersList.firstOrNull() ?: "Male")
            intent.putExtra("SEAT_NUMBER", seatString)

            intent.putStringArrayListExtra("SELECTED_SEATS_ARRAY", ArrayList(generatedSeats))
            intent.putStringArrayListExtra("PASSENGER_NAMES_LIST", passengerNamesList)
            intent.putStringArrayListExtra("PASSENGER_NIDS_LIST", passengerNidsList)
            intent.putStringArrayListExtra("PASSENGER_GENDERS_LIST", passengerGendersList)
            intent.putStringArrayListExtra("PASSENGER_DOBS_LIST", passengerDobsList)

            intent.putExtra("FROM_CITY", fromCity)
            intent.putExtra("TO_CITY", toCity)
            intent.putExtra("TRAVEL_DATE", travelDate)
            intent.putExtra("FLIGHT_CLASS", chosenClass)
            intent.putExtra("AMOUNT", totalAmount)
            startActivity(intent)
        }
    }

    private fun renderPassengerForms() {
        layoutPassengersContainer.removeAllViews()
        passengerHolders.clear()

        val total = getTotalPassengers()
        val typesList = mutableListOf<String>()

        for (i in 1..adultCount) typesList.add("Adult")
        for (i in 1..childCount) typesList.add("Child")
        for (i in 1..infantCount) typesList.add("Infant")

        for (i in 0 until total) {
            val type = typesList.getOrElse(i) { "Adult" }
            val cardView = MaterialCardView(this)
            val cardParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            cardParams.setMargins(0, 0, 0, dpToPx(16))
            cardView.layoutParams = cardParams
            cardView.radius = dpToPx(16).toFloat()
            cardView.cardElevation = dpToPx(4).toFloat()

            val cardContent = LinearLayout(this)
            cardContent.orientation = LinearLayout.VERTICAL
            cardContent.setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20))

            // Card Header
            val tvHeader = TextView(this)
            val leadBadge = if (i == 0) " - Lead Contact" else ""
            tvHeader.text = "👤 Passenger ${i + 1} ($type$leadBadge)"
            tvHeader.textSize = 17f
            tvHeader.setTypeface(null, android.graphics.Typeface.BOLD)
            tvHeader.setTextColor(Color.parseColor("#0066FF"))
            val headerParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            headerParams.setMargins(0, 0, 0, dpToPx(14))
            tvHeader.layoutParams = headerParams
            cardContent.addView(tvHeader)

            // First Name & Last Name Layout
            val layoutNames = LinearLayout(this)
            layoutNames.orientation = LinearLayout.HORIZONTAL
            layoutNames.isBaselineAligned = false

            val tilFirstName = createTextInputLayout("First Name", 1.0f, dpToPx(6), 0)
            val etFirstName = TextInputEditText(tilFirstName.context)
            etFirstName.inputType = android.text.InputType.TYPE_TEXT_VARIATION_PERSON_NAME
            tilFirstName.addView(etFirstName)

            val tilLastName = createTextInputLayout("Last Name", 1.0f, 0, dpToPx(6))
            val etLastName = TextInputEditText(tilLastName.context)
            etLastName.inputType = android.text.InputType.TYPE_TEXT_VARIATION_PERSON_NAME
            tilLastName.addView(etLastName)

            layoutNames.addView(tilFirstName)
            layoutNames.addView(tilLastName)
            cardContent.addView(layoutNames)

            // NID / Passport Field
            val tilNid = createTextInputLayout("NID / Passport Number", 0f, 0, 0)
            val etNid = TextInputEditText(tilNid.context)
            etNid.inputType = android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
            tilNid.addView(etNid)
            cardContent.addView(tilNid)

            // DOB & Gender Layout
            val layoutDobGender = LinearLayout(this)
            layoutDobGender.orientation = LinearLayout.HORIZONTAL
            layoutDobGender.isBaselineAligned = false

            val tilDob = createTextInputLayout("DOB (YYYY-MM-DD)", 1.0f, dpToPx(6), 0)
            val etDob = TextInputEditText(tilDob.context)
            etDob.isFocusable = false
            etDob.isClickable = true
            etDob.setOnClickListener { showDatePicker(etDob) }
            etDob.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) showDatePicker(etDob) }
            tilDob.addView(etDob)

            val tilGender = createTextInputLayout("Gender", 1.0f, 0, dpToPx(6))
            val etGender = TextInputEditText(tilGender.context)
            etGender.isFocusable = false
            etGender.isClickable = true
            etGender.setText("Male")
            etGender.setOnClickListener { showGenderPicker(etGender) }
            etGender.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) showGenderPicker(etGender) }
            tilGender.addView(etGender)

            layoutDobGender.addView(tilDob)
            layoutDobGender.addView(tilGender)
            cardContent.addView(layoutDobGender)

            var etEmail: TextInputEditText? = null
            var etMobile: TextInputEditText? = null
            var etAddress: TextInputEditText? = null

            // Lead Passenger Additional Fields (Email, Mobile, Address)
            if (i == 0) {
                val tvContactHeader = TextView(this)
                tvContactHeader.text = "📞 Contact Details (For Tickets & Updates)"
                tvContactHeader.textSize = 14f
                tvContactHeader.setTypeface(null, android.graphics.Typeface.BOLD)
                tvContactHeader.setTextColor(Color.parseColor("#333333"))
                val contactParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                contactParams.setMargins(0, dpToPx(10), 0, dpToPx(10))
                tvContactHeader.layoutParams = contactParams
                cardContent.addView(tvContactHeader)

                val tilEmail = createTextInputLayout("Email Address", 0f, 0, 0)
                etEmail = TextInputEditText(tilEmail.context)
                etEmail.inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                tilEmail.addView(etEmail)
                cardContent.addView(tilEmail)

                val tilMobile = createTextInputLayout("Mobile Number (e.g. 01712345678)", 0f, 0, 0)
                etMobile = TextInputEditText(tilMobile.context)
                etMobile.inputType = android.text.InputType.TYPE_CLASS_PHONE
                tilMobile.addView(etMobile)
                cardContent.addView(tilMobile)

                val tilAddress = createTextInputLayout("Address", 0f, 0, 0)
                etAddress = TextInputEditText(tilAddress.context)
                etAddress.inputType = android.text.InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS
                tilAddress.addView(etAddress)
                cardContent.addView(tilAddress)
            }

            cardView.addView(cardContent)
            layoutPassengersContainer.addView(cardView)

            passengerHolders.add(
                PassengerFormHolder(
                    index = i,
                    typeLabel = type,
                    etFirstName = etFirstName,
                    etLastName = etLastName,
                    etNid = etNid,
                    etDob = etDob,
                    etGender = etGender,
                    etEmail = etEmail,
                    etMobile = etMobile,
                    etAddress = etAddress
                )
            )
        }
    }

    private fun createTextInputLayout(hint: String, weight: Float, marginEnd: Int, marginStart: Int): TextInputLayout {
        val til = TextInputLayout(this, null, com.google.android.material.R.attr.textInputStyle)
        val params = if (weight > 0) {
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight)
        } else {
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }
        params.setMargins(marginStart, 0, marginEnd, dpToPx(12))
        til.layoutParams = params
        til.hint = hint
        til.boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
        til.setBoxCornerRadii(dpToPx(10).toFloat(), dpToPx(10).toFloat(), dpToPx(10).toFloat(), dpToPx(10).toFloat())
        return til
    }

    private fun createExposedDropdownLayout(hint: String, weight: Float, marginEnd: Int, marginStart: Int): TextInputLayout {
        val til = TextInputLayout(this, null, com.google.android.material.R.attr.textInputStyle)
        val params = if (weight > 0) {
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight)
        } else {
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }
        params.setMargins(marginStart, 0, marginEnd, dpToPx(12))
        til.layoutParams = params
        til.hint = hint
        til.boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
        til.setBoxCornerRadii(dpToPx(10).toFloat(), dpToPx(10).toFloat(), dpToPx(10).toFloat(), dpToPx(10).toFloat())
        return til
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun setupPassengerCounterButtons() {
        val btnMinusAdult = findViewById<MaterialButton>(R.id.btnMinusAdult)
        val btnPlusAdult = findViewById<MaterialButton>(R.id.btnPlusAdult)
        val btnMinusChild = findViewById<MaterialButton>(R.id.btnMinusChild)
        val btnPlusChild = findViewById<MaterialButton>(R.id.btnPlusChild)
        val btnMinusInfant = findViewById<MaterialButton>(R.id.btnMinusInfant)
        val btnPlusInfant = findViewById<MaterialButton>(R.id.btnPlusInfant)

        btnMinusAdult.setOnClickListener {
            if (adultCount > 1) {
                adultCount--
                renderPassengerForms()
                updateFareSummary()
            } else {
                Toast.makeText(this, "At least 1 Adult passenger is required.", Toast.LENGTH_SHORT).show()
            }
        }

        btnPlusAdult.setOnClickListener {
            if (getTotalPassengers() < maxPassengers) {
                adultCount++
                renderPassengerForms()
                updateFareSummary()
            } else {
                Toast.makeText(this, "Maximum $maxPassengers passengers allowed per booking.", Toast.LENGTH_SHORT).show()
            }
        }

        btnMinusChild.setOnClickListener {
            if (childCount > 0) {
                childCount--
                renderPassengerForms()
                updateFareSummary()
            }
        }

        btnPlusChild.setOnClickListener {
            if (getTotalPassengers() < maxPassengers) {
                childCount++
                renderPassengerForms()
                updateFareSummary()
            } else {
                Toast.makeText(this, "Maximum $maxPassengers passengers allowed per booking.", Toast.LENGTH_SHORT).show()
            }
        }

        btnMinusInfant.setOnClickListener {
            if (infantCount > 0) {
                infantCount--
                renderPassengerForms()
                updateFareSummary()
            }
        }

        btnPlusInfant.setOnClickListener {
            if (getTotalPassengers() < maxPassengers) {
                infantCount++
                renderPassengerForms()
                updateFareSummary()
            } else {
                Toast.makeText(this, "Maximum $maxPassengers passengers allowed per booking.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getTotalPassengers(): Int = adultCount + childCount + infantCount

    private fun getUnitPriceForChosenClass(chosenClass: String): Double {
        val isChosenEconomy = chosenClass.equals("Economy", ignoreCase = true)
        val isOriginalBusiness = (flight.flightClass ?: "Economy").equals("Business", ignoreCase = true)

        return if (isChosenEconomy && isOriginalBusiness) {
            (flight.price - 2000.0).coerceAtLeast(1000.0)
        } else if (!isChosenEconomy && !isOriginalBusiness) {
            flight.price + 2000.0
        } else {
            flight.price
        }
    }

    private fun updateFareSummary() {
        tvAdultCount.text = adultCount.toString()
        tvChildCount.text = childCount.toString()
        tvInfantCount.text = infantCount.toString()

        val chosenClass = etClass.text?.toString()?.trim() ?: flightClass
        val unitPrice = getUnitPriceForChosenClass(chosenClass)
        val totalPassengers = getTotalPassengers()

        val adultFare = adultCount * unitPrice
        val childFare = childCount * unitPrice * 0.75
        val infantFare = infantCount * unitPrice * 0.20
        val totalFare = adultFare + childFare + infantFare

        val summaryParts = mutableListOf<String>()
        if (adultCount > 0) summaryParts.add("$adultCount Adult")
        if (childCount > 0) summaryParts.add("$childCount Child")
        if (infantCount > 0) summaryParts.add("$infantCount Infant")

        val summaryText = summaryParts.joinToString(", ")

        tvSelectedSeatsSummary.text = "Total Passengers ($totalPassengers/4): $summaryText"
        tvCalculatedFare.text = "Total Amount: ৳ ${String.format("%.2f", totalFare)} ($chosenClass)"
    }

    private fun autoGenerateSeatNumbers(totalCount: Int, infants: Int): List<String> {
        val row = Random.nextInt(4, 18)
        val columns = arrayOf("A", "B", "C", "D")
        val seats = mutableListOf<String>()
        val seatedCount = (totalCount - infants).coerceAtLeast(1)

        for (i in 0 until seatedCount) {
            val col = columns[i % columns.size]
            val seatRow = row + (i / columns.size)
            seats.add("$seatRow$col")
        }
        for (i in 0 until infants) {
            val parentSeat = seats.getOrNull(i) ?: "${row}A"
            seats.add("Lap ($parentSeat)")
        }
        return seats
    }

    private fun generatePNR(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }

    private fun showDatePicker(editText: TextInputEditText) {
        val calendar = Calendar.getInstance()
        val year = 2000
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val calendarSelected = Calendar.getInstance()
            calendarSelected.set(selectedYear, selectedMonth, selectedDay)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            editText.setText(sdf.format(calendarSelected.time))
        }, year, month, day)

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun showGenderPicker(editText: TextInputEditText) {
        val options = arrayOf("Male", "Female", "Other")
        val current = editText.text.toString().trim()
        val selectedIndex = options.indexOf(current).let { if (it >= 0) it else 0 }

        AlertDialog.Builder(this)
            .setTitle("Select Gender")
            .setSingleChoiceItems(options, selectedIndex) { dialog, which ->
                editText.setText(options[which])
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}