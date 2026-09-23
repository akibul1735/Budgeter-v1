package com.example.ui.components

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoSizeSelectActual
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Rotate90DegreesCcw
import androidx.compose.material.icons.filled.Rotate90DegreesCw
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.remote.OnlineIconSearchService
import com.example.ui.theme.SolidPrimary
import com.example.util.IconHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

enum class PhotoCropShape(val id: String, val label: String) {
    CIRCLE("circle", "Circle"),
    SQUIRCLE("rounded_square", "Squircle"),
    SQUARE("square", "Square")
}

enum class PhotoFitMode {
    FILL, // Fills crop frame completely (no empty borders)
    FIT   // Fits entire photo inside crop frame
}

enum class PhotoFilterPreset(val label: String) {
    ORIGINAL("Original"),
    VIVID("Vivid"),
    WARM("Warm"),
    COOL("Cool"),
    BW("B & W"),
    DRAMATIC("Dramatic")
}

data class BgColorOption(
    val label: String,
    val color: Color?, // null = Transparent
    val hexString: String?
)

val BG_COLOR_OPTIONS = listOf(
    BgColorOption("Transparent", null, null),
    BgColorOption("White", Color.White, "#FFFFFF"),
    BgColorOption("Dark", Color(0xFF1E293B), "#1E293B"),
    BgColorOption("Black", Color(0xFF000000), "#000000"),
    BgColorOption("Cream", Color(0xFFFFFBEB), "#FFFBEB"),
    BgColorOption("Emerald", SolidPrimary, "#0D9488"),
    BgColorOption("Indigo", Color(0xFF4F46E5), "#4F46E5"),
    BgColorOption("Amber", Color(0xFFD97706), "#D97706"),
    BgColorOption("Rose", Color(0xFFE11D48), "#E11D48"),
    BgColorOption("Sky Blue", Color(0xFF0284C7), "#0284C7"),
    BgColorOption("Mint", Color(0xFF059669), "#059669"),
    BgColorOption("Purple", Color(0xFF7C3AED), "#7C3AED")
)

/**
 * Brand-new, highly accurate Photo Crop & Editing Studio.
 * - Photo appears naturally centered without zooming out all the way.
 * - 100% mathematically exact WYSIWYG cropping: whatever is on screen is what is saved.
 * - Full suite of editing tools: Pan, Pinch-Zoom, 90° Rotations, Fine Angle Straighten, Flip H/V.
 * - Photo adjustment suite: Brightness, Contrast, Saturation, and Color Presets.
 */
