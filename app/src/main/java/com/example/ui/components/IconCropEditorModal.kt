package com.example.ui.components

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Rotate90DegreesCcw
import androidx.compose.material.icons.filled.Rotate90DegreesCw
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.SolidPrimary
import com.example.util.IconHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

/**
 * Interactive modal for cropping, rotating, zooming, panning, and flipping custom icons.
 */
@Composable
fun IconCropEditorModal(
    imageUri: Uri? = null,
    initialIconKey: String? = null,
    sourceBitmap: Bitmap? = null,
    onCroppedIconSaved: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var loadedBitmap by remember { mutableStateOf<Bitmap?>(sourceBitmap) }
    var isLoadingImage by remember { mutableStateOf(loadedBitmap == null) }

    // Transformation States
    var scale by remember { mutableFloatStateOf(1.0f) }
    var rotationDegrees by remember { mutableFloatStateOf(0f) }
    var panX by remember { mutableFloatStateOf(0f) }
    var panY by remember { mutableFloatStateOf(0f) }
    var flipHorizontal by remember { mutableStateOf(false) }
    var flipVertical by remember { mutableStateOf(false) }
    var isCircleShape by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    // Load bitmap asynchronously
    LaunchedEffect(imageUri, initialIconKey, sourceBitmap) {
        if (sourceBitmap != null) {
            loadedBitmap = sourceBitmap
            isLoadingImage = false
            return@LaunchedEffect
        }
        isLoadingImage = true
        withContext(Dispatchers.IO) {
            val bitmap = when {
                imageUri != null -> IconHelper.decodeBitmapFromUri(context, imageUri, 1024)
                initialIconKey != null -> IconHelper.decodeBitmapFromCustomKey(context, initialIconKey)
                else -> null
            }
            withContext(Dispatchers.Main) {
                loadedBitmap = bitmap
                isLoadingImage = false
            }
        }
    }

    Dialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Crop & Adjust Icon",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Pinch or slide to zoom, rotate & pan",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Reset button
                    IconButton(
                        onClick = {
                            scale = 1.0f
                            rotationDegrees = 0f
                            panX = 0f
                            panY = 0f
                            flipHorizontal = false
                            flipVertical = false
                        },
                        enabled = !isLoadingImage && !isSaving
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset Transform",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Viewport / Interactive Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .aspectRatio(1.05f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.88f))
                        .pointerInput(loadedBitmap) {
                            if (loadedBitmap == null) return@pointerInput
                            detectTransformGestures { _, pan, zoom, rotation ->
                                scale = (scale * zoom).coerceIn(0.4f, 5.0f)
                                panX += pan.x
                                panY += pan.y
                                rotationDegrees = (rotationDegrees + rotation) % 360f
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

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val canvasW = size.width
                            val canvasH = size.height
                            val cropRadius = minOf(canvasW, canvasH) * 0.40f
                            val centerX = canvasW / 2f
                            val centerY = canvasH / 2f

                            val cropPath = Path().apply {
                                if (isCircleShape) {
                                    addOval(
                                        androidx.compose.ui.geometry.Rect(
                                            centerX - cropRadius,
                                            centerY - cropRadius,
                                            centerX + cropRadius,
                                            centerY + cropRadius
                                        )
                                    )
                                } else {
                                    addRoundRect(
                                        androidx.compose.ui.geometry.RoundRect(
                                            left = centerX - cropRadius,
                                            top = centerY - cropRadius,
                                            right = centerX + cropRadius,
                                            bottom = centerY + cropRadius,
                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                                                cropRadius * 0.35f,
                                                cropRadius * 0.35f
                                            )
                                        )
                                    )
                                }
                            }

                            // 1. Draw image with scale, rotate, pan
                            val baseScale = (cropRadius * 2f) / maxOf(bmp.width, bmp.height).toFloat()
                            val effectiveScaleX = if (flipHorizontal) -scale * baseScale else scale * baseScale
                            val effectiveScaleY = if (flipVertical) -scale * baseScale else scale * baseScale

                            withTransform({
                                translate(centerX + panX, centerY + panY)
                                rotate(rotationDegrees)
                                scale(effectiveScaleX, effectiveScaleY)
                                translate(-bmp.width / 2f, -bmp.height / 2f)
                            }) {
                                drawImage(composeBitmap)
                            }

                            // 2. Dim outside area
                            clipPath(cropPath, clipOp = ClipOp.Difference) {
                                drawRect(
                                    color = Color.Black.copy(alpha = 0.65f),
                                    size = size
                                )
                            }

                            // 3. Draw crop boundary stroke & grid
                            drawPath(
                                path = cropPath,
                                color = Color.White.copy(alpha = 0.85f),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5.dp.toPx())
                            )

                            // Subtle guideline crosses
                            drawLine(
                                color = Color.White.copy(alpha = 0.25f),
                                start = Offset(centerX - cropRadius, centerY),
                                end = Offset(centerX + cropRadius, centerY),
                                strokeWidth = 1.dp.toPx()
                            )
                            drawLine(
                                color = Color.White.copy(alpha = 0.25f),
                                start = Offset(centerX, centerY - cropRadius),
                                end = Offset(centerX, centerY + cropRadius),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    } else {
                        Text(
                            text = "Could not load image",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Controls Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Shape & Flip Controls Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Shape Selector Chips
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = isCircleShape,
                                onClick = { isCircleShape = true },
                                label = { Text("Circle", fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SolidPrimary.copy(alpha = 0.15f),
                                    selectedLabelColor = SolidPrimary
                                )
                            )
                            FilterChip(
                                selected = !isCircleShape,
                                onClick = { isCircleShape = false },
                                label = { Text("Square", fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.CropSquare,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SolidPrimary.copy(alpha = 0.15f),
                                    selectedLabelColor = SolidPrimary
                                )
                            )
                        }

                        // Flip & Rotate Quick Buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = { flipHorizontal = !flipHorizontal },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Flip,
                                    contentDescription = "Flip Horizontal",
                                    tint = if (flipHorizontal) SolidPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(
                                onClick = { rotationDegrees = (rotationDegrees - 90f) % 360f },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Rotate90DegreesCcw,
                                    contentDescription = "Rotate -90",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(
                                onClick = { rotationDegrees = (rotationDegrees + 90f) % 360f },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Rotate90DegreesCw,
                                    contentDescription = "Rotate +90",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Zoom Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = "Zoom",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Zoom: ${(scale * 100).roundToInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.width(76.dp)
                        )
                        IconButton(
                            onClick = { scale = (scale - 0.15f).coerceAtLeast(0.4f) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Zoom Out",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Slider(
                            value = scale,
                            onValueChange = { scale = it },
                            valueRange = 0.4f..4.0f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = SolidPrimary,
                                activeTrackColor = SolidPrimary
                            )
                        )
                        IconButton(
                            onClick = { scale = (scale + 0.15f).coerceAtMost(4.0f) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Zoom In",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Rotation Angle Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.RotateRight,
                            contentDescription = "Angle",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val angleInt = ((rotationDegrees % 360f + 360f) % 360f).roundToInt()
                        Text(
                            text = "Angle: $angleInt°",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.width(76.dp)
                        )
                        Slider(
                            value = (rotationDegrees % 360f + 360f) % 360f,
                            onValueChange = { rotationDegrees = it },
                            valueRange = 0f..360f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = SolidPrimary,
                                activeTrackColor = SolidPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

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
                                    val rendered = IconHelper.renderTransformedBitmap(
                                        sourceBitmap = bitmapToSave,
                                        scale = scale,
                                        rotationDegrees = rotationDegrees,
                                        panX = panX,
                                        panY = panY,
                                        flipHorizontal = flipHorizontal,
                                        flipVertical = flipVertical,
                                        isCircleShape = isCircleShape,
                                        outputSize = 384
                                    )
                                    val iconKey = IconHelper.saveCustomIconBitmap(context, rendered)
                                    withContext(Dispatchers.Main) {
                                        isSaving = false
                                        if (iconKey != null) {
                                            onCroppedIconSaved(iconKey)
                                            Toast.makeText(context, "Icon saved successfully!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Failed to save cropped icon", Toast.LENGTH_SHORT).show()
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
                            Text("Apply & Save", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
