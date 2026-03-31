package com.example.dailyverse.adapter

import android.graphics.Color
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dailyverse.R
import com.example.dailyverse.data.SavedVerse

class SavedAdapter(
    private val items: MutableList<SavedVerse>,
    private val onDelete: (SavedVerse) -> Unit
) : RecyclerView.Adapter<SavedAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val pip: View      = v.findViewById(R.id.savedPip)
        val ref: TextView  = v.findViewById(R.id.tvSavedRef)
        val text: TextView = v.findViewById(R.id.tvSavedText)
        val del: Button    = v.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_saved, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val v = items[pos]
        h.pip.setBackgroundColor(
            try { Color.parseColor(v.colorHex) } catch (e: Exception) { Color.GRAY }
        )
        h.ref.text  = "${v.categoryIcon}  ${v.ref}"
        h.text.text = v.text
        h.del.setOnClickListener { onDelete(v) }
    }
}