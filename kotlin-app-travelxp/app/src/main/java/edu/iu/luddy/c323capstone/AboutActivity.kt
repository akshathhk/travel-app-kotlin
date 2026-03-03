package edu.iu.luddy.c323capstone

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about)

        findViewById<TextView>(R.id.tvAbout).text =
            "TravelXP is a personal travel journal.\n\n" +
                    "You can add trips with dates, rating, notes, and who you traveled with.\n\n" +
                    "Course features shown:\n" +
                    "• RecyclerView list\n" +
                    "• DatePicker\n" +
                    "• Snackbar + AlertDialog\n" +
                    "• SharedPreferences draft saving\n" +
                    "• Firebase Firestore CRUD"
    }
}
