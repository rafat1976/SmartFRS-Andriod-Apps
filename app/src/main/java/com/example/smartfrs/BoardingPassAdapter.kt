package com.example.smartfrs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class BoardingPassAdapter(
    private val tickets: List<BookedTicket>
) : RecyclerView.Adapter<BoardingPassAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAirlineName: TextView = view.findViewById(R.id.tvPassAirlineName)
        val tvClassBadge: TextView = view.findViewById(R.id.tvPassClassBadge)
        val tvPassengerName: TextView = view.findViewById(R.id.tvPassPassengerName)
        val tvDate: TextView = view.findViewById(R.id.tvPassDate)
        val tvFromCode: TextView = view.findViewById(R.id.tvPassFromCode)
        val tvFromCity: TextView = view.findViewById(R.id.tvPassFromCity)
        val tvToCode: TextView = view.findViewById(R.id.tvPassToCode)
        val tvToCity: TextView = view.findViewById(R.id.tvPassToCity)
        val tvBoardingTime: TextView = view.findViewById(R.id.tvPassBoardingTime)
        val tvFlightNo: TextView = view.findViewById(R.id.tvPassFlightNo)
        val tvGate: TextView = view.findViewById(R.id.tvPassGate)
        val tvSeat: TextView = view.findViewById(R.id.tvPassSeat)
        val tvTicketId: TextView = view.findViewById(R.id.tvPassTicketId)
        val ivQrCode: ImageView = view.findViewById(R.id.ivPassQrCode)
        val ivBarcode: ImageView = view.findViewById(R.id.ivPassBarcode)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_boarding_pass, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ticket = tickets[position]
        val context = holder.itemView.context

        holder.tvAirlineName.text = ticket.airlineName.uppercase()
        holder.tvClassBadge.text = ticket.flightClass.uppercase()
        holder.tvPassengerName.text = ticket.passengerName.uppercase()
        holder.tvDate.text = ticket.travelDate
        holder.tvFromCode.text = ticket.fromCode
        holder.tvFromCity.text = ticket.source
        holder.tvToCode.text = ticket.toCode
        holder.tvToCity.text = ticket.destination
        holder.tvBoardingTime.text = ticket.boardingTime
        holder.tvFlightNo.text = ticket.flightNumber
        holder.tvGate.text = ticket.gate
        holder.tvSeat.text = ticket.seatNumber
        holder.tvTicketId.text = "#${ticket.ticketId}"

        // Generate QR Code & Barcode bitmaps dynamically
        val qrBitmap = QRCodeGenerator.generateQRCode(ticket.ticketId, 250, 250)
        holder.ivQrCode.setImageBitmap(qrBitmap)

        val barcodeBitmap = QRCodeGenerator.generateBarcode(ticket.ticketId, 400, 100)
        holder.ivBarcode.setImageBitmap(barcodeBitmap)

        // Copy Ticket ID on click
        holder.tvTicketId.setOnClickListener {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Ticket ID", ticket.ticketId)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Ticket ID (${ticket.ticketId}) copied to clipboard! 📋", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = tickets.size
}
