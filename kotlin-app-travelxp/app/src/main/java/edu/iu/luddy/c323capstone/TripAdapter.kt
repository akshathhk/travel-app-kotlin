package edu.iu.luddy.c323capstone

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TripAdapter(
    private val onClick: (TripRepository.TripItem) -> Unit,
    private val onLongClick: (TripRepository.TripItem) -> Unit
) : RecyclerView.Adapter<TripAdapter.VH>() {

    private val items = mutableListOf<TripRepository.TripItem>()

    fun submitList(newItems: List<TripRepository.TripItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_trip, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.bind(item, onClick, onLongClick)
    }

    override fun getItemCount(): Int = items.size

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvDestination: TextView = itemView.findViewById(R.id.tvDestination)
        private val tvDates: TextView = itemView.findViewById(R.id.tvDates)
        private val tvRating: TextView = itemView.findViewById(R.id.tvRating)

        fun bind(
            item: TripRepository.TripItem,
            onClick: (TripRepository.TripItem) -> Unit,
            onLongClick: (TripRepository.TripItem) -> Unit
        ) {
            val t = item.trip

            tvName.text = t.name ?: "(No name)"
            tvDestination.text = t.destination ?: ""
            val start = t.dateStart ?: ""
            val end = t.dateEnd ?: ""
            tvDates.text = if (start.isNotBlank() || end.isNotBlank()) "$start → $end" else ""
            
            val rating = t.rating ?: 0
            tvRating.text = "★".repeat(rating) + "☆".repeat(5 - rating)

            itemView.setOnClickListener { onClick(item) }
            itemView.setOnLongClickListener {
                onLongClick(item)
                true
            }
        }
    }
}
