package com.example.univibe.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.univibe.R
import com.example.univibe.models.LostFoundItem
import java.text.SimpleDateFormat
import java.util.Locale

class LostFoundAdapter(
    private val itemList: List<LostFoundItem>,
    private val context: Context
) : RecyclerView.Adapter<LostFoundAdapter.LostFoundViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy - h:mm a", Locale.getDefault())

    class LostFoundViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardContainer: CardView = itemView.findViewById(R.id.lost_found_card)
        val statusIndicator: TextView = itemView.findViewById(R.id.item_status_indicator)
        val title: TextView = itemView.findViewById(R.id.item_title)
        val location: TextView = itemView.findViewById(R.id.item_location)
        val postedDate: TextView = itemView.findViewById(R.id.item_posted_date)
        val description: TextView = itemView.findViewById(R.id.item_description)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LostFoundViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lost_found, parent, false)
        return LostFoundViewHolder(view)
    }

    override fun onBindViewHolder(holder: LostFoundViewHolder, position: Int) {
        val item = itemList[position]

        holder.title.text = item.title
        holder.description.text = item.description
        holder.location.text = item.location
        holder.postedDate.text = dateFormat.format(item.postedAt.toDate())

        if (item.isLost) {
            holder.statusIndicator.text = "LOST"
            holder.statusIndicator.setBackgroundColor(Color.parseColor("#FFCCBC"))
            holder.statusIndicator.setTextColor(Color.parseColor("#D32F2F"))
        } else {
            holder.statusIndicator.text = "FOUND"
            holder.statusIndicator.setBackgroundColor(Color.parseColor("#C8E6C9"))
            holder.statusIndicator.setTextColor(Color.parseColor("#388E3C"))
        }

        holder.itemView.setOnClickListener {
            // TODO: Navigate to a LostFoundDetailActivity
        }
    }

    override fun getItemCount(): Int = itemList.size
}
