package com.example.ui.dialogs

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LanguageMode
import com.example.ui.theme.AppThemeConfig
import com.example.ui.theme.ColorIntensity
import com.example.ui.theme.CustomTheme
import com.example.ui.theme.FontPreset
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.ThemePalette
import com.example.ui.theme.darken
import com.example.ui.theme.lighten

// Curated color swatches for quick palette creation
val CURATED_SWATCHES = listOf(
    // Blues & Indigos
    0xFF1D4ED8 to "Royal Blue",
    0xFF2563EB to "Ocean Blue",
    0xFF0284C7 to "Sky Cyan",
    0xFF4F46E5 to "Indigo",
    0xFF6366F1 to "Periwinkle",
    // Purples & Violets
    0xFF7E57C2 to "Purple",
    0xFF8B5CF6 to "Violet",
    0xFF9333EA to "Bright Purple",
    0xFFC026D3 to "Fuchsia",
    // Pinks & Roses
    0xFFE11D48 to "Crimson Rose",
    0xFFDB2777 to "Deep Pink",
    0xFFF43F5E to "Coral Rose",
    // Greens & Teals
    0xFF15803D to "Emerald",
    0xFF16A34A to "Forest Green",
    0xFF059669 to "Mint Teal",
    0xFF0D9488 to "Dark Teal",
    0xFF3B7A57 to "Calm Sage",
    0xFF65A30D to "Fresh Lime",
    // Ambers, Oranges & Earths
    0xFFB45309 to "Amber Gold",
    0xFFD97706 to "Warm Sunset",
    0xFFEA580C to "Burnt Orange",
    0xFFC2410C to "Terracotta",
    0xFF78350F to "Deep Mocha",
    0xFF854D0E to "Bronze",
    // Darks & Neutrals
    0xFF27272A to "Slate Charcoal",
    0xFF343A40 to "Monochrome Dark",
    0xFF475569 to "Steel Gray",
    0xFF0F172A to "Midnight Navy"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ThemeFontSettingsDialog(
    themeConfig: AppThemeConfig,
    languageMode: LanguageMode,
    onPaletteSelected: (ThemePalette) -> Unit,
    onCustomThemeSelected: (String) -> Unit = {},
    onCustomThemeAdded: (name: String, primaryHex: Long, secondaryHex: Long?) -> Unit = { _, _, _ -> },
    onCustomThemeUpdated: (CustomTheme) -> Unit = {},
    onCustomThemeDeleted: (String) -> Unit = {},
    onModeSelected: (ThemeMode) -> Unit,
    onColorIntensitySelected: (ColorIntensity) -> Unit = {},
    onDynamicColorToggled: (Boolean) -> Unit,
    onFontPresetSelected: (FontPreset) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isBangla = languageMode == LanguageMode.BANGLA

    var selectedTab by remember {
        mutableStateOf(if (themeConfig.customThemeId != null) 1 else 0)
    }

    var showThemeEditorDialog by remember { mutableStateOf(false) }
    var editingTheme by remember { mutableStateOf<CustomTheme?>(null) }
    var themeToDelete by remember { mutableStateOf<CustomTheme?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("dialog_theme_font_settings")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isBangla) "থিম ও রূপরেখা সেটিংস" else "Theme & Appearance",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isBangla) "কাস্টম থিম তৈরি, এডিট ও ফন্ট নির্ধারণ করুন" else "Create, edit themes & customize typography",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 1. Interactive Live Theme Preview Card
            LiveThemePreviewCard(
                themeConfig = themeConfig,
                isBangla = isBangla
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Theme Selection Tabs (Presets vs My Custom Themes)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp)),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = MaterialTheme.colorScheme.primary,
                            height = 3.dp
                        )
                    },
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = if (isBangla) "প্যালেট (৮)" else "Presets (8)",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = if (isBangla) "আমার থিম (${themeConfig.customThemes.size})" else "My Themes (${themeConfig.customThemes.size})",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Quick Add Theme Button
                Button(
                    onClick = {
                        editingTheme = null
                        showThemeEditorDialog = true
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isBangla) "নতুন" else "New",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tab Content
            if (selectedTab == 0) {
                // Presets Grid
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ThemePalette.values().forEach { palette ->
                        val isSelected = themeConfig.customThemeId == null && themeConfig.palette == palette
                        PresetThemeCard(
                            palette = palette,
                            isSelected = isSelected,
                            isBangla = isBangla,
                            onSelect = { onPaletteSelected(palette) },
                            onCloneAsCustom = {
                                editingTheme = CustomTheme(
                                    id = "",
                                    name = "${if (isBangla) palette.displayNameBn else palette.displayNameEn} (Custom)",
                                    primaryColorHex = palette.primaryColor.toArgb().toLong() and 0xFFFFFFFFL,
                                    secondaryColorHex = null
                                )
                                showThemeEditorDialog = true
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                // Custom Themes List / Empty State
                if (themeConfig.customThemes.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                editingTheme = null
                                showThemeEditorDialog = true
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isBangla) "কোনো কাস্টম থিম নেই" else "No Custom Themes Yet",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (isBangla) "আপনার পছন্দের রঙ বা হেক্স কোড দিয়ে নিজস্ব থিম বানান" else "Design your own custom color theme with hex codes & color swatches",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    editingTheme = null
                                    showThemeEditorDialog = true
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isBangla) "কাস্টম থিম তৈরি করুন" else "Create Custom Theme", fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        themeConfig.customThemes.forEach { customTheme ->
                            val isSelected = themeConfig.customThemeId == customTheme.id
                            CustomThemeRowItem(
                                theme = customTheme,
                                isSelected = isSelected,
                                onSelect = { onCustomThemeSelected(customTheme.id) },
                                onEdit = {
                                    editingTheme = customTheme
                                    showThemeEditorDialog = true
                                },
                                onDelete = {
                                    themeToDelete = customTheme
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            // 3. Theme Mode (System / Light / Dark / AMOLED)
            Text(
                text = if (isBangla) "🌙 থিম মোড" else "🌙 Theme Mode",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ThemeMode.values().forEach { mode ->
                    val isSelected = themeConfig.mode == mode
                    val icon = when (mode) {
                        ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                        ThemeMode.LIGHT -> Icons.Default.LightMode
                        ThemeMode.DARK -> Icons.Default.DarkMode
                        ThemeMode.AMOLED_NIGHT -> Icons.Default.DarkMode
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { onModeSelected(mode) },
                        label = {
                            Text(
                                text = if (isBangla) mode.titleBn else mode.titleEn,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = {
                            Icon(
                                icon,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            // Dynamic Color option (Android 12+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBangla) "ডাইনামিক কালার (Material You)" else "Dynamic Color (Material You)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.5.sp
                        )
                        Text(
                            text = if (isBangla) "ওয়ালপেপার থেকে স্বয়ংক্রিয় রং গ্রহণ করে" else "Adapts color palette from system wallpaper",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = themeConfig.dynamicColor,
                        onCheckedChange = onDynamicColorToggled,
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(10.dp))

            // 4. Color Intensity Level
            Text(
                text = if (isBangla) "🌈 কালার ইনটেনসিটি (গাঢ়ত্ব)" else "🌈 Color Intensity Level",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (intensity in ColorIntensity.values()) {
                    val isSelected = themeConfig.colorIntensity == intensity
                    FilterChip(
                        selected = isSelected,
                        onClick = { onColorIntensitySelected(intensity) },
                        label = {
                            Text(
                                text = if (isBangla) intensity.titleBn else intensity.titleEn,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(13.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(10.dp))

            // 5. Typography / Fonts Options
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.FormatSize,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isBangla) "✍️ অ্যাপ ফন্ট স্টাইল" else "✍️ App Typography & Fonts",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                FontPreset.values().forEach { font ->
                    val isSelected = themeConfig.fontPreset == font
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onFontPresetSelected(font) }
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isBangla) font.titleBn else font.titleEn,
                                    fontFamily = font.fontFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "৳ ১,২৫,০০০ • Income: ৳50,000 / Expense: ৳12,000",
                                    fontFamily = font.fontFamily,
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Theme Editor Dialog (Create / Edit Theme)
    if (showThemeEditorDialog) {
        ThemeEditorDialog(
            theme = editingTheme,
            isBangla = isBangla,
            onSave = { name, primaryHex, secondaryHex ->
                if (editingTheme != null && editingTheme!!.id.isNotBlank()) {
                    onCustomThemeUpdated(
                        editingTheme!!.copy(
                            name = name,
                            primaryColorHex = primaryHex,
                            secondaryColorHex = secondaryHex
                        )
                    )
                } else {
                    onCustomThemeAdded(name, primaryHex, secondaryHex)
                    selectedTab = 1 // Switch to My Themes tab
                }
                showThemeEditorDialog = false
                editingTheme = null
            },
            onDismiss = {
                showThemeEditorDialog = false
                editingTheme = null
            }
        )
    }

    // Delete Confirmation Dialog
    if (themeToDelete != null) {
        AlertDialog(
            onDismissRequest = { themeToDelete = null },
            title = {
                Text(if (isBangla) "থিম ডিলিট করবেন?" else "Delete Theme?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    if (isBangla) "\"${themeToDelete!!.name}\" কাস্টম থিমটি মুছে ফেলা হবে।"
                    else "Are you sure you want to delete \"${themeToDelete!!.name}\"?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCustomThemeDeleted(themeToDelete!!.id)
                        themeToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isBangla) "ডিলিট" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { themeToDelete = null }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun LiveThemePreviewCard(
    themeConfig: AppThemeConfig,
    isBangla: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBangla) "সক্রিয়: ${themeConfig.activeThemeDisplayName}" else "Active: ${themeConfig.activeThemeDisplayName}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "${if (isBangla) themeConfig.mode.titleBn else themeConfig.mode.titleEn} • ${if (isBangla) themeConfig.colorIntensity.titleBn else themeConfig.colorIntensity.titleEn}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Mini App Elements Representation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Balance badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(end = 6.dp)
                ) {
                    Text(
                        text = "৳ ১,২৫,৪৫০",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                // Income / Expense tags
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "+৳৫০,০০০",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFEF4444).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "-৳১২,০০০",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                // Mini Primary Action Button
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = if (isBangla) "+ যুক্ত" else "+ Add",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PresetThemeCard(
    palette: ThemePalette,
    isSelected: Boolean,
    isBangla: Boolean,
    onSelect: () -> Unit,
    onCloneAsCustom: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onSelect() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) palette.primaryColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                shape = RoundedCornerShape(10.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) palette.primaryColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(palette.primaryColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isBangla) palette.displayNameBn else palette.displayNameEn,
                    fontSize = 11.5.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) palette.primaryColor else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isSelected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = palette.primaryColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                }
                IconButton(
                    onClick = onCloneAsCustom,
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Edit as Custom Theme",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomThemeRowItem(
    theme: CustomTheme,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onSelect() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) theme.primaryColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                shape = RoundedCornerShape(10.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) theme.primaryColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Primary + Secondary color pill
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(theme.primaryColor)
                    )
                    if (theme.secondaryColorHex != null && theme.secondaryColorHex != theme.primaryColorHex) {
                        Spacer(modifier = Modifier.width(2.dp))
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(theme.secondaryColor)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = theme.name,
                        fontSize = 12.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) theme.primaryColor else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "#" + (theme.primaryColorHex and 0xFFFFFFL).toString(16).uppercase().padStart(6, '0'),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isSelected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = theme.primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Theme",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Theme",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ThemeEditorDialog(
    theme: CustomTheme?,
    isBangla: Boolean,
    onSave: (name: String, primaryHex: Long, secondaryHex: Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    var themeName by remember {
        mutableStateOf(theme?.name ?: (if (isBangla) "আমার কাস্টম থিম" else "My Custom Theme"))
    }

    var selectedPrimaryHex by remember {
        mutableStateOf(theme?.primaryColorHex ?: 0xFF1D4ED8L)
    }

    var hexInputText by remember {
        mutableStateOf((selectedPrimaryHex and 0xFFFFFFL).toString(16).uppercase().padStart(6, '0'))
    }

    var isHexValid by remember { mutableStateOf(true) }

    var enableSecondaryCustom by remember {
        mutableStateOf(theme?.secondaryColorHex != null)
    }

    var selectedSecondaryHex by remember {
        mutableStateOf(theme?.secondaryColorHex ?: 0xFF0284C7L)
    }

    val primaryColor = Color(selectedPrimaryHex)
    val secondaryColor = if (enableSecondaryCustom) Color(selectedSecondaryHex) else primaryColor

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (theme != null && theme.id.isNotBlank()) Icons.Default.Edit else Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (theme != null && theme.id.isNotBlank()) {
                        if (isBangla) "থিম এডিট করুন" else "Edit Theme"
                    } else {
                        if (isBangla) "নতুন থিম তৈরি করুন" else "Create New Theme"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Theme Name Input
                OutlinedTextField(
                    value = themeName,
                    onValueChange = { themeName = it },
                    label = { Text(if (isBangla) "থিমের নাম" else "Theme Name", fontSize = 12.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // 2. Live Color Preview Swatch Card
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = primaryColor.copy(alpha = 0.12f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, primaryColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(primaryColor)
                                    .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                            )
                            if (enableSecondaryCustom) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(secondaryColor)
                                        .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = themeName.ifBlank { "Theme" },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = primaryColor
                                )
                                Text(
                                    text = "#${(selectedPrimaryHex and 0xFFFFFFL).toString(16).uppercase().padStart(6, '0')}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Sample mini button
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = primaryColor
                        ) {
                            Text(
                                text = "Preview",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // 3. Quick Color Swatches Grid (28 rich colors)
                Text(
                    text = if (isBangla) "🎨 দ্রুত কালার প্যালেট থেকে বাছুন" else "🎨 Pick From Color Swatches",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CURATED_SWATCHES.forEach { (colorHex, _) ->
                        val isSelected = (selectedPrimaryHex and 0xFFFFFFL) == (colorHex and 0xFFFFFFL)
                        val color = Color(colorHex)
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable {
                                    selectedPrimaryHex = colorHex
                                    hexInputText = (colorHex and 0xFFFFFFL).toString(16).uppercase().padStart(6, '0')
                                    isHexValid = true
                                }
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Black.copy(alpha = 0.2f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // 4. Custom Hex Code Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = hexInputText,
                        onValueChange = { input ->
                            val sanitized = input.filter { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }.take(6)
                            hexInputText = sanitized
                            if (sanitized.length == 6) {
                                try {
                                    val parsed = sanitized.toLong(16)
                                    selectedPrimaryHex = 0xFF000000L or parsed
                                    isHexValid = true
                                } catch (_: Exception) {
                                    isHexValid = false
                                }
                            } else {
                                isHexValid = sanitized.length < 6
                            }
                        },
                        label = { Text(if (isBangla) "হেক্স কোড (#RRGGBB)" else "Hex Code (#RRGGBB)", fontSize = 11.sp) },
                        prefix = { Text("#", fontWeight = FontWeight.Bold) },
                        singleLine = true,
                        isError = !isHexValid,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                // 5. Secondary Accent Color Options
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBangla) "স্বতন্ত্র সেকেন্ডারি অ্যাকসেন্ট" else "Custom Secondary Accent",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isBangla) "অটো-হারমোনাইজ বা আলাদা সেকেন্ডারি রঙ" else "Auto-harmonize or choose distinct accent",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = enableSecondaryCustom,
                        onCheckedChange = { enableSecondaryCustom = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                    )
                }

                AnimatedVisibility(visible = enableSecondaryCustom) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (isBangla) "সেকেন্ডারি রঙ বাছুন" else "Pick Secondary Accent",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CURATED_SWATCHES.take(14).forEach { (colorHex, _) ->
                                val isSelected = (selectedSecondaryHex and 0xFFFFFFL) == (colorHex and 0xFFFFFFL)
                                val color = Color(colorHex)
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .clickable { selectedSecondaryHex = colorHex }
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Black.copy(alpha = 0.2f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalPrimary = if (hexInputText.length == 6) {
                        try {
                            0xFF000000L or hexInputText.toLong(16)
                        } catch (_: Exception) { selectedPrimaryHex }
                    } else selectedPrimaryHex

                    val finalSecondary = if (enableSecondaryCustom) selectedSecondaryHex else null
                    onSave(themeName.trim().ifBlank { "Custom Theme" }, finalPrimary, finalSecondary)
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isBangla) "সংরক্ষণ করুন" else "Save Theme")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBangla) "বাতিল" else "Cancel")
            }
        }
    )
}
