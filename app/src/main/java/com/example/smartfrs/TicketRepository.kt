package com.example.smartfrs

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Serializable

data class BookedTicket(
    val ticketId: String,
    val passportNo: String,
    val firstName: String = "",
    val lastName: String = "",
    val passengerName: String,
    val email: String,
    val mobile: String,
    val address: String,
    val dob: String = "",
    val gender: String = "Male",
    val seatNumber: String = "12A",
    val source: String,
    val destination: String,
    val flightClass: String,
    val travelDate: String,
    val travelDay: String,
    val airlineName: String,
    val price: Double,
    var status: String = "PAID (Confirmed)",
    val flightNumber: String = "BG-202",
    val gate: String = "04",
    val boardingTime: String = "10:20 AM",
    val fromCode: String = "DAC",
    val toCode: String = "CXB"
) : Serializable

object TicketRepository {
    private const val PREF_NAME = "SmartFRS_Tickets_Pref"
    private const val KEY_TICKETS = "KEY_BOOKED_TICKETS"
    private val gson = Gson()

    fun getTickets(context: Context): MutableList<BookedTicket> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_TICKETS, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<BookedTicket>>() {}.type
        return try {
            gson.fromJson(json, type) ?: mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    fun saveTicket(context: Context, ticket: BookedTicket) {
        val list = getTickets(context)
        list.removeAll { it.ticketId.equals(ticket.ticketId, ignoreCase = true) }
        list.add(0, ticket)
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_TICKETS, gson.toJson(list)).apply()
    }

    fun saveTickets(context: Context, tickets: List<BookedTicket>) {
        val list = getTickets(context)
        tickets.reversed().forEach { ticket ->
            list.removeAll { it.ticketId.equals(ticket.ticketId, ignoreCase = true) }
            list.add(0, ticket)
        }
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_TICKETS, gson.toJson(list)).apply()
    }

    fun findTicket(context: Context, passportNo: String, ticketId: String): BookedTicket? {
        val list = getTickets(context)
        val cleanPassport = passportNo.trim()
        val cleanTicketId = ticketId.trim()

        return list.find { ticket ->
            val matchTicket = cleanTicketId.isNotEmpty() && ticket.ticketId.equals(cleanTicketId, ignoreCase = true)
            val matchPassport = cleanPassport.isNotEmpty() && ticket.passportNo.equals(cleanPassport, ignoreCase = true)
            matchTicket || matchPassport
        }
    }

    fun cancelTicket(context: Context, ticketId: String): Boolean {
        val list = getTickets(context)
        val index = list.indexOfFirst { it.ticketId.equals(ticketId.trim(), ignoreCase = true) }
        if (index != -1) {
            list[index].status = "CANCELLED"
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_TICKETS, gson.toJson(list)).apply()
            return true
        }
        return false
    }
}
