package com.example.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.util.IconHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.ByteArrayInputStream
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
     * Searches for logos and icons online using reliable, keyless services:
     * 1. Brandfetch Logos/App Icons
     * 2. DuckDuckGo Instant Answers & Topics
     * 3. Wikimedia Commons Icons / Logos
     * 4. Google Favicon Service (for brand names / domains)
     */
    suspend fun searchIcons(query: String): List<OnlineIconResult> = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) return@withContext emptyList()

        val results = mutableListOf<OnlineIconResult>()
        val seenUrls = mutableSetOf<String>()

        // 1. Search Brandfetch for branded logos and high-res vector app icons
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
                        for (i in 0 until minOf(jsonArr.length(), 6)) {
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
            // Ignore failure from single provider and proceed to others
        }

        // 2. DuckDuckGo Instant Answer / Topics
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
                        val mainImg = obj.optString("Image")
                        if (mainImg.isNotBlank()) {
                            val fullUrl = if (mainImg.startsWith("/")) "https://duckduckgo.com$mainImg" else mainImg
                            if (seenUrls.add(fullUrl)) {
                                results.add(
                                    OnlineIconResult(
                                        title = obj.optString("Heading", cleanQuery),
                                        imageUrl = fullUrl,
                                        sourceName = "Web"
                                    )
                                )
                            }
                        }

                        val relatedTopics = obj.optJSONArray("RelatedTopics")
                        if (relatedTopics != null) {
                            for (i in 0 until minOf(relatedTopics.length(), 5)) {
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
                                                sourceName = "Web"
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

        // 3. Search Wikimedia Commons for icons, vector drawings & cliparts
        try {
            val encoded = URLEncoder.encode("$cleanQuery icon", "UTF-8")
            val wmUrl = "https://commons.wikimedia.org/w/api.php?action=query&generator=search&gsrnamespace=6&gsrsearch=filetype:bitmap|drawing+$encoded&gsrlimit=8&prop=imageinfo&iiprop=url|thumburl&iiurlwidth=160&format=json"
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
                                val page = pages.optJSONObject(key) ?: continue
                                val imageInfoArr = page.optJSONArray("imageinfo") ?: continue
                                val info = imageInfoArr.optJSONObject(0) ?: continue
                                val thumbUrl = info.optString("thumburl").ifBlank { info.optString("url") }
                                if (thumbUrl.isNotBlank() && seenUrls.add(thumbUrl)) {
                                    val rawTitle = page.optString("title", cleanQuery)
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

        // 4. Google Favicon fallback (especially useful if the user types a company or service like bkash, uber, netflix)
        try {
            val noSpaces = cleanQuery.replace(" ", "").lowercase()
            val domain = if (noSpaces.contains(".")) noSpaces else "$noSpaces.com"
            val googleFaviconUrl = "https://t1.gstatic.com/faviconV2?client=SOCIAL&type=FAVICON&fallback_opts=TYPE,SIZE,URL&url=https://$domain&size=128"
            if (seenUrls.add(googleFaviconUrl)) {
                results.add(
                    OnlineIconResult(
                        title = cleanQuery.replaceFirstChar { it.uppercase() },
                        imageUrl = googleFaviconUrl,
                        sourceName = "Google"
                    )
                )
            }
        } catch (_: Exception) {
            // Proceed
        }

        results
    }

    /**
     * Downloads an online image by URL and persists it locally into the app's custom icons directory.
     * Returns the persistent custom icon key (e.g. "custom_icon_172...").
     */
    suspend fun downloadAndSaveIcon(context: Context, imageUrl: String): String? = withContext(Dispatchers.IO) {
        try {
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

            // Scale to max 256x256 while preserving clarity
            val size = 256
            val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, size, size, true)

            val customDir = File(context.filesDir, "custom_icons")
            if (!customDir.exists()) customDir.mkdirs()

            val iconKey = "custom_icon_${System.currentTimeMillis()}"
            val destFile = File(customDir, "$iconKey.png")

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
