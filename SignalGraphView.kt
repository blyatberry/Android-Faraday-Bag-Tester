package com.example.faradaybagtester

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

class SignalGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val cellularPaint = Paint().apply {
        color = Color.rgb(33, 150, 243) // Blue
        strokeWidth = 4f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val wifiPaint = Paint().apply {
        color = Color.rgb(76, 175, 80) // Green
        strokeWidth = 4f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val bluetoothPaint = Paint().apply {
        color = Color.rgb(156, 39, 176) // Purple
        strokeWidth = 4f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val gridPaint = Paint().apply {
        color = Color.LTGRAY
        strokeWidth = 1f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = Color.DKGRAY
        textSize = 24f
        isAntiAlias = true
    }

    private var cellularData = listOf<Int>()
    private var wifiData = listOf<Int>()
    private var bluetoothData = listOf<Int>()
    
    private var cellularEnabled = true
    private var wifiEnabled = true
    private var bluetoothEnabled = true

    fun updateData(
        cellular: List<Int>, 
        wifi: List<Int>, 
        bluetooth: List<Int>,
        cellEnabled: Boolean = true,
        wifiEn: Boolean = true,
        btEnabled: Boolean = true
    ) {
        cellularData = cellular.toList()
        wifiData = wifi.toList()
        bluetoothData = bluetooth.toList()
        cellularEnabled = cellEnabled
        wifiEnabled = wifiEn
        bluetoothEnabled = btEnabled
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val padding = 50f

        // Draw background
        canvas.drawColor(Color.WHITE)

        // Draw grid
        drawGrid(canvas, width, height, padding)

        // Draw data lines
        if (cellularData.isNotEmpty() && cellularEnabled) {
            drawDataLine(canvas, cellularData, cellularPaint, width, height, padding)
        }
        if (wifiData.isNotEmpty() && wifiEnabled) {
            drawDataLine(canvas, wifiData, wifiPaint, width, height, padding)
        }
        if (bluetoothData.isNotEmpty() && bluetoothEnabled) {
            drawDataLine(canvas, bluetoothData, bluetoothPaint, width, height, padding)
        }

        // Draw legend
        drawLegend(canvas, width, height, padding)
    }

    private fun drawGrid(canvas: Canvas, width: Float, height: Float, padding: Float) {
        val graphHeight = height - 2 * padding
        val graphWidth = width - 2 * padding

        // Horizontal lines
        for (i in 0..4) {
            val y = padding + (graphHeight / 4) * i
            canvas.drawLine(padding, y, width - padding, y, gridPaint)
            
            // Y-axis labels
            val label = (4 - i).toString()
            canvas.drawText(label, padding - 30, y + 8, textPaint)
        }

        // Vertical lines
        val numVerticalLines = 5
        for (i in 0..numVerticalLines) {
            val x = padding + (graphWidth / numVerticalLines) * i
            canvas.drawLine(x, padding, x, height - padding, gridPaint)
        }
    }

    private fun drawDataLine(
        canvas: Canvas,
        data: List<Int>,
        paint: Paint,
        width: Float,
        height: Float,
        padding: Float
    ) {
        if (data.isEmpty()) return

        val graphHeight = height - 2 * padding
        val graphWidth = width - 2 * padding

        val path = Path()
        val xStep = if (data.size > 1) graphWidth / (data.size - 1) else 0f

        data.forEachIndexed { index, value ->
            val x = padding + index * xStep
            val y = padding + graphHeight - (value.toFloat() / 4f * graphHeight)

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }

            // Draw point
            canvas.drawCircle(x, y, 5f, paint)
        }

        canvas.drawPath(path, paint)
    }

    private fun drawLegend(canvas: Canvas, width: Float, height: Float, padding: Float) {
        val legendY = height - padding / 2
        val legendStartX = padding
        val legendSpacing = 150f

        val disabledPaint = Paint(textPaint).apply {
            alpha = 128
        }

        // Cellular
        val cellPaint = if (cellularEnabled) cellularPaint else Paint(cellularPaint).apply { alpha = 128 }
        canvas.drawLine(legendStartX, legendY, legendStartX + 30, legendY, cellPaint)
        canvas.drawText("Mobilfunk", legendStartX + 40, legendY + 8, 
            if (cellularEnabled) textPaint else disabledPaint)

        // WiFi
        val wPaint = if (wifiEnabled) wifiPaint else Paint(wifiPaint).apply { alpha = 128 }
        canvas.drawLine(legendStartX + legendSpacing, legendY, 
            legendStartX + legendSpacing + 30, legendY, wPaint)
        canvas.drawText("WLAN", legendStartX + legendSpacing + 40, legendY + 8, 
            if (wifiEnabled) textPaint else disabledPaint)

        // Bluetooth
        val btPaint = if (bluetoothEnabled) bluetoothPaint else Paint(bluetoothPaint).apply { alpha = 128 }
        canvas.drawLine(legendStartX + legendSpacing * 2, legendY, 
            legendStartX + legendSpacing * 2 + 30, legendY, btPaint)
        canvas.drawText("Bluetooth", legendStartX + legendSpacing * 2 + 40, legendY + 8, 
            if (bluetoothEnabled) textPaint else disabledPaint)
    }
}
