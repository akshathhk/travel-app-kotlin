package edu.iu.luddy.c323capstone

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TripFormActivity : AppCompatActivity() {

    private val repo = TripRepository()
    private val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.US)

    private var startMs: Long = 0L
    private var endMs: Long = 0L

    private var editingTripId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.trip_form)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        val tvHeader = findViewById<TextView>(R.id.tvHeader)
        val etPlace = findViewById<EditText>(R.id.etPlaceName)
        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        val btnStart = findViewById<Button>(R.id.btnPickStartDate)
        val btnEnd = findViewById<Button>(R.id.btnPickEndDate)
        val etWith = findViewById<EditText>(R.id.etWith)
        val etNotes = findViewById<EditText>(R.id.etNotes)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnClearDraft = findViewById<Button>(R.id.btnClearDraft)

        editingTripId = intent.getStringExtra("trip_id")
        
        // If returning from rotation, restore from savedInstanceState
        if (savedInstanceState != null) {
            startMs = savedInstanceState.getLong("startMs", 0L)
            endMs = savedInstanceState.getLong("endMs", 0L)
            if (startMs > 0L) btnStart.text = "Start: ${sdf.format(startMs)}"
            if (endMs > 0L) btnEnd.text = "End: ${sdf.format(endMs)}"
            
            val savedPlace = savedInstanceState.getString("place", "")
            if (savedPlace.isNotBlank()) etPlace.setText(savedPlace)
            
            val savedWithWhom = savedInstanceState.getString("withWhom", "")
            if (savedWithWhom.isNotBlank()) etWith.setText(savedWithWhom)
            
            val savedNotes = savedInstanceState.getString("notes", "")
            if (savedNotes.isNotBlank()) etNotes.setText(savedNotes)
            
            val savedRating = savedInstanceState.getFloat("rating", 0f)
            if (savedRating > 0) ratingBar.rating = savedRating
        } 
        // First time loading - load from Firebase or SharedPreferences
        else {
            if (editingTripId != null) {
                tvHeader.text = "Edit Trip"
                loadTripIntoForm(editingTripId!!)
            } else {
                val d = TripDraftPrefs.load(this)
                if (d.place.isNotBlank()) etPlace.setText(d.place)
                if (d.rating > 0) ratingBar.rating = d.rating.toFloat()
                if (d.start > 0L) {
                    startMs = d.start
                    btnStart.text = "Start: ${sdf.format(startMs)}"
                }
                if (d.end > 0L) {
                    endMs = d.end
                    btnEnd.text = "End: ${sdf.format(endMs)}"
                }
                if (d.withWhom.isNotBlank()) etWith.setText(d.withWhom)
                if (d.notes.isNotBlank()) etNotes.setText(d.notes)
            }
        }

        btnStart.setOnClickListener {
            pickDate(initialMs = startMs) { picked ->
                startMs = picked
                btnStart.text = "Start: ${sdf.format(startMs)}"
                if (endMs > 0 && endMs < startMs) {
                    endMs = 0L
                    btnEnd.text = "Pick End Date (DatePicker)"
                }
            }
        }

        btnEnd.setOnClickListener {
            pickDate(initialMs = if (endMs > 0) endMs else startMs) { picked ->
                endMs = picked
                btnEnd.text = "End: ${sdf.format(endMs)}"
            }
        }

        btnSave.setOnClickListener {
            val place = etPlace.text.toString().trim()
            val rating = ratingBar.rating.toInt()
            val withWhom = etWith.text.toString().trim()
            val notes = etNotes.text.toString().trim()

            if (place.isEmpty()) {
                etPlace.error = "Place name required"
                etPlace.requestFocus()
                return@setOnClickListener
            }
            if (rating <= 0) {
                Snackbar.make(btnSave, "Please give a rating.", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (startMs <= 0L) {
                Snackbar.make(btnSave, "Pick a start date.", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (endMs > 0L && endMs < startMs) {
                Snackbar.make(btnSave, "End date cannot be before start date.", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val dateStartStr = sdf.format(startMs)
            val dateEndStr = sdf.format(if (endMs > 0L) endMs else startMs)


            val trip = TripRepository.Trip(
                name = place,
                destination = place,
                dateStart = dateStartStr,
                dateEnd = dateEndStr,
                description = notes,
                withWhom = withWhom,
                rating = rating
            )

            val id = editingTripId
            if (id == null) {
                repo.createTrip(trip) { ok ->
                    if (ok) {
                        TripDraftPrefs.clear(this)
                        Snackbar.make(btnSave, "Trip saved!", Snackbar.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Snackbar.make(btnSave, "Save failed.", Snackbar.LENGTH_SHORT).show()
                    }
                }
            } else {
                repo.updateTrip(id, trip) { ok ->
                    if (ok) {
                        Snackbar.make(btnSave, "Trip updated!", Snackbar.LENGTH_SHORT).show()
                        val i = Intent(this, TripListActivity::class.java)
                        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        startActivity(i)
                        finish()
                    } else {
                        Snackbar.make(btnSave, "Update failed.", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btnClearDraft.setOnClickListener {
            TripDraftPrefs.clear(this)
            etPlace.setText("")
            ratingBar.rating = 0f
            startMs = 0L
            endMs = 0L
            btnStart.text = "Pick Start Date"
            btnEnd.text = "Pick End Date"
            etWith.setText("")
            etNotes.setText("")
            Snackbar.make(btnClearDraft, "Draft cleared.", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong("startMs", startMs)
        outState.putLong("endMs", endMs)
        outState.putString("place", findViewById<EditText>(R.id.etPlaceName).text.toString())
        outState.putString("withWhom", findViewById<EditText>(R.id.etWith).text.toString())
        outState.putString("notes", findViewById<EditText>(R.id.etNotes).text.toString())
        outState.putFloat("rating", findViewById<RatingBar>(R.id.ratingBar).rating)
    }

    override fun onPause() {
        super.onPause()
        if (editingTripId == null) {
            val etPlace = findViewById<EditText>(R.id.etPlaceName)
            val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
            val etWith = findViewById<EditText>(R.id.etWith)
            val etNotes = findViewById<EditText>(R.id.etNotes)

            TripDraftPrefs.save(
                this,
                TripDraftPrefs.Draft(
                    place = etPlace.text.toString(),
                    rating = ratingBar.rating.toInt(),
                    start = startMs,
                    end = endMs,
                    withWhom = etWith.text.toString(),
                    notes = etNotes.text.toString()
                )
            )
        }
    }

    private fun loadTripIntoForm(id: String) {
        val etPlace = findViewById<EditText>(R.id.etPlaceName)
        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        val btnStart = findViewById<Button>(R.id.btnPickStartDate)
        val btnEnd = findViewById<Button>(R.id.btnPickEndDate)
        val etWith = findViewById<EditText>(R.id.etWith)
        val etNotes = findViewById<EditText>(R.id.etNotes)

        repo.getTripById(
            id = id,
            onSuccess = { item ->
                val trip = item?.trip ?: return@getTripById
                etPlace.setText(trip.name ?: "")
                ratingBar.rating = (trip.rating ?: 0).toFloat()

                trip.dateStart?.let { btnStart.text = "Start: $it" }
                trip.dateEnd?.let { btnEnd.text = "End: $it" }

                etWith.setText(trip.withWhom ?: "")
                etNotes.setText(trip.description ?: "")

            },
            onError = {
                Snackbar.make(findViewById(R.id.btnSave), "Failed to load trip.", Snackbar.LENGTH_SHORT).show()
            }
        )
    }

    private fun pickDate(initialMs: Long, onPicked: (Long) -> Unit) {
        val cal = Calendar.getInstance()
        if (initialMs > 0L) cal.timeInMillis = initialMs

        DatePickerDialog(
            this,
            { _, y, m, d ->
                val c = Calendar.getInstance()
                c.set(y, m, d, 0, 0, 0)
                c.set(Calendar.MILLISECOND, 0)
                onPicked(c.timeInMillis)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}
