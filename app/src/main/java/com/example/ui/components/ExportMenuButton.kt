package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LanguageMode
import com.example.util.ExportFormat

@Composable
fun ExportMenuButton(
    onExport: (ExportFormat) -> Unit,
    languageMode: LanguageMode = LanguageMode.ENGLISH,
    modifier: Modifier = Modifier,
    testTag: String = "header_export_btn"
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier
                .size(38.dp)
                .testTag(testTag)
        ) {
            Icon(
                imageVector = Icons.Default.FileDownload,
                contentDescription = if (languageMode == LanguageMode.BANGLA) "এক্সপোর্ট করুন" else "Export",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 4.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (languageMode == LanguageMode.BANGLA) "এক্সপোর্ট ফরম্যাট নির্বাচন করুন" else "Export Format",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // 1. PDF Document
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "PDF ডকুমেন্ট (প্রিন্ট/সেভ)" else "PDF Document (Print/Save)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                leadingIcon = {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFEF4444).copy(alpha = 0.15f),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("PDF", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFDC2626))
                        }
                    }
                },
                onClick = {
                    expanded = false
                    onExport(ExportFormat.PDF)
                },
                modifier = Modifier.testTag("export_pdf_option")
            )

            // 2. CSV Spreadsheet
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "CSV স্প্রেডশীট (এক্সেল/শীট)" else "CSV Spreadsheet (Excel/Sheets)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                leadingIcon = {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("CSV", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF059669))
                        }
                    }
                },
                onClick = {
                    expanded = false
                    onExport(ExportFormat.CSV)
                },
                modifier = Modifier.testTag("export_csv_option")
            )

            // 3. HTML Webpage
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "HTML ওয়েব রিপোর্ট" else "HTML Web Report",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                leadingIcon = {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF3B82F6).copy(alpha = 0.15f),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("HTML", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2563EB))
                        }
                    }
                },
                onClick = {
                    expanded = false
                    onExport(ExportFormat.HTML)
                },
                modifier = Modifier.testTag("export_html_option")
            )

            // 4. JSON Data
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (languageMode == LanguageMode.BANGLA) "JSON ডেটা (র ডেটা)" else "JSON Raw Data",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                leadingIcon = {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF8B5CF6).copy(alpha = 0.15f),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("JSON", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF7C3AED))
                        }
                    }
                },
                onClick = {
                    expanded = false
                    onExport(ExportFormat.JSON)
                },
                modifier = Modifier.testTag("export_json_option")
            )
        }
    }
}
