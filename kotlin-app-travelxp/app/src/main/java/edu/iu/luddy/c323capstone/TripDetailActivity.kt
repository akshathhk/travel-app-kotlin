package edu.iu.luddy.c323capstone

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

class TripDetailActivity : AppCompatActivity() {

    private val repo = TripRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.trip_detail)

        val tripId = intent.getStringExtra(TripListActivity.EXTRA_TRIP_ID) ?: run {
            finish(); return
        }

        val ivType = findViewById<ImageView>(R.id.ivType)
        val tvPlace = findViewById<TextView>(R.id.tvPlace)
        val tvDate = findViewById<TextView>(R.id.tvDate)
        val tvRating = findViewById<TextView>(R.id.tvRating)
        val tvWith = findViewById<TextView>(R.id.tvWith)
        val tvNotes = findViewById<TextView>(R.id.tvNotes)
        val btnEdit = findViewById<Button>(R.id.btnEdit)
        val btnDelete = findViewById<Button>(R.id.btnDelete)

        ivType.setImageResource(R.drawable.travelimage)

        repo.getTripById(
            id = tripId,
            onSuccess = { item ->
                val trip = item?.trip ?: run { finish(); return@getTripById }

                tvPlace.text = trip.name ?: "(No name)"
                tvDate.text = "Dates: ${trip.dateStart ?: "?"} - ${trip.dateEnd ?: "?"}"
                tvRating.text = "Rating: ${(trip.rating ?: 0)}/5"

                tvWith.text =
                    if (trip.withWhom.isNullOrBlank()) "With: (not specified)"
                    else "With: ${trip.withWhom}"

                tvNotes.text =
                    if (trip.description.isNullOrBlank()) "(No notes)"
                    else trip.description

            },
            onError = {
                Snackbar.make(btnDelete, "Failed to load trip.", Snackbar.LENGTH_SHORT).show()
            }
        )

        btnEdit.setOnClickListener {
            val i = Intent(this, TripFormActivity::class.java)
            i.putExtra("trip_id", tripId)
            startActivity(i)
        }

        btnDelete.setOnClickListener { v ->
            AlertDialog.Builder(this)
                .setTitle("Delete trip?")
                .setMessage("This cannot be undone.")
                .setPositiveButton("Delete") { _, _ ->
                    repo.deleteTrip(tripId) { ok ->
                        if (ok) {
                            Snackbar.make(v, "Trip deleted.", Snackbar.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Snackbar.make(v, "Delete failed.", Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}
