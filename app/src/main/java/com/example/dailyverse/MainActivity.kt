package com.example.dailyverse

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dailyverse.adapter.CategoryAdapter
import com.example.dailyverse.data.VerseData
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<TextView>(R.id.tvDate).text =
            SimpleDateFormat("yyyy년 M월 d일 EEEE", Locale.KOREAN).format(Date())

        categoryAdapter = CategoryAdapter(VerseData.categories) { cat ->
            val intent = Intent(this@MainActivity, VerseActivity::class.java)
            intent.putExtra("categoryId", cat.id)
            startActivity(intent)
        }

        findViewById<RecyclerView>(R.id.rvCategories).apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = categoryAdapter
        }
    }

    // VerseActivity에서 돌아올 때 선택 초기화
    override fun onResume() {
        super.onResume()
        categoryAdapter.clearSelection()
    }
}