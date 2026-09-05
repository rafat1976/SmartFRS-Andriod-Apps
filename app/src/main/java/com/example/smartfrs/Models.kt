package com.example.smartfrs

import com.google.gson.annotations.SerializedName
import java.io.Serializable

// --- Login Models ---
data class LoginRequest(val email: String, val password: String)
data class LoginResponse(
    val status: Boolean,
    val message: String,
    val name: String? = null // সার্ভার থেকে ইউজারের নাম পাওয়ার জন্য
)

// --- Register Models ---
data class RegisterRequest(
    val name: String,
    val email: String,
    val phone: String,
    val password: String
)
data class RegisterResponse(val status: Boolean, val message: String)


// --- Flight Search Models ---

// ফ্লাইটের বিস্তারিত তথ্যের মডেল
data class Flight(
    val flightId: String,
    val airlineName: String,
    val departureTime: String,
    val arrivalTime: String,
    val price: Double,
    @SerializedName("class")
    val flightClass: String? = null,
    val source: String? = null,
    val destination: String? = null
) : Serializable

// --- Chat Models ---
data class ChatMessage(
    val message: String,
    val isBot: Boolean // true হলে বট, false হলে ইউজার
)

// --- Booking & Ticket Models ---
data class BookingRequest(
    val flightId: String,
    val passengerName: String,
    val passengerEmail: String,
    val passengerMobile: String,
    val passengerNid: String,
    val passengerDob: String,
    val paymentMethod: String,
    val amount: Double
)

data class BookingResponse(
    val status: Boolean,
    val message: String,
    val pnr: String? = null
)

data class Ticket(
    val pnr: String,
    val flight: Flight,
    val passengerName: String,
    var status: String // "CONFIRMED", "CANCELLED"
) : Serializable

// সার্চ রেসপন্সের মডেল
data class SearchFlightResponse(
    val status: Boolean,
    val message: String,
    val flights: List<Flight>
)