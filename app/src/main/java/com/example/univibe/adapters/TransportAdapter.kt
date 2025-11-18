package com.example.univibe.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.univibe.databinding.ItemTransportBinding
import com.example.univibe.models.Transport

class TransportAdapter(
    private var items: ArrayList<Transport>,
    private val onClick: (Transport) -> Unit
) : RecyclerView.Adapter<TransportAdapter.TransportViewHolder>() {

    inner class TransportViewHolder(private val binding: ItemTransportBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transport: Transport) = with(binding) {
            transportTitle.text = transport.routename
            transportOrigin.text = "From: ${transport.source}"
            transportDestination.text = "To: ${transport.destination}"
            transportDepartureTime.text = "Departure: ${transport.departureTime}"
            transportArrivalTime.text =
                if (transport.arrivalTime.isNotEmpty()) "Arrival: ${transport.arrivalTime}"
                else "Arrival: Not specified"

            transportDays.text = "Days: ${transport.dayType}"

            root.setOnClickListener { onClick(transport) }
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

    fun updateData(newItems: ArrayList<Transport>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
