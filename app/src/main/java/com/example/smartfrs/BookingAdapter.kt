package com.example.smartfrs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BookingAdapter(
    private var tickets: MutableList<Ticket>,
    private val onCancelClick: (Ticket, Int) -> Unit
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    class BookingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvBookingId: TextView = view.findViewById(R.id.tvBookingId)
        val tvAirline: TextView = view.findViewById(R.id.tvAirline)
        val tvFlightDetails: TextView = view.findViewById(R.id.tvFlightDetails)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val btnCancelTicket: Button = view.findViewById(R.id.btnCancelTicket)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_booked_ticket, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val ticket = tickets[position]
        holder.tvBookingId.text = "Booking ID: #${ticket.pnr}"
        holder.tvAirline.text = ticket.flight.airlineName
        holder.tvFlightDetails.text = "${ticket.flight.departureTime} - ${ticket.flight.arrivalTime}"
        holder.tvStatus.text = "Status: ${ticket.status}"

        holder.btnCancelTicket.setOnClickListener {
            onCancelClick(ticket, position)
        }
    }

    override fun getItemCount(): Int = tickets.size

    fun removeAt(position: Int) {
        tickets.removeAt(position)
        notifyItemRemoved(position)
    }
}