package com.sharmadipanshu.aistudybuddy.utils

import com.sharmadipanshu.aistudybuddy.models.HighlightArea
import com.sharmadipanshu.aistudybuddy.models.SelectedSection
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min

@Singleton
class NoteSectionExtractor @Inject constructor() {

    private fun splitIntoSelectableUnits(pageText: String): List<String> {
        val normalized = pageText.replace("\r\n", "\n").trim()
        if (normalized.isBlank()) {
            return emptyList()
        }

        val lines = normalized
            .split("\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (lines.size >= 3) {
            return lines
        }

        val sentences = normalized
            .split(Regex("(?<=[.!?])\\s+"))
            .map { it.trim() }
            .filter { it.isNotBlank() }

        return if (sentences.isNotEmpty()) sentences else listOf(normalized)
    }

    fun extractSection(
        noteId: String,
        pageNumber: Int,
        xCoordinate: Float,
        yCoordinate: Float,
        viewWidth: Float,
        viewHeight: Float,
        pageText: String
    ): Pair<SelectedSection, HighlightArea> {
        val units = splitIntoSelectableUnits(pageText)
            .ifEmpty { listOf(pageText.trim()) }

        val normalizedY = if (viewHeight > 0f) (yCoordinate / viewHeight).coerceIn(0f, 1f) else 0f
        val centerIndex = min(
            max((normalizedY * units.size).toInt(), 0),
            units.lastIndex
        )

        val windowSize = min(3, max(units.size, 1))
        val startIndex = max(centerIndex - windowSize / 2, 0)
        val endIndexExclusive = min(startIndex + windowSize, units.size)
        val selectedText = units
            .subList(startIndex, endIndexExclusive)
            .joinToString("\n")
            .ifBlank { pageText.trim() }

        val unitHeight = if (viewHeight > 0f) (viewHeight / max(units.size, 1)) else 0f
        val sectionHeight = if (unitHeight > 0f) {
            max(unitHeight * (endIndexExclusive - startIndex), 72f)
        } else {
            96f
        }
        val top = if (unitHeight > 0f) {
            (startIndex * unitHeight).coerceAtMost(max(viewHeight - sectionHeight, 0f))
        } else {
            0f
        }

        val highlightArea = HighlightArea(
            x = 16f,
            y = top,
            width = max(viewWidth - 32f, 0f),
            height = min(sectionHeight, max(viewHeight - top, 0f))
        )

        return SelectedSection(
            noteId = noteId,
            pageNumber = pageNumber,
            xCoordinate = xCoordinate,
            yCoordinate = yCoordinate,
            selectedText = selectedText
        ) to highlightArea
    }
}
