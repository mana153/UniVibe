package com.example.univibe.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.univibe.R
import com.example.univibe.models.Event
import java.text.SimpleDateFormat
import java.util.Locale

class EventAdapter(private val eventList: List<Event>) :
    RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy - h:mm a", Locale.getDefault())

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.event_title)
        val description: TextView = itemView.findViewById(R.id.event_description)
        val date: TextView = itemView.findViewById(R.id.event_date)
        val category: TextView = itemView.findViewById(R.id.event_category)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventList[position]

        holder.title.text = event.title
        holder.description.text = event.description
        holder.date.text = dateFormat.format(event.date.toDate())
        holder.category.text = event.category

        holder.itemView.setOnClickListener {
            // TODO: Navigate to EventDetailActivity
        }
    }

    override fun getItemCount(): Int = eventList.size
}
