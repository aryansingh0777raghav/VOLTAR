package com.voltar.battery.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.SweepGradient

object WidgetGaugeRenderer {

    /**
     * Renders an authentic One UI 9 Frosted Glass squircle background with customizable blur/opacity.
     */
    fun renderGlassBackground(
        context: Context,
        widthDp: Int,
        heightDp: Int,
        opacityPercent: Int,
        isDark: Boolean
    ): Bitmap {
        val density = context.resources.displayMetrics.density
        val w = (widthDp * density).toInt().coerceAtLeast(100)
        val h = (heightDp * density).toInt().coerceAtLeast(100)
        val cornerRadius = 26f * density

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val opacity = opacityPercent.coerceIn(15, 100) / 100f
        val rect = RectF(1.5f * density, 1.5f * density, w - 1.5f * density, h - 1.5f * density)

        // 1. Frosted Glass Body Gradient
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            val topAlpha = (240 * opacity).toInt().coerceIn(20, 255)
            val bottomAlpha = (200 * opacity).toInt().coerceIn(15, 240)

            val (topColor, bottomColor) = if (isDark) {
                Color.argb(topAlpha, 30, 30, 32) to Color.argb(bottomAlpha, 20, 20, 22)
            } else {
                Color.argb(topAlpha, 255, 255, 255) to Color.argb(bottomAlpha, 245, 246, 248)
            }

            shader = LinearGradient(
                0f, 0f, 0f, h.toFloat(),
                topColor, bottomColor,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bodyPaint)

        // 2. Translucent Glass Refraction Rim / Border (One UI aesthetic)
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.4f * density
            val borderAlpha = (180 * opacity).toInt().coerceIn(30, 230)

            val (borderTop, borderBottom) = if (isDark) {
                Color.argb(borderAlpha, 255, 255, 255) to Color.argb((borderAlpha * 0.4f).toInt(), 255, 255, 255)
            } else {
                Color.argb(borderAlpha, 255, 255, 255) to Color.argb((borderAlpha * 0.3f).toInt(), 200, 200, 200)
            }

            shader = LinearGradient(
                0f, 0f, 0f, h.toFloat(),
                borderTop, borderBottom,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint)

        return bitmap
    }

    /**
     * Renders a One UI 9 Frosted Glass Capsule / Pill background for horizontal compact widgets.
     */
    fun renderGlassCapsuleBackground(
        context: Context,
        widthDp: Int,
        heightDp: Int,
        opacityPercent: Int,
        isDark: Boolean
    ): Bitmap {
        val density = context.resources.displayMetrics.density
        val w = (widthDp * density).toInt().coerceAtLeast(100)
        val h = (heightDp * density).toInt().coerceAtLeast(40)
        val cornerRadius = h / 2f

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val opacity = opacityPercent.coerceIn(15, 100) / 100f
        val rect = RectF(1.5f * density, 1.5f * density, w - 1.5f * density, h - 1.5f * density)

        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            val topAlpha = (240 * opacity).toInt().coerceIn(20, 255)
            val bottomAlpha = (200 * opacity).toInt().coerceIn(15, 240)

            val (topColor, bottomColor) = if (isDark) {
                Color.argb(topAlpha, 30, 30, 32) to Color.argb(bottomAlpha, 20, 20, 22)
            } else {
                Color.argb(topAlpha, 255, 255, 255) to Color.argb(bottomAlpha, 245, 246, 248)
            }

            shader = LinearGradient(0f, 0f, 0f, h.toFloat(), topColor, bottomColor, Shader.TileMode.CLAMP)
        }
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bodyPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.4f * density
            val borderAlpha = (180 * opacity).toInt().coerceIn(30, 230)

            val (borderTop, borderBottom) = if (isDark) {
                Color.argb(borderAlpha, 255, 255, 255) to Color.argb((borderAlpha * 0.4f).toInt(), 255, 255, 255)
            } else {
                Color.argb(borderAlpha, 255, 255, 255) to Color.argb((borderAlpha * 0.3f).toInt(), 200, 200, 200)
            }

