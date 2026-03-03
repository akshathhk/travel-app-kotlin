package edu.iu.luddy.c323capstone

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Plain Kotlin class – NOT an Activity
class TripRepository {

    // Firestore instance and "trips" collection
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val tripsCollection = db.collection("trips")
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // You can also move this to its own file Trip.kt if you want
    data class Trip(
        val name: String? = null,
        val destination: String? = null,
        val dateStart: String? = null,
        val dateEnd: String? = null,
        val description: String? = null,
        val rating: Int? = null,
        val withWhom: String? = null,
        val userId: String? = null
    )

    // Wrapper that keeps Firestore doc id + data
    data class TripItem(
        val id: String,
        val trip: Trip
    )

    // Ensures there's an authenticated user (anonymous) before Firestore calls.
    private fun withAuth(onReady: () -> Unit, onError: (Exception) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            onReady()
            return
        }

        auth.signInAnonymously()
            .addOnSuccessListener { onReady() }
            .addOnFailureListener { e ->
                Log.w("TripRepository", "auth:failure", e)
                onError(e)
            }
    }

    // CREATE – add a new trip
    fun createTrip(trip: Trip, onComplete: (Boolean) -> Unit) {
        withAuth(
            onReady = {
                val userId = auth.currentUser?.uid ?: return@withAuth onComplete(false)
                val tripWithUser = trip.copy(userId = userId)
                tripsCollection
                    .add(tripWithUser)
                    .addOnSuccessListener { onComplete(true) }
                    .addOnFailureListener { e ->
                        Log.w("TripRepository", "createTrip:failure", e)
                        onComplete(false)
                    }
            },
            onError = { _ -> onComplete(false) }
        )
    }

    // READ – get all trips (one-shot)
    fun getTrips(
        onSuccess: (List<TripItem>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        withAuth(
            onReady = {
                val userId = auth.currentUser?.uid ?: return@withAuth onError(Exception("No user"))
                tripsCollection
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val result = mutableListOf<TripItem>()
                        for (doc in querySnapshot.documents) {
                            val trip = doc.toObject(Trip::class.java)
                            val id = doc.id
                            if (trip != null) {
                                result.add(TripItem(id, trip))
                            }
                        }
                        onSuccess(result)
                    }
                    .addOnFailureListener { e ->
                        Log.w("TripRepository", "getTrips:failure", e)
                        onError(e)
                    }
            },
            onError = onError
        )
    }

    // READ – single trip by id (for detail/edit screens)
    fun getTripById(
        id: String,
        onSuccess: (TripItem?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        withAuth(
            onReady = {
                tripsCollection
                    .document(id)
                    .get()
                    .addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            val trip = doc.toObject(Trip::class.java)
                            if (trip != null) {
                                onSuccess(TripItem(doc.id, trip))
                            } else {
                                onSuccess(null)
                            }
                        } else {
                            onSuccess(null)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.w("TripRepository", "getTripById:failure", e)
                        onError(e)
                    }
            },
            onError = onError
        )
    }

    // UPDATE – overwrite an existing trip by its id
    fun updateTrip(
        id: String,
        updatedTrip: Trip,
        onComplete: (Boolean) -> Unit
    ) {
        withAuth(
            onReady = {
                val userId = auth.currentUser?.uid ?: return@withAuth onComplete(false)
                val tripWithUser = updatedTrip.copy(userId = userId)
                tripsCollection
                    .document(id)
                    .set(tripWithUser)
                    .addOnSuccessListener { onComplete(true) }
                    .addOnFailureListener { e ->
                        Log.w("TripRepository", "updateTrip:failure", e)
                        onComplete(false)
                    }
            },
            onError = { _ -> onComplete(false) }
        )
    }

    // DELETE – remove a trip by its id
    fun deleteTrip(
        id: String,
        onComplete: (Boolean) -> Unit
    ) {
        withAuth(
            onReady = {
                tripsCollection
                    .document(id)
                    .delete()
                    .addOnSuccessListener { onComplete(true) }
                    .addOnFailureListener { e ->
                        Log.w("TripRepository", "deleteTrip:failure", e)
                        onComplete(false)
                    }
            },
            onError = { _ -> onComplete(false) }
        )
    }
}
