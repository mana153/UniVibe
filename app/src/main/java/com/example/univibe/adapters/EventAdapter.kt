package com.example.univibe.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.univibe.databinding.ItemEventBinding
import com.example.univibe.models.Event

class EventAdapter(
    private var items: List<Event>,
    private val onClick: (Event) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    inner class EventViewHolder(private val binding: ItemEventBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {
            // ✅ Use underscore naming to match XML
            binding.eventTitle.text = event.title
            binding.eventDate.text = event.date
            binding.eventLocation.text = event.location
            binding.eventCategory.text = event.category
            binding.eventDescription.text = event.description

            // Description removed since it's not in the XML layout

            binding.root.setOnClickListener { onClick(event) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Event>) {
        items = newItems
        notifyDataSetChanged()
    }
}