@Composable
fun PhotoCropEditorModal(
    imageUri: Uri? = null,
    imageUrl: String? = null,
    initialIconKey: String? = null,
    sourceBitmap: Bitmap? = null,
    onCroppedIconSaved: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var loadedBitmap by remember { mutableStateOf<Bitmap?>(sourceBitmap) }
    var isLoadingImage by remember { mutableStateOf(loadedBitmap == null) }

    // Editor Tab State (0: Crop & Transform, 1: Adjust & Filters)
    var selectedTab by remember { mutableIntStateOf(0) }

    // Transformation States
    var userScale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var quarterTurns by remember { mutableIntStateOf(0) } // 0, 1, 2, 3 (each 90°)
    var fineAngle by remember { mutableFloatStateOf(0f) } // -45° to +45°
    var flipHorizontal by remember { mutableStateOf(false) }
    var flipVertical by remember { mutableStateOf(false) }

    // Cropping & Framing Mode
    var cropShape by remember { mutableStateOf(PhotoCropShape.CIRCLE) }
    var fitMode by remember { mutableStateOf(PhotoFitMode.FILL) }
    var cropBoxSizePx by remember { mutableFloatStateOf(300f) }

    // Background Color for icons/photos (null = transparent)
    var selectedBgColor by remember { mutableStateOf<Color?>(null) }
    var showCustomColorDialog by remember { mutableStateOf(false) }
    var customColorHexInput by remember { mutableStateOf("#FFFFFF") }

    // Color Adjustments & Filters
    var filterPreset by remember { mutableStateOf(PhotoFilterPreset.ORIGINAL) }
    var brightnessAdj by remember { mutableFloatStateOf(0f) } // -0.5 to +0.5
    var contrastAdj by remember { mutableFloatStateOf(0f) }   // -0.5 to +0.5
    var saturationAdj by remember { mutableFloatStateOf(0f) } // -1.0 to +1.0

    var isSaving by remember { mutableStateOf(false) }

    // Load bitmap asynchronously
    LaunchedEffect(imageUri, imageUrl, initialIconKey, sourceBitmap) {
        userScale = 1.0f
        panOffset = Offset.Zero
        quarterTurns = 0
        fineAngle = 0f
        flipHorizontal = false
        flipVertical = false

        if (sourceBitmap != null) {
            loadedBitmap = sourceBitmap
            isLoadingImage = false
            return@LaunchedEffect
        }
        isLoadingImage = true
        withContext(Dispatchers.IO) {
            val bitmap = when {
                imageUri != null -> IconHelper.decodeBitmapFromUri(context, imageUri, 1200)
                imageUrl != null && (imageUrl.startsWith("content://") || imageUrl.startsWith("file://")) -> {
                    IconHelper.decodeBitmapFromUri(context, Uri.parse(imageUrl), 1200)
                }
                imageUrl != null -> OnlineIconSearchService.downloadBitmap(context, imageUrl, 1200)
                initialIconKey != null -> IconHelper.decodeBitmapFromCustomKey(context, initialIconKey)
                else -> null
            }
            withContext(Dispatchers.Main) {
                loadedBitmap = bitmap
                isLoadingImage = false
            }
        }
    }

    // Build the 4x5 ColorMatrix for preview and export
    val activeColorMatrixValues = remember(filterPreset, brightnessAdj, contrastAdj, saturationAdj) {
        buildCombinedColorMatrix(filterPreset, brightnessAdj, contrastAdj, saturationAdj)
    }

    Dialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(SolidPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Crop,
                                contentDescription = null,
                                tint = SolidPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Photo & Icon Studio",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Crop, adjust & style picture",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = { if (!isSaving) onDismiss() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Interactive Cropping Viewport
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.05f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF141414))
                        .onSizeChanged { intSize ->
                            val side = minOf(intSize.width, intSize.height) * 0.82f
                            if (side > 0f) {
                                cropBoxSizePx = side
                            }
                        }
                        .pointerInput(loadedBitmap) {
                            if (loadedBitmap == null) return@pointerInput
                            detectTransformGestures { _, pan, zoom, _ ->
                                userScale = (userScale * zoom).coerceIn(0.35f, 5.5f)
                                panOffset += pan
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoadingImage) {
                        CircularProgressIndicator(
                            color = SolidPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    } else if (loadedBitmap != null) {
                        val bmp = loadedBitmap!!
                        val composeBitmap = remember(bmp) { bmp.asImageBitmap() }
                        val composeColorFilter = remember(activeColorMatrixValues) {
                            if (activeColorMatrixValues != null) {
                                ColorFilter.colorMatrix(ColorMatrix(activeColorMatrixValues))
                            } else null
                        }

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val canvasW = size.width
                            val canvasH = size.height
                            val cropSide = minOf(canvasW, canvasH) * 0.82f
                            cropBoxSizePx = cropSide
                            val centerX = canvasW / 2f
                            val centerY = canvasH / 2f
                            val cropRadius = cropSide / 2f

                            val cropLeft = centerX - cropRadius
                            val cropTop = centerY - cropRadius
                            val cropRight = centerX + cropRadius
                            val cropBottom = centerY + cropRadius

                            // 1. Calculate Base Scale (Default is FILL to prevent zooming out all the way!)
                            val baseScale = if (fitMode == PhotoFitMode.FILL) {
                                maxOf(cropSide / bmp.width.toFloat(), cropSide / bmp.height.toFloat())
                            } else {
                                minOf(cropSide / bmp.width.toFloat(), cropSide / bmp.height.toFloat())
                            }
                            val effectiveScale = baseScale * userScale
                            val totalRotation = ((quarterTurns % 4) * 90f + fineAngle)

                            // 2. Define crop path for masking and outline
                            val cropPath = Path().apply {
                                when (cropShape) {
                                    PhotoCropShape.CIRCLE -> {
                                        addOval(Rect(cropLeft, cropTop, cropRight, cropBottom))
                                    }
                                    PhotoCropShape.SQUIRCLE -> {
                                        addRoundRect(
                                            RoundRect(
                                                cropLeft, cropTop, cropRight, cropBottom,
                                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                                                    cropSide * 0.20f,
                                                    cropSide * 0.20f
                                                )
                                            )
                                        )
                                    }
                                    PhotoCropShape.SQUARE -> {
                                        addRect(Rect(cropLeft, cropTop, cropRight, cropBottom))
                                    }
                                }
                            }

                            // 2b. Draw background color (or subtle checkerboard for transparent icons) inside crop path
                            clipPath(cropPath) {
                                if (selectedBgColor != null) {
                                    drawRect(
                                        color = selectedBgColor!!,
                                        topLeft = Offset(cropLeft, cropTop),
                                        size = androidx.compose.ui.geometry.Size(cropSide, cropSide)
                                    )
                                } else {
                                    // Draw subtle checkerboard pattern to indicate transparency
                                    val sq = 12.dp.toPx()
                                    val cCount = (cropSide / sq).toInt() + 1
                                    val rCount = (cropSide / sq).toInt() + 1
                                    for (r in 0 until rCount) {
                                        for (c in 0 until cCount) {
                                            val isEven = (r + c) % 2 == 0
                                            drawRect(
                                                color = if (isEven) Color(0xFF262626) else Color(0xFF191919),
                                                topLeft = Offset(cropLeft + c * sq, cropTop + r * sq),
                                                size = androidx.compose.ui.geometry.Size(sq, sq)
                                            )
                                        }
                                    }
                                }
                            }

                            // 3. Draw Transformed Image (100% Identical Native Matrix Math to Export)
                            val nativePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG or android.graphics.Paint.FILTER_BITMAP_FLAG).apply {
                                isFilterBitmap = true
                                isDither = true
                                if (activeColorMatrixValues != null) {
                                    colorFilter = android.graphics.ColorMatrixColorFilter(activeColorMatrixValues)
                                }
                            }

                            val previewMatrix = android.graphics.Matrix().apply {
                                // 1. Move center of source bitmap to origin
                                postTranslate(-bmp.width / 2f, -bmp.height / 2f)

                                // 2. Rotate around origin
                                postRotate(totalRotation)

                                // 3. Flip & Scale from origin
                                val sx = if (flipHorizontal) -effectiveScale else effectiveScale
                                val sy = if (flipVertical) -effectiveScale else effectiveScale
                                postScale(sx, sy)

                                // 4. Translate origin to screen crop center + pan offset
                                postTranslate(centerX + panOffset.x, centerY + panOffset.y)
                            }

                            drawContext.canvas.nativeCanvas.drawBitmap(bmp, previewMatrix, nativePaint)

                            // 4. Darken area outside crop frame
                            clipPath(cropPath, clipOp = ClipOp.Difference) {
                                drawRect(
                                    color = Color.Black.copy(alpha = 0.72f),
                                    size = size
                                )
                            }

                            // 5. Rule-of-thirds grid inside crop frame
                            val step = cropSide / 3f
                            clipPath(cropPath) {
                                drawLine(
                                    color = Color.White.copy(alpha = 0.28f),
                                    start = Offset(cropLeft + step, cropTop),
                                    end = Offset(cropLeft + step, cropBottom),
                                    strokeWidth = 1.dp.toPx()
                                )
                                drawLine(
                                    color = Color.White.copy(alpha = 0.28f),
                                    start = Offset(cropLeft + step * 2, cropTop),
                                    end = Offset(cropLeft + step * 2, cropBottom),
                                    strokeWidth = 1.dp.toPx()
                                )
                                drawLine(
                                    color = Color.White.copy(alpha = 0.28f),
                                    start = Offset(cropLeft, cropTop + step),
                                    end = Offset(cropRight, cropTop + step),
                                    strokeWidth = 1.dp.toPx()
                                )
                                drawLine(
                                    color = Color.White.copy(alpha = 0.28f),
                                    start = Offset(cropLeft, cropTop + step * 2),
                                    end = Offset(cropRight, cropTop + step * 2),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }

                            // 6. Draw Crop Boundary Outline
                            drawPath(
                                path = cropPath,
                                color = Color.White,
                                style = Stroke(width = 2.dp.toPx())
                            )

                            // 7. Corner Handles for Square & Squircle
                            if (cropShape != PhotoCropShape.CIRCLE) {
                                val handleLen = 16.dp.toPx()
                                val handleThick = 3.dp.toPx()
                                val handleColor = Color.White

                                // Top-Left
                                drawLine(handleColor, Offset(cropLeft, cropTop), Offset(cropLeft + handleLen, cropTop), handleThick)
                                drawLine(handleColor, Offset(cropLeft, cropTop), Offset(cropLeft, cropTop + handleLen), handleThick)

                                // Top-Right
                                drawLine(handleColor, Offset(cropRight, cropTop), Offset(cropRight - handleLen, cropTop), handleThick)
                                drawLine(handleColor, Offset(cropRight, cropTop), Offset(cropRight, cropTop + handleLen), handleThick)

                                // Bottom-Left
                                drawLine(handleColor, Offset(cropLeft, cropBottom), Offset(cropLeft + handleLen, cropBottom), handleThick)
                                drawLine(handleColor, Offset(cropLeft, cropBottom), Offset(cropLeft, cropBottom - handleLen), handleThick)

                                // Bottom-Right
                                drawLine(handleColor, Offset(cropRight, cropBottom), Offset(cropRight - handleLen, cropBottom), handleThick)
                                drawLine(handleColor, Offset(cropRight, cropBottom), Offset(cropRight, cropBottom - handleLen), handleThick)
                            }
                        }
                    } else {
                        Text(
                            text = "Could not load image",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Navigation Tabs between "Crop & Frame" and "Adjust & Filters"
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = SolidPrimary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Crop, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Crop & Frame", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Adjust & Filter", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Scrollable Controls Body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (selectedTab == 0) {
                        // TAB 1: CROP & TRANSFORM CONTROLS

                        // 1. Shape & Framing Mode Selection
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Shape Chips
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                PhotoCropShape.values().forEach { shape ->
                                    val isSelected = cropShape == shape
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { cropShape = shape },
                                        leadingIcon = {
                                            val icon = when (shape) {
                                                PhotoCropShape.CIRCLE -> Icons.Default.RadioButtonUnchecked
                                                PhotoCropShape.SQUIRCLE -> Icons.Default.CropSquare
                                                PhotoCropShape.SQUARE -> Icons.Default.CropSquare
                                            }
                                            Icon(icon, contentDescription = null, modifier = Modifier.size(13.dp))
                                        },
                                        label = { Text(shape.label, fontSize = 11.sp) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = SolidPrimary.copy(alpha = 0.15f),
                                            selectedLabelColor = SolidPrimary
                                        ),
                                        modifier = Modifier.height(32.dp)
                                    )
                                }
                            }

                            // Framing Mode (Fill vs Fit)
                            FilterChip(
                                selected = fitMode == PhotoFitMode.FILL,
                                onClick = {
                                    fitMode = if (fitMode == PhotoFitMode.FILL) PhotoFitMode.FIT else PhotoFitMode.FILL
                                    userScale = 1.0f
                                    panOffset = Offset.Zero
                                },
                                leadingIcon = {
                                    Icon(
                                        if (fitMode == PhotoFitMode.FILL) Icons.Default.PhotoSizeSelectActual else Icons.Default.FitScreen,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        if (fitMode == PhotoFitMode.FILL) "Fill Crop" else "Fit All",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 2. Background Color Selection (For transparent icons)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FormatColorFill,
                                    contentDescription = null,
                                    tint = SolidPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (selectedBgColor != null) "Background: Solid Color" else "Background: Transparent",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            TextButton(
                                onClick = { showCustomColorDialog = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Custom", fontSize = 11.sp, color = SolidPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(BG_COLOR_OPTIONS) { opt ->
                                val isSelected = selectedBgColor == opt.color
                                val swatchBorderColor = if (isSelected) SolidPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                val swatchBorderWidth = if (isSelected) 2.5.dp else 1.dp

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable { selectedBgColor = opt.color }
                                        .padding(vertical = 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .border(swatchBorderWidth, swatchBorderColor, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (opt.color == null) {
                                            // Checkerboard pattern for Transparent
                                            Canvas(modifier = Modifier.fillMaxSize()) {
                                                val sq = size.width / 4f
                                                for (r in 0 until 4) {
                                                    for (c in 0 until 4) {
                                                        val isEven = (r + c) % 2 == 0
                                                        drawRect(
                                                            color = if (isEven) Color(0xFFDDDDDD) else Color(0xFF888888),
                                                            topLeft = Offset(c * sq, r * sq),
                                                            size = androidx.compose.ui.geometry.Size(sq, sq)
                                                        )
                                                    }
                                                }
                                            }
                                            if (isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(16.dp)
                                                        .clip(CircleShape)
                                                        .background(SolidPrimary),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
                                                }
                                            }
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(opt.color),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isSelected) {
                                                    val checkTint = if (opt.color == Color.White || opt.color == Color(0xFFFFFBEB)) Color.Black else Color.White
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = checkTint,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = opt.label,
                                        fontSize = 9.5.sp,
                                        color = if (isSelected) SolidPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 3. Quick Transform Actions Toolbar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { quarterTurns = (quarterTurns - 1 + 4) % 4 },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Rotate90DegreesCcw, contentDescription = "Rotate -90°", modifier = Modifier.size(19.dp))
                            }

                            IconButton(
                                onClick = { quarterTurns = (quarterTurns + 1) % 4 },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Rotate90DegreesCw, contentDescription = "Rotate +90°", modifier = Modifier.size(19.dp))
                            }

                            IconButton(
                                onClick = { flipHorizontal = !flipHorizontal },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.Flip,
                                    contentDescription = "Flip Horizontal",
                                    modifier = Modifier.size(19.dp),
                                    tint = if (flipHorizontal) SolidPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            IconButton(
                                onClick = { flipVertical = !flipVertical },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = "Flip Vertical",
                                    modifier = Modifier.size(19.dp),
                                    tint = if (flipVertical) SolidPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            IconButton(
                                onClick = {
                                    userScale = 1.0f
                                    panOffset = Offset.Zero
                                    quarterTurns = 0
                                    fineAngle = 0f
                                    flipHorizontal = false
                                    flipVertical = false
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.RestartAlt, contentDescription = "Reset", modifier = Modifier.size(19.dp))
                            }
                        }

                        // 3. Zoom Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.ZoomOut,
                                contentDescription = "Zoom",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Zoom: ${(userScale * 100).roundToInt()}%",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.width(76.dp)
                            )
                            Slider(
                                value = userScale,
                                onValueChange = { userScale = it },
                                valueRange = 0.3f..5.0f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = SolidPrimary,
                                    activeTrackColor = SolidPrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { userScale = (userScale + 0.25f).coerceAtMost(5.0f) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", modifier = Modifier.size(16.dp))
                            }
                        }

                        // 4. Fine Angle Straighten Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Crop,
                                contentDescription = "Angle",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Angle: ${if (fineAngle >= 0) "+" else ""}${fineAngle.roundToInt()}°",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.width(76.dp)
                            )
                            Slider(
                                value = fineAngle,
                                onValueChange = { fineAngle = it },
                                valueRange = -45f..45f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = SolidPrimary,
                                    activeTrackColor = SolidPrimary
                                )
                            )
                            if (fineAngle != 0f) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "0°",
                                    fontSize = 11.sp,
                                    color = SolidPrimary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clickable { fineAngle = 0f }
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    } else {
                        // TAB 2: ADJUST & FILTERS CONTROLS

                        // Filter Presets Row
                        Text("Style Presets", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PhotoFilterPreset.values().forEach { preset ->
                                val isSelected = filterPreset == preset
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { filterPreset = preset },
                                    label = { Text(preset.label, fontSize = 11.sp) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SolidPrimary.copy(alpha = 0.15f),
                                        selectedLabelColor = SolidPrimary
                                    ),
                                    modifier = Modifier.height(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Brightness Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Brightness: ${(brightnessAdj * 100).roundToInt()}%",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.width(110.dp)
                            )
                            Slider(
                                value = brightnessAdj,
                                onValueChange = { brightnessAdj = it },
                                valueRange = -0.5f..0.5f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = SolidPrimary,
                                    activeTrackColor = SolidPrimary
                                )
                            )
                        }

                        // Contrast Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Contrast: ${(contrastAdj * 100).roundToInt()}%",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.width(110.dp)
                            )
                            Slider(
                                value = contrastAdj,
                                onValueChange = { contrastAdj = it },
                                valueRange = -0.5f..0.5f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = SolidPrimary,
                                    activeTrackColor = SolidPrimary
                                )
                            )
                        }

                        // Saturation Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Saturation: ${(saturationAdj * 100).roundToInt()}%",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.width(110.dp)
                            )
                            Slider(
                                value = saturationAdj,
                                onValueChange = { saturationAdj = it },
                                valueRange = -1.0f..1.0f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = SolidPrimary,
                                    activeTrackColor = SolidPrimary
                                )
                            )
                        }

                        // Reset Adjustments
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "Reset Adjustments",
                                fontSize = 11.5.sp,
                                color = SolidPrimary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clickable {
                                        filterPreset = PhotoFilterPreset.ORIGINAL
                                        brightnessAdj = 0f
                                        contrastAdj = 0f
                                        saturationAdj = 0f
                                    }
                                    .padding(vertical = 4.dp, horizontal = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isSaving
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val bitmapToSave = loadedBitmap ?: return@Button
                            isSaving = true
                            coroutineScope.launch {
                                withContext(Dispatchers.IO) {
                                    val totalRotation = ((quarterTurns % 4) * 90f + fineAngle)
                                    val baseScale = if (fitMode == PhotoFitMode.FILL) {
                                        maxOf(cropBoxSizePx / bitmapToSave.width.toFloat(), cropBoxSizePx / bitmapToSave.height.toFloat())
                                    } else {
                                        minOf(cropBoxSizePx / bitmapToSave.width.toFloat(), cropBoxSizePx / bitmapToSave.height.toFloat())
                                    }
                                    val effectiveScale = baseScale * userScale

                                    val rendered = IconHelper.renderWysiwygCroppedBitmap(
                                        sourceBitmap = bitmapToSave,
                                        cropBoxSizePx = cropBoxSizePx,
                                        effectiveScreenScale = effectiveScale,
                                        panOffsetScreenX = panOffset.x,
                                        panOffsetScreenY = panOffset.y,
                                        rotationDegrees = totalRotation,
                                        flipHorizontal = flipHorizontal,
                                        flipVertical = flipVertical,
                                        cropShape = cropShape.id,
                                        colorMatrixValues = activeColorMatrixValues,
                                        backgroundColor = selectedBgColor?.toArgb(),
                                        outputSize = 512
                                    )

                                    val iconKey = IconHelper.saveCustomIconBitmap(context, rendered)
                                    withContext(Dispatchers.Main) {
                                        isSaving = false
                                        if (iconKey != null) {
                                            onCroppedIconSaved(iconKey)
                                            Toast.makeText(context, "Saved custom icon successfully!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Failed to save cropped photo", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary),
                        enabled = loadedBitmap != null && !isSaving && !isLoadingImage
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Crop & Save", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }

    if (showCustomColorDialog) {
        val presetCustomHexes = listOf(
            "#FFFFFF", "#000000", "#1E293B", "#0D9488",
            "#2563EB", "#7C3AED", "#DB2777", "#DC2626",
            "#D97706", "#059669", "#0891B2", "#475569"
        )
        val parsedCustomColor = remember(customColorHexInput) {
            IconHelper.parseColorHex(customColorHexInput, Color.White)
        }

        AlertDialog(
            onDismissRequest = { showCustomColorDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = SolidPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Custom Background Color", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Choose or enter a background color for transparent icons/photos:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(parsedCustomColor)
                                .border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        )

                        OutlinedTextField(
                            value = customColorHexInput,
                            onValueChange = { customColorHexInput = it },
                            label = { Text("Hex Code (e.g. #FFFFFF)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Palette Presets", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(presetCustomHexes) { hex ->
                            val c = IconHelper.parseColorHex(hex, Color.White)
                            val isSel = customColorHexInput.equals(hex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .border(
                                        width = if (isSel) 2.5.dp else 1.dp,
                                        color = if (isSel) SolidPrimary else MaterialTheme.colorScheme.outlineVariant,
                                        shape = CircleShape
                                    )
                                    .clickable { customColorHexInput = hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSel) {
                                    val checkTint = if (hex == "#FFFFFF") Color.Black else Color.White
                                    Icon(Icons.Default.Check, contentDescription = null, tint = checkTint, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedBgColor = parsedCustomColor
                        showCustomColorDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SolidPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Apply Color")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomColorDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Backward compatibility wrapper so existing code calling IconCropEditorModal
 * seamlessly uses the new, enhanced PhotoCropEditorModal.
 */
@Composable
fun IconCropEditorModal(
    imageUri: Uri? = null,
    imageUrl: String? = null,
    initialIconKey: String? = null,
    sourceBitmap: Bitmap? = null,
    onCroppedIconSaved: (String) -> Unit,
    onDismiss: () -> Unit
) {
    PhotoCropEditorModal(
        imageUri = imageUri,
        imageUrl = imageUrl,
        initialIconKey = initialIconKey,
        sourceBitmap = sourceBitmap,
        onCroppedIconSaved = onCroppedIconSaved,
        onDismiss = onDismiss
    )
}

/**
 * Computes a combined 4x5 ColorMatrix array for preset filters and user adjustments.
 */
private fun buildCombinedColorMatrix(
    preset: PhotoFilterPreset,
    brightness: Float,
    contrast: Float,
    saturation: Float
): FloatArray? {
    if (preset == PhotoFilterPreset.ORIGINAL && brightness == 0f && contrast == 0f && saturation == 0f) {
        return null
    }

    val cm = android.graphics.ColorMatrix()

    // 1. Preset filter baseline
    when (preset) {
        PhotoFilterPreset.ORIGINAL -> {
            // Identity
        }
        PhotoFilterPreset.VIVID -> {
            cm.setSaturation(1.35f)
        }
        PhotoFilterPreset.WARM -> {
            val warmMatrix = android.graphics.ColorMatrix(
                floatArrayOf(
                    1.10f, 0f, 0f, 0f, 15f,
                    0f, 1.04f, 0f, 0f, 8f,
                    0f, 0f, 0.90f, 0f, -10f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            cm.postConcat(warmMatrix)
        }
        PhotoFilterPreset.COOL -> {
            val coolMatrix = android.graphics.ColorMatrix(
                floatArrayOf(
                    0.92f, 0f, 0f, 0f, -8f,
                    0f, 1.02f, 0f, 0f, 4f,
                    0f, 0f, 1.15f, 0f, 18f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            cm.postConcat(coolMatrix)
        }
        PhotoFilterPreset.BW -> {
            cm.setSaturation(0.0f)
        }
        PhotoFilterPreset.DRAMATIC -> {
            val scale = 1.35f
            val translate = (-0.5f * scale + 0.5f) * 255f
            val contrastMat = android.graphics.ColorMatrix(
                floatArrayOf(
                    scale, 0f, 0f, 0f, translate,
                    0f, scale, 0f, 0f, translate,
                    0f, 0f, scale, 0f, translate,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            cm.postConcat(contrastMat)
            val satMat = android.graphics.ColorMatrix()
            satMat.setSaturation(1.20f)
            cm.postConcat(satMat)
        }
    }

    // 2. User Saturation Adjustment
    if (saturation != 0f) {
        val satMat = android.graphics.ColorMatrix()
        satMat.setSaturation(1.0f + saturation)
        cm.postConcat(satMat)
    }

    // 3. User Contrast Adjustment
    if (contrast != 0f) {
        val scale = 1.0f + contrast
        val translate = (-0.5f * scale + 0.5f) * 255f
        val contrastMat = android.graphics.ColorMatrix(
            floatArrayOf(
                scale, 0f, 0f, 0f, translate,
                0f, scale, 0f, 0f, translate,
                0f, 0f, scale, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )
        )
        cm.postConcat(contrastMat)
    }

    // 4. User Brightness Adjustment
    if (brightness != 0f) {
        val bShift = brightness * 128f
        val brightMat = android.graphics.ColorMatrix(
            floatArrayOf(
                1f, 0f, 0f, 0f, bShift,
                0f, 1f, 0f, 0f, bShift,
                0f, 0f, 1f, 0f, bShift,
                0f, 0f, 0f, 1f, 0f
            )
        )
        cm.postConcat(brightMat)
    }

    return cm.array
}
