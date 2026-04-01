package com.example.dailyverse

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dailyverse.adapter.CategoryAdapter
import com.example.dailyverse.adapter.SavedAdapter
import com.example.dailyverse.data.SavedVerse
import com.example.dailyverse.data.SavedVerseManager
import com.example.dailyverse.data.VerseData
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var savedAdapter: SavedAdapter
    private val savedList = mutableListOf<SavedVerse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 날짜
        findViewById<TextView>(R.id.tvDate).text =
            SimpleDateFormat("yyyy년 M월 d일 EEEE", Locale.KOREAN).format(Date())

        // 카테고리 그리드
        categoryAdapter = CategoryAdapter(VerseData.categories) { cat ->
            startActivity(
                Intent(this@MainActivity, VerseActivity::class.java)
                    .putExtra("categoryId", cat.id)
            )
        }
        findViewById<RecyclerView>(R.id.rvCategories).apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = categoryAdapter
        }

        // 저장된 말씀 어댑터
        savedAdapter = SavedAdapter(savedList) { verse ->
            SavedVerseManager.delete(this, verse.ref)
            savedList.remove(verse)
            savedAdapter.notifyDataSetChanged()
            updateSavedSection()
        }
        findViewById<RecyclerView>(R.id.rvSavedMain).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = savedAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        // 카테고리 선택 초기화
        categoryAdapter.clearSelection()

        // 저장된 말씀 갱신
        savedList.clear()
        savedList.addAll(SavedVerseManager.getAll(this))
        savedAdapter.notifyDataSetChanged()
        updateSavedSection()
    }

    private fun updateSavedSection() {
        findViewById<LinearLayout>(R.id.savedSection).visibility =
            if (savedList.isEmpty()) View.GONE else View.VISIBLE
    }
}