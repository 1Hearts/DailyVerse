package com.example.dailyverse.adapter

import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dailyverse.R
import com.example.dailyverse.data.Category

class CategoryAdapter(
    private val items: List<Category>,
    private val onClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.VH>() {

    private var selectedPos = -1

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val bar: View      = v.findViewById(R.id.colorBar)
        val icon: TextView = v.findViewById(R.id.tvCatIcon)
        val name: TextView = v.findViewById(R.id.tvCatName)
        val sub: TextView  = v.findViewById(R.id.tvCatSub)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val cat = items[pos]
        h.icon.text = cat.icon
        h.name.text = cat.name
        h.sub.text  = cat.sub

        // 테두리 스타일 적용
        if (pos == selectedPos) {
            applyBorder(h, cat.colorHex, true)
        } else {
            applyBorder(h, "#e8e2d9", false)
            h.bar.setBackgroundColor(Color.TRANSPARENT)
        }

        h.itemView.setOnClickListener {
            val prev = selectedPos
            selectedPos = pos
            if (prev != -1) notifyItemChanged(prev)
            notifyItemChanged(pos)

            // 상단 컬러바 왼쪽→오른쪽 애니메이션
            animateBar(h.bar, cat.colorHex)

            onClick(cat)
        }
    }

    // 선택 초기화 (카테고리 화면으로 돌아올 때 호출)
    fun clearSelection() {
        val prev = selectedPos
        selectedPos = -1
        if (prev != -1) {
            notifyItemChanged(prev)
        }
        // 전체 갱신으로 잔상 완전 제거
        notifyDataSetChanged()
    }

    private fun applyBorder(h: VH, colorHex: String, selected: Boolean) {
        val drawable = GradientDrawable().apply {
            setColor(Color.WHITE)
            setStroke(if (selected) 4 else 1, Color.parseColor(colorHex))
            cornerRadius = 8f
        }
        h.itemView.background = drawable
    }

    private fun animateBar(bar: View, colorHex: String) {
        val color = Color.parseColor(colorHex)
        bar.setBackgroundColor(Color.TRANSPARENT)

        // 너비 0 → 전체 너비로 애니메이션
        bar.post {
            val fullWidth = bar.width
            val animator = ValueAnimator.ofInt(0, fullWidth).apply {
                duration = 300
                addUpdateListener { anim ->
                    val width = anim.animatedValue as Int
                    val drawable = GradientDrawable().apply {
                        setColor(color)
                        setSize(width, bar.height)
                    }
                    bar.background = drawable
                    bar.requestLayout()
                }
            }
            animator.start()
        }
    }
}