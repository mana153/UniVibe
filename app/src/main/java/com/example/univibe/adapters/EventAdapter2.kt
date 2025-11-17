package com.example.univibe.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.univibe.R
import com.example.univibe.databinding.ItemEventBinding
import com.example.univibe.models.Event
import java.text.SimpleDateFormat
import java.util.Locale

class EventAdapter2(private var items: List<Event>, private val onClick: (Event) -> Unit) : RecyclerView.Adapter<EventAdapter2.EventViewHolder>() {

    inner class EventViewHolder(private val binding: ItemEventBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event) {
            binding.eventTitle.text = event.title
            binding.eventLocation.text = event.location

            if (event.date != null) {
                val sdf = SimpleDateFormat("MMM d, yyyy - h:mm a", Locale.getDefault())
                binding.eventDate.text = sdf.format(event.date!!.toDate())
            } else {
                binding.eventDate.text = "Date not set"
            }

            binding.eventCategory.text = event.category

            if (event.imageUrl.isNotEmpty()) {
                binding.eventImage.visibility = android.view.View.VISIBLE
                Glide.with(binding.eventImage.context)
                    .load(event.imageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(binding.eventImage)
            } else {
                binding.eventImage.visibility = android.view.View.GONE
            }

            binding.root.setOnClickListener { onClick(event) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

