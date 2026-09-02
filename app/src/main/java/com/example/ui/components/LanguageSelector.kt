package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.LanguageMode

@Composable
fun LanguageSelector(
    currentMode: LanguageMode,
    onModeSelected: (LanguageMode) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val displayText = when (currentMode) {
        LanguageMode.ENGLISH -> "EN"
        LanguageMode.BANGLA -> "বাং"
        LanguageMode.BILINGUAL -> "EN/বাং"
    }

    Box(modifier = modifier) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable { expanded = true }
                .testTag("language_selector_btn")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Translate,
                    contentDescription = "Language",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = displayText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("English (EN)", fontWeight = if (currentMode == LanguageMode.ENGLISH) FontWeight.Bold else FontWeight.Normal) },
                leadingIcon = {
                    if (currentMode == LanguageMode.ENGLISH) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                },
                onClick = {
                    onModeSelected(LanguageMode.ENGLISH)
                    expanded = false
                }
            )

            DropdownMenuItem(
                text = { Text("বাংলা (Bangla)", fontWeight = if (currentMode == LanguageMode.BANGLA) FontWeight.Bold else FontWeight.Normal) },
                leadingIcon = {
                    if (currentMode == LanguageMode.BANGLA) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                },
                onClick = {
                    onModeSelected(LanguageMode.BANGLA)
                    expanded = false
                }
            )

            DropdownMenuItem(
                text = { Text("Bilingual (উভয় ভাষা)", fontWeight = if (currentMode == LanguageMode.BILINGUAL) FontWeight.Bold else FontWeight.Normal) },
                leadingIcon = {
                    if (currentMode == LanguageMode.BILINGUAL) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                },
                onClick = {
                    onModeSelected(LanguageMode.BILINGUAL)
                    expanded = false
                }
            )
        }
    }
}
