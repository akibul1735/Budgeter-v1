package com.example.ui.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.example.util.BackupManager
import com.example.util.StorageLocationHelper
import java.io.File

class ExportDocActivity : ComponentActivity() {

    companion object {
        const val EXTRA_FILE_NAME = "extra_file_name"
        const val EXTRA_MIME_TYPE = "extra_mime_type"
        const val EXTRA_TITLE = "extra_title"

        // In-memory buffer to avoid TransactionTooLargeException with large HTML/JSON/CSV
        @Volatile
        private var pendingContentBuffer: String? = null

        fun start(
            context: Context,
            fileName: String,
            mimeType: String,
            content: String,
            title: String
        ) {
            pendingContentBuffer = content
            // Also write to temporary cache file in case the activity process is recreated
            try {
                val tempFile = File(context.cacheDir, "export_pending.tmp")
                tempFile.writeText(content, Charsets.UTF_8)
            } catch (_: Exception) {}

            val intent = Intent(context, ExportDocActivity::class.java).apply {
                putExtra(EXTRA_FILE_NAME, fileName)
                putExtra(EXTRA_MIME_TYPE, mimeType)
                putExtra(EXTRA_TITLE, title)
                if (context !is Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            context.startActivity(intent)
        }
    }

    private var fileName: String = ""
    private var mimeType: String = "*/*"

    private fun getPendingContent(): String {
        pendingContentBuffer?.let { return it }
        return try {
            val tempFile = File(cacheDir, "export_pending.tmp")
            if (tempFile.exists()) tempFile.readText(Charsets.UTF_8) else ""
        } catch (_: Exception) {
            ""
        }
    }

    private fun cleanUp() {
        pendingContentBuffer = null
        try {
            val tempFile = File(cacheDir, "export_pending.tmp")
            if (tempFile.exists()) tempFile.delete()
        } catch (_: Exception) {}
    }

    private val createDocLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            if (uri != null) {
                try {
                    val contentToSave = getPendingContent()
                    contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(contentToSave.toByteArray(Charsets.UTF_8))
                        out.flush()
                    }
                    Toast.makeText(this, "Saved: $fileName", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Failed to save: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
        cleanUp()
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fileName = intent.getStringExtra(EXTRA_FILE_NAME) ?: "export.txt"
        mimeType = intent.getStringExtra(EXTRA_MIME_TYPE) ?: "*/*"

        // Launch system Save Document intent with initial storage URI
        try {
            val initialUri = StorageLocationHelper.getInitialStorageUri(this)
            val normalizedMimeType = when {
                mimeType.contains("html") -> "text/html"
                mimeType.contains("json") -> "application/json"
                mimeType.contains("csv") -> "text/csv"
                else -> mimeType
            }
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = normalizedMimeType
                putExtra(Intent.EXTRA_TITLE, fileName)
                if (initialUri != null) {
                    putExtra(android.provider.DocumentsContract.EXTRA_INITIAL_URI, initialUri)
                }
            }
            createDocLauncher.launch(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback: save to local folder if SAF fails
            val contentToSave = getPendingContent()
            val savedLocation = BackupManager.saveExportToLocalFolder(
                context = this,
                fileName = fileName,
                mimeType = mimeType,
                content = contentToSave
            )
            val feedback = if (savedLocation != null) "Saved to $savedLocation" else "Exported $fileName"
            Toast.makeText(this, feedback, Toast.LENGTH_SHORT).show()
            cleanUp()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            cleanUp()
        }
    }
}
