package edu.iu.luddy.c323capstone

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TripListActivity : AppCompatActivity() {

    private val repo = TripRepository()

    private lateinit var rvTrips: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var rgSort: RadioGroup
    private lateinit var rbNewest: RadioButton
    private lateinit var rbOldest: RadioButton

    private lateinit var adapter: TripAdapter
    private var allTrips: List<TripRepository.TripItem> = emptyList()

    companion object {
        const val EXTRA_TRIP_ID = "extra_trip_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.trip_list)

        rvTrips = findViewById(R.id.rvTrips)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)
        fabAdd = findViewById(R.id.fabAdd)
        rgSort = findViewById(R.id.rgSort)
        rbNewest = findViewById(R.id.rbNewest)
        rbOldest = findViewById(R.id.rbOldest)

        rbNewest.isChecked = true

        adapter = TripAdapter(
            onClick = { item ->
                Log.d("TripListActivity", "Item clicked: ${item.id}")
                val i = Intent(this, TripDetailActivity::class.java)
                i.putExtra(EXTRA_TRIP_ID, item.id)
                startActivity(i)
            },
            onLongClick = { item ->
                showRowActions(item)
            }
        )

        rvTrips.layoutManager = LinearLayoutManager(this)
        rvTrips.adapter = adapter

        fabAdd.setOnClickListener {
            startActivity(Intent(this, TripFormActivity::class.java))
        }

        rgSort.setOnCheckedChangeListener { _, _ ->
            applySorting()
        }
    }

    override fun onResume() {
        super.onResume()
        loadTrips()
    }

    private fun loadTrips() {
        progressBar.visibility = android.view.View.VISIBLE
        tvEmpty.visibility = android.view.View.GONE

        repo.getTrips(
            onSuccess = { items ->
                progressBar.visibility = android.view.View.GONE
                allTrips = items
                applySorting()
                tvEmpty.visibility = if (items.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            },
            onError = { e ->
                progressBar.visibility = android.view.View.GONE
                Toast.makeText(this, e.message ?: "Failed to load trips", Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun applySorting() {
        val sorted = when {
            rbNewest.isChecked -> allTrips.sortedByDescending { it.trip.dateStart }
            rbOldest.isChecked -> allTrips.sortedBy { it.trip.dateStart }
            else -> allTrips
        }
        adapter.submitList(sorted)
    }

    private fun showRowActions(item: TripRepository.TripItem) {
        val options = arrayOf("Edit", "Delete")
        AlertDialog.Builder(this)
            .setTitle(item.trip.name ?: "Trip")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        // go to trip edit page
                        val i = Intent(this, TripFormActivity::class.java)
                        i.putExtra("trip_id", item.id)
                        startActivity(i)
                    }
                    1 -> confirmDelete(item.id)
                }
            }
            .show()
    }

    private fun confirmDelete(id: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete trip?")
            .setMessage("This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                repo.deleteTrip(id) { ok ->
                    if (ok) {
                        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
                        loadTrips()
                    } else {
                        Toast.makeText(this, "Delete failed", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
