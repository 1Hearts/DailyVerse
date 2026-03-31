package com.example.dailyverse.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SavedVerseManager {
    private const val KEY = "saved_verses"
    private val gson = Gson()

    fun save(context: Context, verse: SavedVerse) {
        val list = getAll(context).toMutableList()
        if (list.none { it.ref == verse.ref }) { list.add(0, verse); write(context, list) }
    }
    fun delete(context: Context, ref: String) = write(context, getAll(context).filter { it.ref != ref })
    fun isSaved(context: Context, ref: String) = getAll(context).any { it.ref == ref }
    fun getAll(context: Context): List<SavedVerse> {
        val json = prefs(context).getString(KEY, "[]") ?: "[]"
        return gson.fromJson(json, object : TypeToken<List<SavedVerse>>() {}.type)
    }
    private fun write(context: Context, list: List<SavedVerse>) =
        prefs(context).edit().putString(KEY, gson.toJson(list)).apply()
    private fun prefs(context: Context) =
        context.getSharedPreferences("daily_verse", Context.MODE_PRIVATE)
}
