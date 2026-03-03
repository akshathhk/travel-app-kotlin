package edu.iu.luddy.c323capstone

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnAddTrip).setOnClickListener {
            startActivity(Intent(this, TripFormActivity::class.java))
        }

        findViewById<Button>(R.id.btnViewTrips).setOnClickListener {
            startActivity(Intent(this, TripListActivity::class.java))
        }

        findViewById<Button>(R.id.btnAbout).setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            startActivity(Intent(this, EmailPasswordActivity::class.java))
        }

        findViewById<Button>(R.id.btnSignOut).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show()
        }
    }
}
