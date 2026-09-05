package com.example.smartfrs

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RealBookingAdapter(
    private val tickets: MutableList<BookedTicket>,
    private val onItemClick: (BookedTicket) -> Unit,
    private val onCancelClick: (BookedTicket, Int) -> Unit
) : RecyclerView.Adapter<RealBookingAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvBookingId: TextView = view.findViewById(R.id.tvBookingId)
        val tvAirline: TextView = view.findViewById(R.id.tvAirline)
        val tvFlightDetails: TextView = view.findViewById(R.id.tvFlightDetails)
        val tvPassenger: TextView = view.findViewById(R.id.tvPassenger)
        val tvSeatInfo: TextView = view.findViewById(R.id.tvSeatInfo)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val ivMiniQr: ImageView = view.findViewById(R.id.ivMiniQr)
        val btnCancelTicket: Button = view.findViewById(R.id.btnCancelTicket)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_booked_ticket, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ticket = tickets[position]

        holder.tvBookingId.text = "Ticket ID: #${ticket.ticketId}"
        holder.tvAirline.text = "${ticket.airlineName} (${ticket.flightNumber})"
        holder.tvFlightDetails.text = "${ticket.source} ➔ ${ticket.destination}  |  ${ticket.travelDate}"
        holder.tvPassenger.text = "Passenger: ${ticket.passengerName}"
        holder.tvSeatInfo.text = "Seat: ${ticket.seatNumber}  |  Class: ${ticket.flightClass}"
        holder.tvStatus.text = "Status: ${ticket.status}"

        if (ticket.status.contains("CANCELLED", ignoreCase = true)) {
            holder.tvStatus.setTextColor(Color.parseColor("#D32F2F"))
            holder.btnCancelTicket.visibility = View.GONE
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#2E7D32"))
            holder.btnCancelTicket.visibility = View.VISIBLE
        }

        // Render QR Code Preview
        val qrBitmap = QRCodeGenerator.generateQRCode(ticket.ticketId, 150, 150)
        holder.ivMiniQr.setImageBitmap(qrBitmap)

        holder.itemView.setOnClickListener {
            onItemClick(ticket)
        }

        holder.btnCancelTicket.setOnClickListener {
            onCancelClick(ticket, position)
        }
    }

    override fun getItemCount(): Int = tickets.size
}
