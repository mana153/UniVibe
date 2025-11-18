package com.example.univibe.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.univibe.databinding.ItemTransportBinding
import com.example.univibe.models.Transport

class TransportAdapter(
    private var items: List<Transport>,
    private val onClick: (Transport) -> Unit
) : RecyclerView.Adapter<TransportAdapter.TransportViewHolder>() {

    inner class TransportViewHolder(private val binding: ItemTransportBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transport: Transport) {
            binding.transportTitle.text = transport.routename  // Changed from routeName
            binding.transportOrigin.text = transport.source
            binding.transportDestination.text = transport.destination
            binding.transportDepartureTime.text = "Departure: ${transport.departureTime}"

            // Handle nullable arrivalTime
            binding.transportArrivalTime.text = if (transport.arrivalTime != null) {
                "Arrival: ${transport.arrivalTime}"
            } else {
                "Arrival: Not specified"
            }

            // Changed from days list to dayType string
            binding.transportDays.text = "Days: ${transport.dayType}"

            binding.root.setOnClickListener { onClick(transport) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransportViewHolder {
        val binding = ItemTransportBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransportViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Transport>) {
        items = newItems
        notifyDataSetChanged()
    }
}
