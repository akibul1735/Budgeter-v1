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

class ExportDocActivity : ComponentActivity() {

    companion object {
        const val EXTRA_FILE_NAME = "extra_file_name"
        const val EXTRA_MIME_TYPE = "extra_mime_type"
        const val EXTRA_CONTENT = "extra_content"
        const val EXTRA_TITLE = "extra_title"

        fun start(
            context: Context,
            fileName: String,
            mimeType: String,
            content: String,
            title: String
        ) {
            val intent = Intent(context, ExportDocActivity::class.java).apply {
                putExtra(EXTRA_FILE_NAME, fileName)
                putExtra(EXTRA_MIME_TYPE, mimeType)
                putExtra(EXTRA_CONTENT, content)
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
    private var content: String = ""

    private val createDocLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            if (uri != null) {
                try {
                    contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(content.toByteArray(Charsets.UTF_8))
                        out.flush()
                    }
                    Toast.makeText(this, "Saved: $fileName", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Failed to save: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fileName = intent.getStringExtra(EXTRA_FILE_NAME) ?: "export.txt"
        mimeType = intent.getStringExtra(EXTRA_MIME_TYPE) ?: "*/*"
        content = intent.getStringExtra(EXTRA_CONTENT) ?: ""

        // Keep local backup copy in Budgeter/Exports folder
        try {
            BackupManager.saveExportToLocalFolder(
                context = this,
                fileName = fileName,
                mimeType = mimeType,
                content = content
            )
        } catch (_: Exception) {}

        // Launch system Save Document intent with initial storage URI
        try {
            val initialUri = StorageLocationHelper.getInitialStorageUri(this)
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = mimeType
                putExtra(Intent.EXTRA_TITLE, fileName)
                if (initialUri != null) {
                    putExtra(android.provider.DocumentsContract.EXTRA_INITIAL_URI, initialUri)
                }
            }
            createDocLauncher.launch(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Export saved to Budgeter/Exports", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
