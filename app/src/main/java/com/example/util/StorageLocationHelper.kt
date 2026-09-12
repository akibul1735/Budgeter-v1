package com.example.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import androidx.activity.result.contract.ActivityResultContract
import java.io.File

object StorageLocationHelper {

    val CSV_EXCEL_MIME_TYPES = arrayOf(
        "text/csv",
        "text/comma-separated-values",
        "application/csv",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/vnd.oasis.opendocument.spreadsheet",
        "text/tab-separated-values",
        "text/plain"
    )

    val JSON_MIME_TYPES = arrayOf(
        "application/json",
        "text/json",
        "application/octet-stream"
    )

    val QIF_MIME_TYPES = arrayOf(
        "application/qif",
        "text/qif",
        "text/plain",
        "application/octet-stream"
    )

    /**
     * Obtains the DocumentsContract EXTRA_INITIAL_URI pointing to the user's selected
     * local storage location (defaults to "Documents/Budgeter").
     */
    fun getInitialStorageUri(context: Context, customDirectoryPath: String? = null): Uri? {
        val dirPath = customDirectoryPath ?: BackupPreferences.getInstance(context).getLocalBackupDirectory()
        return try {
            if (dirPath.startsWith("content://")) {
                val uri = Uri.parse(dirPath)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (DocumentsContract.isTreeUri(uri)) {
                        val docId = DocumentsContract.getTreeDocumentId(uri)
                        DocumentsContract.buildDocumentUriUsingTree(uri, docId)
                    } else {
                        uri
                    }
                } else {
                    uri
                }
            } else {
                // Ensure local Budgeter folder exists
                try {
                    val publicDocs = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Budgeter")
                    if (!publicDocs.exists()) publicDocs.mkdirs()
                } catch (_: Exception) {}

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val docId = when {
                        dirPath.equals("Documents/Budgeter", ignoreCase = true) || dirPath.startsWith("Documents") -> "primary:Documents/Budgeter"
                        dirPath.equals("Downloads/Budgeter", ignoreCase = true) || dirPath.startsWith("Downloads") -> "primary:Download/Budgeter"
                        dirPath.contains("Download", ignoreCase = true) -> "primary:Download"
                        dirPath.contains("Documents", ignoreCase = true) -> "primary:Documents"
                        dirPath.startsWith("/storage/emulated/0/") -> {
                            val rel = dirPath.removePrefix("/storage/emulated/0/").trim('/')
                            "primary:$rel"
                        }
                        else -> "primary:Documents/Budgeter"
                    }
                    DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", docId)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

class CreateDocumentWithInitialUri(
    private val mimeType: String,
    private val initialUriProvider: () -> Uri?
) : ActivityResultContract<String, Uri?>() {

    override fun createIntent(context: Context, input: String): Intent {
        return Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mimeType
            putExtra(Intent.EXTRA_TITLE, input)
            val initialUri = initialUriProvider()
            if (initialUri != null) {
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri)
            }
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (intent == null || resultCode != Activity.RESULT_OK) null else intent.data
    }
}

class OpenDocumentWithInitialUri(
    private val initialUriProvider: () -> Uri?
) : ActivityResultContract<Array<String>, Uri?>() {

    override fun createIntent(context: Context, input: Array<String>): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = if (input.size == 1) input[0] else "*/*"
            if (input.isNotEmpty()) {
                putExtra(Intent.EXTRA_MIME_TYPES, input)
            }
            val initialUri = initialUriProvider()
            if (initialUri != null) {
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri)
            }
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (intent == null || resultCode != Activity.RESULT_OK) null else intent.data
    }
}
