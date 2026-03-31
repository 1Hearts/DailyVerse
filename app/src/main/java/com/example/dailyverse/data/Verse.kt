package com.example.dailyverse.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Verse(
    val text: String,
    val ref: String,
    val reflection: String
) : Parcelable

@Parcelize
data class Category(
    val id: String,
    val name: String,
    val icon: String,
    val colorHex: String,
    val sub: String
) : Parcelable

data class SavedVerse(
    val text: String,
    val ref: String,
    val reflection: String,
    val categoryName: String,
    val categoryIcon: String,
    val colorHex: String
)
