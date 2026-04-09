package com.sharmadipanshu.aistudybuddy.repository

import android.content.Context
import com.sharmadipanshu.aistudybuddy.models.PDFDocument
import com.sharmadipanshu.aistudybuddy.models.HighlightArea
import com.sharmadipanshu.aistudybuddy.models.SelectedSection
import com.sharmadipanshu.aistudybuddy.utils.NoteSectionExtractor
import com.sharmadipanshu.aistudybuddy.utils.PdfTextExtractor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PDFRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val noteSectionExtractor: NoteSectionExtractor,
    private val pdfTextExtractor: PdfTextExtractor,
    private val okHttpClient: OkHttpClient
) {

    suspend fun resolvePdfFile(document: PDFDocument): File = withContext(Dispatchers.IO) {
        document.localPath?.let { localPath ->
            val localFile = File(localPath)
            if (localFile.exists()) return@withContext localFile
        }

        val remoteUrl = document.remoteUrl
            ?: throw IllegalArgumentException("PDF file path or URL is required.")

        val cachedFile = File(
            context.cacheDir,
            "${document.noteId.ifBlank { document.title }}_${document.title.replace("[^A-Za-z0-9._-]".toRegex(), "_")}.pdf"
        )

        if (cachedFile.exists() && cachedFile.length() > 0L) {
            cachedFile.inputStream().use { input ->
                val header = ByteArray(4)
                if (input.read(header) == 4 && header.decodeToString() == "%PDF") {
                    return@withContext cachedFile
                }
            }
            cachedFile.delete()
        }

        val requestBuilder = Request.Builder()
            .url(remoteUrl)

        if (remoteUrl.contains("/uploads/")) {
            requestBuilder.header("Accept", "application/pdf")
        }

        val request = requestBuilder.build()

        okHttpClient.newCall(request).execute().use { response ->
            val contentType = response.body?.contentType()?.toString().orEmpty()

            if (!response.isSuccessful || response.body == null) {
                throw IllegalStateException("Unable to load PDF document.")
            }

            if (!contentType.contains("pdf", ignoreCase = true)) {
                throw IllegalStateException("The downloaded file is not a valid PDF.")
            }

            cachedFile.outputStream().use { outputStream ->
                response.body!!.byteStream().copyTo(outputStream)
            }

            cachedFile.inputStream().use { input ->
                val header = ByteArray(4)
                if (input.read(header) != 4 || header.decodeToString() != "%PDF") {
                    cachedFile.delete()
                    throw IllegalStateException("Downloaded file is corrupted or not a PDF.")
                }
            }

            return@withContext cachedFile
        }
    }

    suspend fun extractPageText(pdfFile: File, page: Int): String = withContext(Dispatchers.IO) {
        pdfTextExtractor.extractPageText(pdfFile, page)
    }

    suspend fun extractSection(
        noteId: String,
        pageNumber: Int,
        xCoordinate: Float,
        yCoordinate: Float,
        viewWidth: Float,
        viewHeight: Float,
        pageText: String
    ): Pair<SelectedSection, HighlightArea> = withContext(Dispatchers.Default) {
        noteSectionExtractor.extractSection(
            noteId = noteId,
            pageNumber = pageNumber,
            xCoordinate = xCoordinate,
            yCoordinate = yCoordinate,
            viewWidth = viewWidth,
            viewHeight = viewHeight,
            pageText = pageText
        )
    }
}
