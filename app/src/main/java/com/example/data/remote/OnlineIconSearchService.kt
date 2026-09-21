package com.example.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.util.IconHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class OnlineIconResult(
    val title: String,
    val imageUrl: String,
    val sourceName: String,
    val isColorful: Boolean = false
) {
    val name: String get() = title
    val downloadUrl: String get() = imageUrl
    val previewUrl: String get() = imageUrl
}

data class OnlineImageResult(
    val title: String,
    val imageUrl: String,
    val thumbUrl: String = imageUrl,
    val sourceName: String,
    val width: Int = 0,
    val height: Int = 0
) {
    val name: String get() = title
    val downloadUrl: String get() = imageUrl
    val previewUrl: String get() = thumbUrl
    val source: String get() = sourceName
}

object OnlineIconSearchService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private const val USER_AGENT = "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

    private val KNOWN_COLORFUL_SOURCES = setOf(
        "SVGL", "Brandfetch", "Favicon", "LOGOS", "FLAT-COLOR-ICONS",
        "FLUENT-EMOJI", "TWEMOJI", "OPENMOJI", "CIRCLE-FLAGS", "SKILL-ICONS", "VSCODE-ICONS", "NOTO"
    )

    /**
     * Searches for logos and icons online using keyless free providers with pagination,
     * sorting colorful icons first before colorless/monochrome icons:
     * 1. SVGL Open Brand Logos (Vibrant vector logos for brands, tech, fintech)
     * 2. Iconify Vector Icon Search (Open API, millions of vector icons across sets)
     * 3. Brandfetch Brand Logo Search
     * 4. Wikimedia Commons Open Media Search
     * 5. DuckDuckGo Instant Topics & Images
     * 6. Google Favicon Service
     */
    suspend fun searchIcons(context: Context?, query: String, page: Int = 1): List<OnlineIconResult> = searchIcons(query, context, page)

    suspend fun searchIcons(query: String, context: Context? = null, page: Int = 1): List<OnlineIconResult> = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) return@withContext emptyList()

        val results = mutableListOf<OnlineIconResult>()
        val seenUrls = mutableSetOf<String>()

        val encoded = try {
            URLEncoder.encode(cleanQuery, "UTF-8")
        } catch (_: Exception) {
            cleanQuery
        }

        // 1. SVGL Open Logos API (High-res, multi-color brand and tech vector logos)
        try {
            val svglUrl = "https://api.svgl.app?search=$encoded"
            val request = Request.Builder()
                .url(svglUrl)
                .header("User-Agent", USER_AGENT)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        val jsonArr = JSONArray(body)
                        val startIdx = (page - 1) * 6
                        val endIdx = minOf(jsonArr.length(), page * 6)
                        for (i in startIdx until endIdx) {
                            val obj = jsonArr.optJSONObject(i) ?: continue
                            val title = obj.optString("title").ifBlank { obj.optString("name") }
                            var routeUrl = obj.optString("route")
                            if (routeUrl.isBlank()) {
                                val routeObj = obj.optJSONObject("route")
                                if (routeObj != null) {
                                    routeUrl = routeObj.optString("light").ifBlank { routeObj.optString("dark") }
                                }
                            }
                            if (routeUrl.isNotBlank() && routeUrl.startsWith("http") && seenUrls.add(routeUrl)) {
                                results.add(
                                    OnlineIconResult(
                                        title = title.ifBlank { cleanQuery },
                                        imageUrl = routeUrl,
                                        sourceName = "SVGL",
                                        isColorful = true
                                    )
                                )
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Proceed to other providers
        }

        // 2. Iconify Open Vector Icon API (Supports both colorful vector sets and line icon sets)
        try {
            val start = (page - 1) * 14
            val iconifyUrl = "https://api.iconify.design/search?query=$encoded&limit=14&start=$start"
            val request = Request.Builder()
                .url(iconifyUrl)
                .header("User-Agent", USER_AGENT)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        val json = JSONObject(body)
                        val iconsArr = json.optJSONArray("icons")
                        if (iconsArr != null) {
                            for (i in 0 until iconsArr.length()) {
                                val iconStr = iconsArr.optString(i)
                                if (iconStr.isNotBlank() && iconStr.contains(":")) {
                                    val parts = iconStr.split(":", limit = 2)
                                    val prefix = parts[0].uppercase()
                                    val iconName = parts[1]
                                    val svgUrl = "https://api.iconify.design/${parts[0]}/$iconName.svg"
                                    if (seenUrls.add(svgUrl)) {
                                        val cleanTitle = iconName.replace("-", " ")
                                            .replaceFirstChar { it.uppercase() }
                                        val isKnownColorful = prefix in KNOWN_COLORFUL_SOURCES
                                        results.add(
                                            OnlineIconResult(
                                                title = cleanTitle,
                                                imageUrl = svgUrl,
                                                sourceName = prefix,
                                                isColorful = isKnownColorful
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Proceed to other providers
        }

        // 3. Search Brandfetch for company / app logos (Colorful brandmarks)
        try {
            val request = Request.Builder()
                .url("https://api.brandfetch.io/v2/search/$encoded")
                .header("User-Agent", USER_AGENT)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        val jsonArr = JSONArray(body)
                        val startIdx = (page - 1) * 6
                        val endIdx = minOf(jsonArr.length(), page * 6)
                        for (i in startIdx until endIdx) {
                            val obj = jsonArr.optJSONObject(i) ?: continue
                            val iconUrl = obj.optString("icon")
                            val name = obj.optString("name").ifBlank { obj.optString("domain") }
                            if (iconUrl.isNotBlank() && iconUrl.startsWith("http") && seenUrls.add(iconUrl)) {
                                results.add(
                                    OnlineIconResult(
                                        title = name,
                                        imageUrl = iconUrl,
                                        sourceName = "Brandfetch",
                                        isColorful = true
                                    )
                                )
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Proceed
        }

        // 4. Wikimedia Commons Open Media Search
        try {
            val offset = (page - 1) * 10
            val wmUrl = "https://commons.wikimedia.org/w/api.php?action=query&generator=search&gsrnamespace=6&gsrsearch=filetype:bitmap|drawing+$encoded+logo|icon&gsrlimit=10&gsroffset=$offset&prop=imageinfo&iiprop=url|thumburl&iiurlwidth=160&format=json"
            val request = Request.Builder()
                .url(wmUrl)
                .header("User-Agent", "BudgeterApp/1.0 (Android; open-source)")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        val root = JSONObject(body)
                        val pages = root.optJSONObject("query")?.optJSONObject("pages")
                        if (pages != null) {
                            val keys = pages.keys()
                            while (keys.hasNext()) {
                                val key = keys.next()
                                val pageObj = pages.optJSONObject(key) ?: continue
                                val imageInfoArr = pageObj.optJSONArray("imageinfo") ?: continue
                                val info = imageInfoArr.optJSONObject(0) ?: continue
                                val thumbUrl = info.optString("thumburl").ifBlank { info.optString("url") }
                                if (thumbUrl.isNotBlank() && seenUrls.add(thumbUrl)) {
                                    val rawTitle = pageObj.optString("title", cleanQuery)
                                        .replace("File:", "")
                                        .substringBeforeLast(".")
                                        .take(28)
                                    results.add(
                                        OnlineIconResult(
                                            title = rawTitle,
                                            imageUrl = thumbUrl,
                                            sourceName = "Wikimedia",
                                            isColorful = true
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Proceed
        }

        // 5. DuckDuckGo Instant Answer / Topics / Open Search
        try {
            val request = Request.Builder()
                .url("https://api.duckduckgo.com/?q=$encoded&format=json")
                .header("User-Agent", USER_AGENT)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        val obj = JSONObject(body)
                        if (page == 1) {
                            val mainImg = obj.optString("Image")
                            if (mainImg.isNotBlank()) {
                                val fullUrl = if (mainImg.startsWith("/")) "https://duckduckgo.com$mainImg" else mainImg
                                if (seenUrls.add(fullUrl)) {
                                    results.add(
                                        OnlineIconResult(
                                            title = obj.optString("Heading", cleanQuery),
                                            imageUrl = fullUrl,
                                            sourceName = "DuckDuckGo",
                                            isColorful = true
                                        )
                                    )
                                }
                            }
                        }

                        val relatedTopics = obj.optJSONArray("RelatedTopics")
                        if (relatedTopics != null) {
                            val startIdx = (page - 1) * 6
                            val endIdx = minOf(relatedTopics.length(), page * 6)
                            for (i in startIdx until endIdx) {
                                val topic = relatedTopics.optJSONObject(i) ?: continue
                                val iconObj = topic.optJSONObject("Icon") ?: continue
                                val iconUrl = iconObj.optString("URL")
                                if (iconUrl.isNotBlank()) {
                                    val fullUrl = if (iconUrl.startsWith("/")) "https://duckduckgo.com$iconUrl" else iconUrl
                                    if (seenUrls.add(fullUrl)) {
                                        val text = topic.optString("Text", cleanQuery).take(30)
                                        results.add(
                                            OnlineIconResult(
                                                title = text,
                                                imageUrl = fullUrl,
                                                sourceName = "DuckDuckGo",
                                                isColorful = true
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Proceed
        }

        // 6. Google Favicon (included on first page)
        if (page == 1) {
            try {
                val noSpaces = cleanQuery.replace(" ", "").lowercase()
                val domain = if (noSpaces.contains(".")) noSpaces else "$noSpaces.com"
                val googleFaviconUrl = "https://t1.gstatic.com/faviconV2?client=SOCIAL&type=FAVICON&fallback_opts=TYPE,SIZE,URL&url=https://$domain&size=128"
                if (seenUrls.add(googleFaviconUrl)) {
                    results.add(
                        OnlineIconResult(
                            title = cleanQuery.replaceFirstChar { it.uppercase() },
                            imageUrl = googleFaviconUrl,
                            sourceName = "Favicon",
                            isColorful = true
                        )
                    )
                }
            } catch (_: Exception) {
                // Proceed
            }
        }

        // Concurrently pre-validate all candidate images so dead, broken, or unrenderable URLs are excluded before returning
        val validatedResults = coroutineScope {
            results.map { item ->
                async(Dispatchers.IO) {
                    val analysis = validateAndAnalyzeColor(context, item)
                    if (analysis != null) analysis else null
                }
            }.awaitAll().filterNotNull()
        }

        // Sort colorful icons first, followed by monochrome/colorless icons
        val sortedResults = validatedResults.sortedWith(
            compareByDescending<OnlineIconResult> { it.isColorful }
                .thenBy { it.sourceName == "Wikimedia" } // Prefer dedicated icon/logo libraries
        )

        sortedResults
    }

    /**
     * Validates that an image URL is reachable, valid, and analyzes whether it is colorful or monochrome.
     */
    private suspend fun validateAndAnalyzeColor(context: Context?, item: OnlineIconResult): OnlineIconResult? = withContext(Dispatchers.IO) {
        val url = item.imageUrl
        if (url.isBlank() || !url.startsWith("http")) return@withContext null

        // 1. Check HTTP reachability and content type
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .build()

            var isSvg = url.endsWith(".svg", ignoreCase = true)
            var isImage = false

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val contentType = response.header("Content-Type")?.lowercase() ?: ""
                val contentLength = response.body?.contentLength() ?: -1L
                if (contentLength == 0L) return@withContext null

                if (contentType.startsWith("text/html")) return@withContext null

                isSvg = isSvg || contentType.contains("svg")
                isImage = contentType.startsWith("image/") ||
                        contentType.contains("svg") ||
                        contentType.contains("xml") ||
                        contentType.contains("octet-stream") ||
                        url.endsWith(".png") ||
                        url.endsWith(".webp") ||
                        url.endsWith(".jpg") ||
                        url.endsWith(".jpeg") ||
                        url.endsWith(".ico") ||
                        url.contains("favicon")

                if (!isImage) return@withContext null
            }

            // 2. If source is inherently colorful (SVGL, Brandfetch, Favicon, Twemoji, etc.), mark as colorful
            if (item.sourceName in KNOWN_COLORFUL_SOURCES || item.isColorful) {
                return@withContext item.copy(isColorful = true)
            }

            // 3. For other items, check bitmap saturation if context is available
            if (context != null) {
                try {
                    val loader = context.imageLoader
                    val req = ImageRequest.Builder(context)
                        .data(url)
                        .size(32, 32)
                        .allowHardware(false)
                        .build()
                    val res = loader.execute(req)
                    if (res is SuccessResult) {
                        val drawable = res.drawable
                        val bmp = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bmp)
                        drawable.setBounds(0, 0, 32, 32)
                        drawable.draw(canvas)

                        val isColorful = checkBitmapColorfulness(bmp)
                        return@withContext item.copy(isColorful = isColorful)
                    }
                } catch (_: Exception) {
                    // Fallback to initial isColorful state
                }
            }

            item
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Samples pixels of a small bitmap to determine if it has meaningful color saturation.
     */
    private fun checkBitmapColorfulness(bitmap: Bitmap): Boolean {
        var colorfulPixels = 0
        var totalVisiblePixels = 0
        val hsv = FloatArray(3)
        val step = maxOf(1, bitmap.width / 8)

        for (x in 0 until bitmap.width step step) {
            for (y in 0 until bitmap.height step step) {
                val pixel = bitmap.getPixel(x, y)
                val alpha = (pixel ushr 24) and 0xFF
                if (alpha > 30) {
                    totalVisiblePixels++
                    val r = (pixel ushr 16) and 0xFF
                    val g = (pixel ushr 8) and 0xFF
                    val b = pixel and 0xFF
                    Color.RGBToHSV(r, g, b, hsv)
                    // Saturation >= 0.22 and brightness >= 0.15 indicates genuine color (not black, white, or gray)
                    if (hsv[1] >= 0.22f && hsv[2] >= 0.15f) {
                        colorfulPixels++
                    }
                }
            }
        }
        return if (totalVisiblePixels > 0) {
            (colorfulPixels.toFloat() / totalVisiblePixels.toFloat()) >= 0.15f
        } else {
            false
        }
    }

    /**
     * Searches online images and photos across multiple free, keyless endpoints:
     * 1. Wikimedia Commons High-Res Media API
     * 2. Openverse Open Image Library
     * 3. DuckDuckGo Image Results
     * 4. Unsplash Public Search
     */
    suspend fun searchImages(context: Context?, query: String, page: Int = 1): List<OnlineImageResult> = searchImages(query, context, page)

    suspend fun searchImages(query: String, context: Context? = null, page: Int = 1): List<OnlineImageResult> = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) return@withContext emptyList()

        val results = mutableListOf<OnlineImageResult>()
        val seenUrls = mutableSetOf<String>()

        val encoded = try {
            URLEncoder.encode(cleanQuery, "UTF-8")
        } catch (_: Exception) {
            cleanQuery
        }

        coroutineScope {
            // 1. Wikimedia Commons High Quality Media
            val wikiJob = async {
                try {
                    val offset = (page - 1) * 16
                    val url = "https://commons.wikimedia.org/w/api.php?action=query&generator=search&gsrnamespace=6&gsrsearch=filetype:bitmap+$encoded&gsrlimit=16&gsroffset=$offset&prop=imageinfo&iiprop=url|thumburl|dimensions&iiurlwidth=500&format=json"
                    val request = Request.Builder()
                        .url(url)
                        .header("User-Agent", USER_AGENT)
                        .build()
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val body = response.body?.string().orEmpty()
                        response.close()
                        val json = JSONObject(body)
                        val queryObj = json.optJSONObject("query")
                        val pages = queryObj?.optJSONObject("pages")
                        if (pages != null) {
                            val keys = pages.keys()
                            val pageResults = mutableListOf<OnlineImageResult>()
                            while (keys.hasNext()) {
                                val key = keys.next()
                                val pageItem = pages.optJSONObject(key) ?: continue
                                val title = pageItem.optString("title", "Image")
                                    .removePrefix("File:")
                                    .substringBeforeLast(".")
                                    .replace("_", " ")
                                val imageinfo = pageItem.optJSONArray("imageinfo")
                                if (imageinfo != null && imageinfo.length() > 0) {
                                    val info = imageinfo.optJSONObject(0) ?: continue
                                    val fullUrl = info.optString("url")
                                    val thumbUrl = info.optString("thumburl", fullUrl)
                                    val width = info.optInt("width", 0)
                                    val height = info.optInt("height", 0)
                                    if (fullUrl.isNotBlank() && !fullUrl.endsWith(".svg", ignoreCase = true)) {
                                        pageResults.add(
                                            OnlineImageResult(
                                                title = title,
                                                imageUrl = fullUrl,
                                                thumbUrl = thumbUrl,
                                                sourceName = "Wikimedia",
                                                width = width,
                                                height = height
                                            )
                                        )
                                    }
                                }
                            }
                            pageResults
                        } else emptyList()
                    } else {
                        response.close()
                        emptyList()
                    }
                } catch (_: Exception) {
                    emptyList()
                }
            }

            // 2. Openverse Free Photos
            val openverseJob = async {
                try {
                    val url = "https://api.openverse.org/v1/images/?q=$encoded&page=$page&page_size=16"
                    val request = Request.Builder()
                        .url(url)
                        .header("User-Agent", USER_AGENT)
                        .build()
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val body = response.body?.string().orEmpty()
                        response.close()
                        val json = JSONObject(body)
                        val resultsArr = json.optJSONArray("results")
                        val items = mutableListOf<OnlineImageResult>()
                        if (resultsArr != null) {
                            for (i in 0 until resultsArr.length()) {
                                val item = resultsArr.optJSONObject(i) ?: continue
                                val title = item.optString("title", cleanQuery)
                                val imgUrl = item.optString("url")
                                val thumb = item.optString("thumbnail", imgUrl)
                                val provider = item.optString("provider", "Openverse").uppercase()
                                if (imgUrl.isNotBlank()) {
                                    items.add(
                                        OnlineImageResult(
                                            title = title,
                                            imageUrl = imgUrl,
                                            thumbUrl = thumb,
                                            sourceName = provider
                                        )
                                    )
                                }
                            }
                        }
                        items
                    } else {
                        response.close()
                        emptyList()
                    }
                } catch (_: Exception) {
                    emptyList()
                }
            }

            // 3. Unsplash Public Search
            val unsplashJob = async {
                try {
                    val url = "https://unsplash.com/napi/search/photos?query=$encoded&per_page=16&page=$page"
                    val request = Request.Builder()
                        .url(url)
                        .header("User-Agent", USER_AGENT)
                        .build()
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val body = response.body?.string().orEmpty()
                        response.close()
                        val json = JSONObject(body)
                        val resultsArr = json.optJSONArray("results")
                        val items = mutableListOf<OnlineImageResult>()
                        if (resultsArr != null) {
                            for (i in 0 until resultsArr.length()) {
                                val item = resultsArr.optJSONObject(i) ?: continue
                                val title = item.optString("alt_description", item.optString("description", cleanQuery))
                                val urls = item.optJSONObject("urls") ?: continue
                                val regular = urls.optString("regular")
                                val small = urls.optString("small", regular)
                                if (regular.isNotBlank()) {
                                    items.add(
                                        OnlineImageResult(
                                            title = if (title.isBlank()) cleanQuery else title,
                                            imageUrl = regular,
                                            thumbUrl = small,
                                            sourceName = "Unsplash"
                                        )
                                    )
                                }
                            }
                        }
                        items
                    } else {
                        response.close()
                        emptyList()
                    }
                } catch (_: Exception) {
                    emptyList()
                }
            }

            val (wikiResults, openverseResults, unsplashResults) = awaitAll(wikiJob, openverseJob, unsplashJob)
            
            // Interleave and deduplicate results
            val maxLen = maxOf(wikiResults.size, openverseResults.size, unsplashResults.size)
            for (i in 0 until maxLen) {
                if (i < wikiResults.size) {
                    val item = wikiResults[i]
                    if (seenUrls.add(item.imageUrl)) results.add(item)
                }
                if (i < unsplashResults.size) {
                    val item = unsplashResults[i]
                    if (seenUrls.add(item.imageUrl)) results.add(item)
                }
                if (i < openverseResults.size) {
                    val item = openverseResults[i]
                    if (seenUrls.add(item.imageUrl)) results.add(item)
                }
            }
        }

        results
    }

    /**
     * Downloads an online image by URL (supports vector SVG, PNG, WebP, JPG)
     * and persists it locally into the app's custom icons directory with optimal compression.
     * Returns the persistent custom icon key (e.g. "custom_icon_172...").
     */
    suspend fun downloadAndSaveIcon(context: Context, imageUrl: String, name: String? = null): String? = withContext(Dispatchers.IO) {
        try {
            val customDir = File(context.filesDir, "custom_icons")
            if (!customDir.exists()) customDir.mkdirs()

            val iconKey = "custom_icon_${System.currentTimeMillis()}"
            val destFile = File(customDir, "$iconKey.png")

            // 1. Attempt loading and rasterizing via Coil ImageLoader (handles SVGs, PNGs, WebP, JPG)
            try {
                val loader = context.imageLoader
                val request = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .size(384, 384)
                    .allowHardware(false)
                    .build()

                val result = loader.execute(request)
                if (result is SuccessResult) {
                    val drawable = result.drawable
                    val targetSize = 384
                    val bitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    drawable.setBounds(0, 0, targetSize, targetSize)
                    drawable.draw(canvas)

                    FileOutputStream(destFile).use { outStream ->
                        val format = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                            Bitmap.CompressFormat.WEBP_LOSSY
                        } else {
                            @Suppress("DEPRECATION")
                            Bitmap.CompressFormat.WEBP
                        }
                        val compressed = try {
                            bitmap.compress(format, 92, outStream)
                        } catch (_: Exception) {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 90, outStream)
                        }
                        if (!compressed) {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 90, outStream)
                        }
                        outStream.flush()
                    }
                    return@withContext iconKey
                }
            } catch (_: Exception) {
                // Fallback to direct HTTP stream
            }

            // 2. Direct HTTP download fallback
            val request = Request.Builder()
                .url(imageUrl)
                .header("User-Agent", USER_AGENT)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                response.close()
                return@withContext null
            }

            val bytes = response.body?.bytes() ?: return@withContext null
            response.close()

            val originalBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                ?: return@withContext null

            val targetDim = 384
            val scaleFactor = targetDim.toFloat() / maxOf(originalBitmap.width, originalBitmap.height)
            val scaledBitmap = Bitmap.createScaledBitmap(
                originalBitmap,
                (originalBitmap.width * scaleFactor).toInt().coerceAtLeast(1),
                (originalBitmap.height * scaleFactor).toInt().coerceAtLeast(1),
                true
            )

            FileOutputStream(destFile).use { outStream ->
                val format = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSY
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
                val compressed = try {
                    scaledBitmap.compress(format, 92, outStream)
                } catch (_: Exception) {
                    scaledBitmap.compress(Bitmap.CompressFormat.PNG, 90, outStream)
                }
                if (!compressed) {
                    scaledBitmap.compress(Bitmap.CompressFormat.PNG, 90, outStream)
                }
                outStream.flush()
            }

            iconKey
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Downloads an online image into a high-res in-memory Bitmap suitable for editing and cropping.
     */
    suspend fun downloadBitmap(context: Context, imageUrl: String, targetSize: Int = 512): Bitmap? = withContext(Dispatchers.IO) {
        try {
            // 1. Attempt loading and rasterizing via Coil ImageLoader (supports SVGs, PNGs, WebP, JPG)
            try {
                val loader = context.imageLoader
                val request = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .size(targetSize, targetSize)
                    .allowHardware(false)
                    .build()

                val result = loader.execute(request)
                if (result is SuccessResult) {
                    val drawable = result.drawable
                    val bitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    drawable.setBounds(0, 0, targetSize, targetSize)
                    drawable.draw(canvas)
                    return@withContext bitmap
                }
            } catch (_: Exception) {}

            // 2. Direct HTTP download fallback
            val request = Request.Builder()
                .url(imageUrl)
                .header("User-Agent", USER_AGENT)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                response.close()
                return@withContext null
            }

            val bytes = response.body?.bytes() ?: return@withContext null
            response.close()

            val originalBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return@withContext null
            Bitmap.createScaledBitmap(originalBitmap, targetSize, targetSize, true)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
