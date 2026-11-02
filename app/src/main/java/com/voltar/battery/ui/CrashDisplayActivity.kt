package com.voltar.battery.ui

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.voltar.battery.MainActivity

class CrashDisplayActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val error = intent.getStringExtra("error") ?: "Unknown error"

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 40)
            setBackgroundColor(0xFF080808.toInt())
        }

        val title = TextView(this).apply {
            text = "VOLTAR Diagnostic Crash Log"
            setTextColor(0xFF00E676.toInt())
            textSize = 20f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        layout.addView(title)

        val desc = TextView(this).apply {
            text = "An exception occurred during launch. Please review or copy this log:"
            setTextColor(0xFFA0A0A0.toInt())
            textSize = 13f
            setPadding(0, 16, 0, 24)
        }
        layout.addView(desc)

        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        val errorView = TextView(this).apply {
            text = error
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 12f
            typeface = android.graphics.Typeface.MONOSPACE
            setTextIsSelectable(true)
        }
        scrollView.addView(errorView)
        layout.addView(scrollView)

        val btnRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 20, 0, 0)
        }

        val copyBtn = Button(this).apply {
            text = "Copy Error"
            setBackgroundColor(0xFF222222.toInt())
            setTextColor(0xFFFFFFFF.toInt())
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = 16
            }
            setOnClickListener {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("VOLTAR Crash Log", error)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this@CrashDisplayActivity, "Error copied to clipboard", Toast.LENGTH_SHORT).show()
            }
        }
        btnRow.addView(copyBtn)

        val restartBtn = Button(this).apply {
            text = "Restart App"
            setBackgroundColor(0xFF00E676.toInt())
            setTextColor(0xFF000000.toInt())
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener {
                val restart = Intent(this@CrashDisplayActivity, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(restart)
                finish()
            }
        }
        btnRow.addView(restartBtn)

        layout.addView(btnRow)

        setContentView(layout)
    }
}