            shader = LinearGradient(0f, 0f, 0f, h.toFloat(), borderTop, borderBottom, Shader.TileMode.CLAMP)
        }
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint)

        return bitmap
    }

    /**
     * Renders the One UI circular doughnut ring gauge matching the user's reference mockup.
     */
    fun renderDonutGauge(
        context: Context,
        sizeDp: Int,
        healthPercent: Float?,
        levelPercent: Int,
        isDark: Boolean
    ): Bitmap {
        val density = context.resources.displayMetrics.density
        val sizePx = (sizeDp * density).toInt().coerceAtLeast(60)
        val strokeWidth = 11f * density

        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val padding = strokeWidth / 2f + 2f * density
        val rect = RectF(padding, padding, sizePx - padding, sizePx - padding)

        // 1. Background Track Ring
        val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
            color = if (isDark) Color.argb(45, 160, 160, 160) else Color.argb(55, 200, 200, 205)
        }
        canvas.drawOval(rect, trackPaint)

        // 2. Health Progress Arc (One UI Gradient: Emerald Green to Mint Cyan)
        // Only draw when real battery health has been calculated
        if (healthPercent != null && healthPercent > 0f) {
            val sweepAngle = ((healthPercent.coerceIn(0f, 120f) / 100f) * 360f).coerceAtMost(360f)

            val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                this.strokeWidth = strokeWidth
                strokeCap = Paint.Cap.ROUND

                val colors = intArrayOf(
                    Color.parseColor("#00E676"), // Emerald Primary
                    Color.parseColor("#00E5FF"), // Cyan Accent
                    Color.parseColor("#00E676")  // Loop back
                )
                val cx = sizePx / 2f
                val cy = sizePx / 2f
                shader = SweepGradient(cx, cy, colors, floatArrayOf(0f, 0.7f, 1f))
            }

            canvas.save()
            canvas.rotate(-90f, sizePx / 2f, sizePx / 2f)
            canvas.drawArc(rect, 0f, sweepAngle, false, arcPaint)
            canvas.restore()
        }

        return bitmap
    }

    /**
     * Renders a dedicated circular Battery Level dial (0-100%) in One UI 9 style.
     */
    fun renderBatteryLevelDial(
        context: Context,
        sizeDp: Int,
        levelPercent: Int,
        isCharging: Boolean,
        isDark: Boolean
    ): Bitmap {
        val density = context.resources.displayMetrics.density
        val sizePx = (sizeDp * density).toInt().coerceAtLeast(60)
        val strokeWidth = 9.5f * density

        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val padding = strokeWidth / 2f + 2f * density
        val rect = RectF(padding, padding, sizePx - padding, sizePx - padding)

        // 1. Background Track
        val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
            color = if (isDark) Color.argb(45, 160, 160, 160) else Color.argb(55, 200, 200, 205)
        }
        canvas.drawOval(rect, trackPaint)

        // 2. Battery Level Progress Arc
        val sweepAngle = ((levelPercent.coerceIn(0, 100) / 100f) * 360f).coerceAtMost(360f)
        if (sweepAngle > 0f) {
            val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                this.strokeWidth = strokeWidth
                strokeCap = Paint.Cap.ROUND

                val (c1, c2) = when {
                    levelPercent <= 20 && !isCharging -> Color.parseColor("#FF5252") to Color.parseColor("#FF1744")
                    isCharging -> Color.parseColor("#00E676") to Color.parseColor("#00E5FF")
                    else -> Color.parseColor("#00E676") to Color.parseColor("#00C853")
                }
                val cx = sizePx / 2f
                val cy = sizePx / 2f
                shader = SweepGradient(cx, cy, intArrayOf(c1, c2, c1), floatArrayOf(0f, 0.7f, 1f))
            }

            canvas.save()
            canvas.rotate(-90f, sizePx / 2f, sizePx / 2f)
            canvas.drawArc(rect, 0f, sweepAngle, false, arcPaint)
            canvas.restore()
        }

        return bitmap
    }

    /**
     * Renders a mini circular battery dial (approx 36-44dp) for the compact 2x1 capsule.
     */
    fun renderMiniBatteryDial(
        context: Context,
        sizeDp: Int,
        levelPercent: Int,
        isCharging: Boolean,
        isDark: Boolean
    ): Bitmap {
        val density = context.resources.displayMetrics.density
        val sizePx = (sizeDp * density).toInt().coerceAtLeast(36)
        val strokeWidth = 4.5f * density

        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val padding = strokeWidth / 2f + 1.5f * density
        val rect = RectF(padding, padding, sizePx - padding, sizePx - padding)

        val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
            color = if (isDark) Color.argb(50, 160, 160, 160) else Color.argb(60, 200, 200, 205)
        }
        canvas.drawOval(rect, trackPaint)

        val sweepAngle = ((levelPercent.coerceIn(0, 100) / 100f) * 360f).coerceAtMost(360f)
        if (sweepAngle > 0f) {
            val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                this.strokeWidth = strokeWidth
                strokeCap = Paint.Cap.ROUND
                color = when {
                    levelPercent <= 20 && !isCharging -> Color.parseColor("#FF5252")
                    isCharging -> Color.parseColor("#00E5FF")
                    else -> Color.parseColor("#00E676")
                }
            }

            canvas.save()
            canvas.rotate(-90f, sizePx / 2f, sizePx / 2f)
            canvas.drawArc(rect, 0f, sweepAngle, false, arcPaint)
            canvas.restore()
        }

        return bitmap
    }

    /**
     * Renders a circular thermal badge with temperature-based dynamic color.
     */
    fun renderThermalBadge(
        context: Context,
        sizeDp: Int,
        tempCelsius: Int,
        isDark: Boolean
    ): Bitmap {
        val density = context.resources.displayMetrics.density
        val sizePx = (sizeDp * density).toInt().coerceAtLeast(32)

        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val cx = sizePx / 2f
        val cy = sizePx / 2f
        val radius = (sizePx / 2f) - (2f * density)

        val (accentColor, bgAlpha) = when {
            tempCelsius >= 42 -> Color.parseColor("#FF5252") to 45
            tempCelsius >= 38 -> Color.parseColor("#FFA000") to 40
            tempCelsius <= 15 -> Color.parseColor("#00B0FF") to 40
            else -> Color.parseColor("#00E676") to 35
        }

        // Ambient background circle
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.argb(bgAlpha, Color.red(accentColor), Color.green(accentColor), Color.blue(accentColor))
        }
        canvas.drawCircle(cx, cy, radius, bgPaint)

        // Outer rim
        val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2.5f * density
            color = accentColor
        }
        canvas.drawCircle(cx, cy, radius, rimPaint)

        // Center dot
        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = accentColor
        }
        canvas.drawCircle(cx, cy, 4f * density, dotPaint)

        return bitmap
    }
}
