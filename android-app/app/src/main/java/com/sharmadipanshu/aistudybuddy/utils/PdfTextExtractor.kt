package com.sharmadipanshu.aistudybuddy.utils

import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfTextExtractor @Inject constructor() {

    fun extractPageText(pdfFile: File, page: Int): String {
        PDDocument.load(pdfFile).use { document ->
            if (page < 0 || page >= document.numberOfPages) {
                return ""
            }

            val stripper = PDFTextStripper().apply {
                startPage = page + 1
                endPage = page + 1
            }

            return stripper.getText(document).trim()
        }
    }
}
