package com.example.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
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
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class OnlineIconResult(
    val title: String,
    val imageUrl: String,
    val sourceName: String
)

object OnlineIconSearchService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private const val USER_AGENT = "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

    /**
     * Searches for logos and icons online using open, keyless providers with pagination:
     * 1. Iconify Vector Icon Search (Open API, millions of vector icons across sets)
     * 2. Wikimedia Commons Open Media Search
     * 3. DuckDuckGo Instant Topics & Images
     * 4. Brandfetch Brand Logo Search
     * 5. Google Favicon / Clearbit
     */
    suspend fun searchIcons(query: String, page: Int = 1): List<OnlineIconResult> = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) return@withContext emptyList()

        val results = mutableListOf<OnlineIconResult>()
        val seenUrls = mutableSetOf<String>()

        // 1. Iconify Open Vector Icon API (free, open-source icons: Lucide, Material, Tabler, Remix, FontAwesome)
        try {
            val encoded = URLEncoder.encode(cleanQuery, "UTF-8")
            val start = (page - 1) * 12
            val iconifyUrl = "https://api.iconify.design/search?query=$encoded&limit=12&start=$start"
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
                                    val prefix = parts[0]
                                    val iconName = parts[1]
                                    val svgUrl = "https://api.iconify.design/$prefix/$iconName.svg"
                                    if (seenUrls.add(svgUrl)) {
                                        val cleanTitle = iconName.replace("-", " ")
                                            .replaceFirstChar { it.uppercase() }
                                        results.add(
                                            OnlineIconResult(
                                                title = cleanTitle,
                                                imageUrl = svgUrl,
                                                sourceName = prefix.uppercase()
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

        // 2. Wikimedia Commons Open Search (supports pagination via gsroffset)
        try {
            val encoded = URLEncoder.encode("$cleanQuery icon", "UTF-8")
            val offset = (page - 1) * 12
            val wmUrl = "https://commons.wikimedia.org/w/api.php?action=query&generator=search&gsrnamespace=6&gsrsearch=filetype:bitmap|drawing+$encoded&gsrlimit=12&gsroffset=$offset&prop=imageinfo&iiprop=url|thumburl&iiurlwidth=160&format=json"
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
                                            sourceName = "Wikimedia"
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

        // 3. DuckDuckGo Instant Answer / Topics / Open Search
        try {
            val encoded = URLEncoder.encode(cleanQuery, "UTF-8")
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
                                            sourceName = "DuckDuckGo"
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
                                                sourceName = "DuckDuckGo"
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

        // 4. Search Brandfetch for company / app logos
        try {
            val encoded = URLEncoder.encode(cleanQuery, "UTF-8")
            val request = Request.Builder()
                .url("https://api.brandfetch.io/v2/search/$encoded")
                .header("User-Agent", USER_AGENT)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        val jsonArr = org.json.JSONArray(body)
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
                                        sourceName = "Brandfetch"
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

        // 5. Google Favicon (included on first page)
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
                            sourceName = "Favicon"
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
                    if (isReachableAndValidImage(item.imageUrl)) item else null
                }
            }.awaitAll().filterNotNull()
        }

        validatedResults
    }

    /**
     * Quickly checks if an image URL is reachable, returns HTTP 200, and is valid image/svg content.
     */
    private suspend fun isReachableAndValidImage(url: String): Boolean = withContext(Dispatchers.IO) {
        if (url.isBlank() || !url.startsWith("http")) return@withContext false
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext false
                val contentType = response.header("Content-Type")?.lowercase() ?: ""
                val contentLength = response.body?.contentLength() ?: -1L
                if (contentLength == 0L) return@withContext false

                val isImageOrSvg = contentType.startsWith("image/") ||
                        contentType.contains("svg") ||
                        contentType.contains("xml") ||
                        contentType.contains("octet-stream") ||
                        url.endsWith(".svg") ||
                        url.endsWith(".png") ||
                        url.endsWith(".webp") ||
                        url.endsWith(".jpg") ||
                        url.endsWith(".jpeg") ||
                        url.endsWith(".ico") ||
                        url.contains("favicon")

                if (!isImageOrSvg) return@withContext false
                if (contentType.startsWith("text/html")) return@withContext false

                true
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Downloads an online image by URL (supports vector SVG, PNG, WebP, JPG)
     * and persists it locally into the app's custom icons directory.
     * Returns the persistent custom icon key (e.g. "custom_icon_172...").
     */
    suspend fun downloadAndSaveIcon(context: Context, imageUrl: String): String? = withContext(Dispatchers.IO) {
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
                    .size(256, 256)
                    .allowHardware(false)
                    .build()

                val result = loader.execute(request)
                if (result is SuccessResult) {
                    val drawable = result.drawable
                    val bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    drawable.setBounds(0, 0, 256, 256)
                    drawable.draw(canvas)

                    val outStream = FileOutputStream(destFile)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 95, outStream)
                    outStream.flush()
                    outStream.close()
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

            val size = 256
            val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, size, size, true)

            val outStream = FileOutputStream(destFile)
            scaledBitmap.compress(Bitmap.CompressFormat.PNG, 95, outStream)
            outStream.flush()
            outStream.close()

            iconKey
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
