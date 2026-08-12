package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun QrCodeCanvas(
    payload: String,
    modifier: Modifier = Modifier,
    darkColor: Color = Color(0xFF0F172A),
    lightColor: Color = Color.White
) {
    val gridSize = 21 // Standard QR version 1 matrix size
    val hash = payload.hashCode()

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(lightColor)
            .padding(16.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val moduleWidth = size.width / gridSize
            val moduleHeight = size.height / gridSize

            // Draw white background
            drawRect(color = lightColor, size = size)

            // Function to check if inside locator box
            fun isCornerFinder(r: Int, c: Int): Boolean {
                val inTopLeft = r < 7 && c < 7
                val inTopRight = r < 7 && c >= gridSize - 7
                val inBottomLeft = r >= gridSize - 7 && c < 7
                return inTopLeft || inTopRight || inBottomLeft
            }

            // Draw Finder Patterns (Top-Left, Top-Right, Bottom-Left)
            fun drawFinderPattern(startR: Int, startC: Int) {
                // Outer 7x7 dark box
                drawRect(
                    color = darkColor,
                    topLeft = Offset(startC * moduleWidth, startR * moduleHeight),
                    size = Size(7 * moduleWidth, 7 * moduleHeight)
                )
                // Inner 5x5 light box
                drawRect(
                    color = lightColor,
                    topLeft = Offset((startC + 1) * moduleWidth, (startR + 1) * moduleHeight),
                    size = Size(5 * moduleWidth, 5 * moduleHeight)
                )
                // Center 3x3 dark box
                drawRect(
                    color = darkColor,
                    topLeft = Offset((startC + 2) * moduleWidth, (startR + 2) * moduleHeight),
                    size = Size(3 * moduleWidth, 3 * moduleHeight)
                )
            }

            drawFinderPattern(0, 0)
            drawFinderPattern(0, gridSize - 7)
            drawFinderPattern(gridSize - 7, 0)

            // Draw pseudo-random data modules based on payload hash
            for (r in 0 until gridSize) {
                for (c in 0 until gridSize) {
                    if (isCornerFinder(r, c)) continue

                    // Timing pattern lines
                    if (r == 6 || c == 6) {
                        if ((r + c) % 2 == 0) {
                            drawRect(
                                color = darkColor,
                                topLeft = Offset(c * moduleWidth, r * moduleHeight),
                                size = Size(moduleWidth, moduleHeight)
                            )
                        }
                        continue
                    }

                    // Deterministic module generation using pseudo-random hashing
                    val cellHash = abs((payload + "$r,$c").hashCode() xor hash)
                    if (cellHash % 3 == 0) {
                        drawRect(
                            color = darkColor,
                            topLeft = Offset(c * moduleWidth, r * moduleHeight),
                            size = Size(moduleWidth, moduleHeight)
                        )
                    }
                }
            }
        }
    }
}
