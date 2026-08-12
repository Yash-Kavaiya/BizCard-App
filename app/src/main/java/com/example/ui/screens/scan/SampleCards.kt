package com.example.ui.screens.scan

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

object SampleCards {

    fun generateTechCardBitmap(): Pair<Bitmap, String> {
        val width = 800
        val height = 480
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        val bgPaint = Paint().apply { color = Color.parseColor("#0F172A") }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Decorative Accent Line
        val accentPaint = Paint().apply { color = Color.parseColor("#6366F1") }
        canvas.drawRect(0f, 0f, 24f, height.toFloat(), accentPaint)

        // Text Paints
        val namePaint = Paint().apply {
            color = Color.WHITE
            textSize = 38f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val titlePaint = Paint().apply {
            color = Color.parseColor("#94A3B8")
            textSize = 24f
            isAntiAlias = true
        }

        val companyPaint = Paint().apply {
            color = Color.parseColor("#818CF8")
            textSize = 30f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val contactPaint = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            textSize = 20f
            isAntiAlias = true
        }

        canvas.drawText("Dr. Vikram Malhotra", 60f, 100f, namePaint)
        canvas.drawText("VP of Artificial Intelligence", 60f, 140f, titlePaint)
        canvas.drawText("NEXUS AI LABS", 60f, 210f, companyPaint)

        canvas.drawText("Phone: +1 (650) 555-0199", 60f, 290f, contactPaint)
        canvas.drawText("Email: v.malhotra@nexusailabs.com", 60f, 330f, contactPaint)
        canvas.drawText("Web: www.nexusailabs.com", 60f, 370f, contactPaint)
        canvas.drawText("HQ: 100 Innovation Way, Palo Alto, CA 94301", 60f, 410f, contactPaint)

        val rawText = """
            Dr. Vikram Malhotra
            VP of Artificial Intelligence
            NEXUS AI LABS
            Phone: +1 (650) 555-0199
            Email: v.malhotra@nexusailabs.com
            Web: www.nexusailabs.com
            HQ: 100 Innovation Way, Palo Alto, CA 94301
        """.trimIndent()

        return bitmap to rawText
    }

    fun generateGujaratiCardBitmap(): Pair<Bitmap, String> {
        val width = 800
        val height = 480
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        val bgPaint = Paint().apply { color = Color.parseColor("#FFFBEB") }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val borderPaint = Paint().apply {
            color = Color.parseColor("#B45309")
            style = Paint.Style.STROKE
            strokeWidth = 10f
        }
        canvas.drawRect(20f, 20f, width - 20f, height - 20f, borderPaint)

        val namePaint = Paint().apply {
            color = Color.parseColor("#78350F")
            textSize = 36f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val companyPaint = Paint().apply {
            color = Color.parseColor("#B45309")
            textSize = 32f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val detailPaint = Paint().apply {
            color = Color.parseColor("#1F2937")
            textSize = 22f
            isAntiAlias = true
        }

        canvas.drawText("Suresh Patel (સુરેશ પટેલ)", 60f, 100f, namePaint)
        canvas.drawText("Managing Director - MD", 60f, 140f, detailPaint)
        canvas.drawText("GUJARAT TEXTILE EXPORTS", 60f, 200f, companyPaint)

        canvas.drawText("Phone: +91 98251 99887", 60f, 270f, detailPaint)
        canvas.drawText("Email: suresh@gujartextile.in", 60f, 310f, detailPaint)
        canvas.drawText("Website: www.gujarattextile.in", 60f, 350f, detailPaint)
        canvas.drawText("Textiles, Silk, Cotton & Garment Export", 60f, 400f, detailPaint)

        val rawText = """
            Suresh Patel (સુરેશ પટેલ)
            Managing Director - MD
            GUJARAT TEXTILE EXPORTS
            Phone: +91 98251 99887
            Email: suresh@gujartextile.in
            Website: www.gujarattextile.in
            Textiles, Silk, Cotton & Garment Export
        """.trimIndent()

        return bitmap to rawText
    }
}
