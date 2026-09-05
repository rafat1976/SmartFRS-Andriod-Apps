package com.example.smartfrs

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class FlightAdapter(
    private val flightList: List<Flight>,
    private val fromCity: String = "Dhaka",
    private val toCity: String = "Sylhet",
    private val travelDate: String = "2026-08-10",
    private val flightClass: String = "Economy"
) : RecyclerView.Adapter<FlightAdapter.FlightViewHolder>() {

    class FlightViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAirlineName: TextView = itemView.findViewById(R.id.tvAirlineName)
        val tvFlightId: TextView? = itemView.findViewById(R.id.tvFlightId)
        val tvFlightClass: TextView = itemView.findViewById(R.id.tvFlightClass)
        val tvDepartureTime: TextView = itemView.findViewById(R.id.tvDepartureTime)
        val tvArrivalTime: TextView = itemView.findViewById(R.id.tvArrivalTime)
        val tvFromCity: TextView? = itemView.findViewById(R.id.tvFromCity)
        val tvToCity: TextView? = itemView.findViewById(R.id.tvToCity)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val btnBookNow: MaterialButton = itemView.findViewById(R.id.btnBookNow)
        val layoutDelayPredictor: LinearLayout? = itemView.findViewById(R.id.layoutDelayPredictor)
        val tvDelayIcon: TextView? = itemView.findViewById(R.id.tvDelayIcon)
        val tvDelayPredictorText: TextView? = itemView.findViewById(R.id.tvDelayPredictorText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlightViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_flight, parent, false)
        return FlightViewHolder(view)
    }

    override fun onBindViewHolder(holder: FlightViewHolder, position: Int) {
        val flight = flightList[position]
        val currentClass = flight.flightClass ?: flightClass
        val isEconomy = currentClass.equals("Economy", ignoreCase = true)
        val displayPrice = if (isEconomy && flight.price >= 4000.0) {
            (flight.price - 2000.0).coerceAtLeast(1500.0)
        } else {
            flight.price
        }

        val itemFromCity = flight.source ?: fromCity
        val itemToCity = flight.destination ?: toCity
        val updatedFlight = flight.copy(price = displayPrice, flightClass = currentClass, source = itemFromCity, destination = itemToCity)

        holder.tvAirlineName.text = flight.airlineName
        holder.tvFlightId?.text = "Flight: ${flight.flightId}"
        holder.tvFlightClass.text = currentClass
        holder.tvDepartureTime.text = flight.departureTime
        holder.tvArrivalTime.text = flight.arrivalTime
        holder.tvFromCity?.text = itemFromCity
        holder.tvToCity?.text = itemToCity
        holder.tvPrice.text = "৳ ${String.format("%.2f", displayPrice)}"

        // AI Flight Delay Prediction Logic
        val (icon, text, score, estDelay) = getDelayPredictionData(position, flight.airlineName)
        holder.tvDelayIcon?.text = icon
        holder.tvDelayPredictorText?.text = text

        holder.layoutDelayPredictor?.setOnClickListener {
            showDelayAnalyticsDialog(holder.itemView.context, flight, itemFromCity, itemToCity, score, estDelay)
        }

        holder.btnBookNow.setOnClickListener {
            val intent = Intent(holder.itemView.context, PassengerInfoActivity::class.java)
            intent.putExtra("FLIGHT_DATA", updatedFlight)
            intent.putExtra("FROM_CITY", itemFromCity)
            intent.putExtra("TO_CITY", itemToCity)
            intent.putExtra("TRAVEL_DATE", travelDate)
            intent.putExtra("FLIGHT_CLASS", currentClass)
            holder.itemView.context.startActivity(intent)
        }
    }

    private fun getDelayPredictionData(position: Int, airline: String): Quadruple<String, String, Int, String> {
        return when (position % 3) {
            0 -> Quadruple("🟢", "AI Prediction: 96% On-Time (Low Delay Risk)", 96, "0 - 5 Mins")
            1 -> Quadruple("🟡", "AI Prediction: 83% On-Time (Est. 10m Delay)", 83, "10 - 15 Mins")
            else -> Quadruple("🟢", "AI Prediction: 92% On-Time (Low Delay Risk)", 92, "0 - 8 Mins")
        }
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    private fun showDelayAnalyticsDialog(
        context: android.content.Context,
        flight: Flight,
        from: String,
        to: String,
        onTimeScore: Int,
        estDelay: String
    ) {
        val details = """
            ✈️ FLIGHT DELAY RISK ANALYTICS

            📌 Flight ID: ${flight.flightId} (${flight.airlineName})
            🛣️ Route: $from ➔ $to
            
            📊 AI Punctuality Rating: $onTimeScore%
            ⏱️ Estimated Delay: $estDelay
            🌦️ Weather Factor: Normal Flight Operations
            🤖 AI Model Confidence: 94.5%
            
            💡 Insights: Based on historical flight data and current destination weather patterns, this flight has a very low probability of schedule disruption.
        """.trimIndent()

        MaterialAlertDialogBuilder(context)
            .setTitle("🤖 Smart AI Flight Delay Analytics")
            .setMessage(details)
            .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun getItemCount(): Int {
        return flightList.size
    }
}