package edu.iu.luddy.c323capstone

import android.content.Context

object TripDraftPrefs {
    private const val FILE = "trip_form_draft"

    private const val K_PLACE = "place"
    private const val K_RATING = "rating"
    private const val K_START = "start"
    private const val K_END = "end"
    private const val K_WITH = "with"
    private const val K_NOTES = "notes"

    data class Draft(
        val place: String,
        val rating: Int,
        val start: Long,
        val end: Long,
        val withWhom: String,
        val notes: String
    )

    fun save(ctx: Context, d: Draft) {
        ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit()
            .putString(K_PLACE, d.place)
            .putInt(K_RATING, d.rating)
            .putLong(K_START, d.start)
            .putLong(K_END, d.end)
            .putString(K_WITH, d.withWhom)
            .putString(K_NOTES, d.notes)
            .apply()
    }

    fun load(ctx: Context): Draft {
        val sp = ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        return Draft(
            place = sp.getString(K_PLACE, "") ?: "",
            rating = sp.getInt(K_RATING, 0),
            start = sp.getLong(K_START, 0L),
            end = sp.getLong(K_END, 0L),
            withWhom = sp.getString(K_WITH, "") ?: "",
            notes = sp.getString(K_NOTES, "") ?: ""
        )
    }

    fun clear(ctx: Context) {
        ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
