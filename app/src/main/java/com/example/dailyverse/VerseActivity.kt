package com.example.dailyverse

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dailyverse.adapter.SavedAdapter
import com.example.dailyverse.data.*

class VerseActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_STORAGE = 1001
    }

    private lateinit var category: Category
    private lateinit var currentVerse: Verse
    private lateinit var savedAdapter: SavedAdapter
    private val savedList = mutableListOf<SavedVerse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verse)

        val categoryId = intent.getStringExtra("categoryId")
        if (categoryId == null) {
            finish()
            return
        }
        category = VerseData.categories.first { it.id == categoryId }

        // 카테고리 라벨
        findViewById<TextView>(R.id.tvCatLabel).apply {
            text = "${category.icon}  ${category.name}  —  ${category.sub}"
            setTextColor(Color.parseColor(category.colorHex))
        }
        // 카드 상단 컬러바
        findViewById<View>(R.id.cardTopBar)
            .setBackgroundColor(Color.parseColor(category.colorHex))

        // 뒤로가기
        findViewById<TextView>(R.id.tvBack).setOnClickListener { finish() }

        // 저장 목록 초기화
        savedList.addAll(SavedVerseManager.getAll(this))
        savedAdapter = SavedAdapter(savedList) { verse ->
            SavedVerseManager.delete(this, verse.ref)
            savedList.remove(verse)
            savedAdapter.notifyDataSetChanged()
            updateSavedSection()
        }
        findViewById<RecyclerView>(R.id.rvSaved).apply {
            layoutManager = LinearLayoutManager(this@VerseActivity)
            adapter = savedAdapter
        }
        updateSavedSection()

        // 첫 말씀
        showVerse(VerseData.getRandom(category.id))

        // 다시뽑기
        findViewById<Button>(R.id.btnDraw).setOnClickListener {
            showVerse(VerseData.getRandom(category.id))
        }

        // 저장
        findViewById<Button>(R.id.btnSave).setOnClickListener {
            if (SavedVerseManager.isSaved(this, currentVerse.ref)) {
                Toast.makeText(this, "이미 저장된 말씀입니다", Toast.LENGTH_SHORT).show()
            } else {
                val saved = SavedVerse(
                    currentVerse.text, currentVerse.ref, currentVerse.reflection,
                    category.name, category.icon, category.colorHex
                )
                SavedVerseManager.save(this, saved)
                savedList.add(0, saved)
                savedAdapter.notifyDataSetChanged()
                updateSavedSection()
                (it as Button).text = "♥ 저장됨"
                Toast.makeText(this, "말씀이 저장되었습니다 🙏", Toast.LENGTH_SHORT).show()
            }
        }

        // 사진저장
        findViewById<Button>(R.id.btnSaveImg).setOnClickListener {
            saveCardToGallery()
        }

        // 공유
        findViewById<Button>(R.id.btnShare).setOnClickListener {
            startActivity(Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT,
                        "\"${currentVerse.text}\"\n— ${currentVerse.ref}\n\n#오늘의말씀")
                }, "말씀 공유"
            ))
        }
    }

    private fun showVerse(verse: Verse) {
        currentVerse = verse
        findViewById<TextView>(R.id.tvVerseText).text = verse.text
        findViewById<TextView>(R.id.tvVerseRef).text = "— ${verse.ref}"
        findViewById<TextView>(R.id.tvReflection).text = verse.reflection
        findViewById<Button>(R.id.btnSave).text =
            if (SavedVerseManager.isSaved(this, verse.ref)) "♥ 저장됨" else "♡ 저장"
    }

    private fun updateSavedSection() {
        findViewById<LinearLayout>(R.id.savedSection).visibility =
            if (savedList.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun saveCardToGallery() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_STORAGE
                )
                return
            }
        }

        val card = findViewById<CardView>(R.id.verseCard)
        card.post {
            try {
                val bmp = Bitmap.createBitmap(card.width, card.height, Bitmap.Config.ARGB_8888)
                Canvas(bmp).also { card.draw(it) }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val values = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, "말씀_${System.currentTimeMillis()}.png")
                        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/오늘의말씀")
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                    val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    uri?.let {
                        contentResolver.openOutputStream(it)?.use { out ->
                            bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
                        }
                        values.clear()
                        values.put(MediaStore.Images.Media.IS_PENDING, 0)
                        contentResolver.update(it, values, null, null)
                        Toast.makeText(this, "사진첩에 저장되었습니다 🙏", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Android 9 이하 (노트8)
                    val dir = android.os.Environment.getExternalStoragePublicDirectory(
                        android.os.Environment.DIRECTORY_PICTURES
                    )
                    val folder = java.io.File(dir, "오늘의말씀")
                    if (!folder.exists()) folder.mkdirs()
                    val file = java.io.File(folder, "말씀_${System.currentTimeMillis()}.png")
                    java.io.FileOutputStream(file).use { out ->
                        bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    android.media.MediaScannerConnection.scanFile(
                        this, arrayOf(file.absolutePath), arrayOf("image/png"), null
                    )
                    Toast.makeText(this, "사진첩에 저장되었습니다 🙏", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_STORAGE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            saveCardToGallery()
        }
    }
}