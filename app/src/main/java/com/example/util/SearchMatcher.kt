package com.example.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * High-performance search matcher with multi-word AND-logic, typo tolerance (fuzzy),
 * Bangla/English bilingual support, and field-weighted relevance scoring.
 */
object SearchMatcher {

    enum class FieldWeight(val weight: Double) {
        PRIMARY(3.0),   // Title, Name, Payee
        KEYWORD(2.0),   // Keywords, Aliases
        SECONDARY(1.0)  // Subtitle, Section, Category, Account, Note, Labels, Details
    }

    data class SearchField(
        val rawText: String,
        val weight: Double = FieldWeight.SECONDARY.weight,
        val isNormalized: Boolean = false,
        val preNormalizedText: String? = null
    ) {
        val normalized: String by lazy {
            preNormalizedText ?: normalize(rawText)
        }

        val words: List<String> by lazy {
            splitWords(normalized)
        }
    }

    data class MatchResult<T>(
        val item: T,
        val score: Double,
        val dateEpochMs: Long = 0L,
        val secondarySortKey: String = ""
    )

    /**
     * Normalizes text for consistent search comparison:
     * 1. NFC normalization
     * 2. Lowercase (Locale.ROOT)
     * 3. Remove zero-width characters (U+200C, U+200D)
     * 4. Convert Bangla digits (০-৯) to ASCII digits (0-9)
     * 5. Strip thousands commas inside numeric sequences
     * 6. Collapse whitespace and trim
     */
    fun normalize(text: String): String {
        if (text.isEmpty()) return ""
        val nfc = Normalizer.normalize(text, Normalizer.Form.NFC)
        val lower = nfc.lowercase(Locale.ROOT)
        val noZeroWidth = lower.replace("\u200C", "").replace("\u200D", "")
        val asciiDigits = convertBanglaDigitsToAscii(noZeroWidth)
        val strippedCommas = stripCommasInNumbers(asciiDigits)
        return strippedCommas.replace(Regex("\\s+"), " ").trim()
    }

    fun convertBanglaDigitsToAscii(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            when (ch) {
                '০' -> sb.append('0')
                '১' -> sb.append('1')
                '২' -> sb.append('2')
                '৩' -> sb.append('3')
                '৪' -> sb.append('4')
                '৫' -> sb.append('5')
                '৬' -> sb.append('6')
                '৭' -> sb.append('7')
                '৮' -> sb.append('8')
                '৯' -> sb.append('9')
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun convertAsciiDigitsToBangla(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            when (ch) {
                '0' -> sb.append('০')
                '1' -> sb.append('১')
                '2' -> sb.append('২')
                '3' -> sb.append('৩')
                '4' -> sb.append('৪')
                '5' -> sb.append('৫')
                '6' -> sb.append('৬')
                '7' -> sb.append('৭')
                '8' -> sb.append('৮')
                '9' -> sb.append('৯')
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    private fun stripCommasInNumbers(input: String): String {
        return input.replace(Regex("(?<=\\d),(?=\\d)"), "")
    }

    fun splitWords(text: String): List<String> {
        if (text.isEmpty()) return emptyList()
        return text.split(Regex("[\\s,;:/|•()\\-_+~<>]+")).filter { it.isNotEmpty() }
    }

    /**
     * Splits query into normalized search tokens.
     */
    fun tokenize(query: String): List<String> {
        val normalizedQuery = normalize(query)
        if (normalizedQuery.isEmpty()) return emptyList()
        return normalizedQuery.split(" ").filter { it.isNotEmpty() }
    }

    /**
     * Scores a single query token against a pre-normalized field.
     * Rules:
     * - Exact field match = 100
     * - Field startsWith = 80
     * - Any word in field startsWith = 60
     * - Field contains = 40
     * - Fuzzy match (Levenshtein distance <= 1 for 4-6 chars, <= 2 for 7+ chars) = 15
     */
    fun scoreTokenAgainstField(token: String, field: SearchField): Double {
        val fNorm = field.normalized
        if (fNorm.isEmpty() || token.isEmpty()) return 0.0

        if (fNorm == token) return 100.0
        if (fNorm.startsWith(token)) return 80.0

        val words = field.words
        if (words.any { it.startsWith(token) }) return 60.0
        if (fNorm.contains(token)) return 40.0

        // Fuzzy match: skip if token is under 4 characters or is purely numeric
        val isNumeric = token.all { it.isDigit() || it == '.' }
        if (!isNumeric && token.length >= 4) {
            val maxDistance = if (token.length in 4..6) 1 else 2
            for (word in words) {
                if (word.length >= 3) {
                    val dist = levenshteinDistance(token, word)
                    if (dist <= maxDistance) {
                        return 15.0
                    }
                    if (word.length > token.length) {
                        val prefixDist = levenshteinDistance(token, word.take(token.length))
                        if (prefixDist <= maxDistance) {
                            return 15.0
                        }
                    }
                }
            }
        }

        return 0.0
    }

    /**
     * Computes the relevance score of a list of search fields against query tokens.
     * Returns 0.0 if ANY token fails to match at least one field (AND logic).
     */
    fun computeScore(tokens: List<String>, fields: List<SearchField>): Double {
        if (tokens.isEmpty() || fields.isEmpty()) return 0.0

        var totalScore = 0.0

        for (token in tokens) {
            var bestTokenScore = 0.0
            for (field in fields) {
                val rawScore = scoreTokenAgainstField(token, field)
                if (rawScore > 0) {
                    val weightedScore = rawScore * field.weight
                    if (weightedScore > bestTokenScore) {
                        bestTokenScore = weightedScore
                    }
                }
            }

            if (bestTokenScore == 0.0) {
                return 0.0 // AND condition failed
            }
            totalScore += bestTokenScore
        }

        return totalScore
    }

    /**
     * Standard Levenshtein Distance calculation.
     */
    fun levenshteinDistance(s1: String, s2: String): Int {
        val m = s1.length
        val n = s2.length
        if (m == 0) return n
        if (n == 0) return m

        var prev = IntArray(n + 1) { it }
        var curr = IntArray(n + 1)

        for (i in 1..m) {
            curr[0] = i
            val c1 = s1[i - 1]
            for (j in 1..n) {
                val cost = if (c1 == s2[j - 1]) 0 else 1
                curr[j] = minOf(
                    prev[j] + 1,       // deletion
                    curr[j - 1] + 1,   // insertion
                    prev[j - 1] + cost // substitution
                )
            }
            val temp = prev
            prev = curr
            curr = temp
        }
        return prev[n]
    }

    /**
     * Builds searchable date variations in both English and Bangla.
     */
    fun generateDateKeywords(epochMs: Long): String {
        if (epochMs <= 0) return ""
        val d = Date(epochMs)
        val enFull = SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH).format(d)
        val enShort = SimpleDateFormat("d MMM yyyy", Locale.ENGLISH).format(d)
        val enPattern = SimpleDateFormat("dd-MM-yyyy yyyy-MM-dd dd/MM/yyyy", Locale.ENGLISH).format(d)
        val monthEn = SimpleDateFormat("MMMM MMM", Locale.ENGLISH).format(d)

        val cal = java.util.Calendar.getInstance().apply { time = d }
        val monthIndex = cal.get(java.util.Calendar.MONTH)
        val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
        val year = cal.get(java.util.Calendar.YEAR)

        val banglaMonths = arrayOf(
            "জানুয়ারি জানু", "ফেব্রুয়ারি ফেব", "মার্চ মার", "এপ্রিল এপ্রি",
            "মে", "জুন", "জুলাই জুল", "আগস্ট আগ", "সেপ্টেম্বর সেপ্টে",
            "অক্টোবর অক্টো", "নভেম্বর নভে", "ডিসেম্বর ডিসে"
        )
        val monthBn = if (monthIndex in banglaMonths.indices) banglaMonths[monthIndex] else ""
        val dayBn = convertAsciiDigitsToBangla(day.toString())
        val yearBn = convertAsciiDigitsToBangla(year.toString())

        return "$enFull $enShort $enPattern $monthEn $monthBn $day $dayBn $year $yearBn $day $monthEn $dayBn $monthBn"
    }

    /**
     * Highlights matched search query tokens in text by returning an AnnotatedString.
     */
    fun highlight(
        text: String,
        query: String,
        highlightColor: Color,
        highlightWeight: FontWeight = FontWeight.Bold
    ): AnnotatedString {
        if (text.isEmpty() || query.isBlank()) {
            return AnnotatedString(text)
        }

        val tokens = tokenize(query)
        if (tokens.isEmpty()) return AnnotatedString(text)

        val lowerText = normalize(text)
        val matchedRanges = mutableListOf<IntRange>()

        for (token in tokens) {
            if (token.isEmpty()) continue
            var startIndex = 0
            while (startIndex < lowerText.length) {
                val index = lowerText.indexOf(token, startIndex)
                if (index == -1) break
                val end = (index + token.length).coerceAtMost(text.length)
                matchedRanges.add(index until end)
                startIndex = index + token.length
            }
        }

        if (matchedRanges.isEmpty()) {
            return AnnotatedString(text)
        }

        // Merge overlapping or adjacent ranges
        matchedRanges.sortBy { it.first }
        val mergedRanges = mutableListOf<IntRange>()
        var current = matchedRanges.first()

        for (i in 1 until matchedRanges.size) {
            val next = matchedRanges[i]
            if (next.first <= current.last + 1) {
                current = current.first..maxOf(current.last, next.last)
            } else {
                mergedRanges.add(current)
                current = next
            }
        }
        mergedRanges.add(current)

        return buildAnnotatedString {
            var lastIdx = 0
            for (range in mergedRanges) {
                val safeStart = range.first.coerceIn(0, text.length)
                val safeEnd = (range.last + 1).coerceIn(safeStart, text.length)

                if (safeStart > lastIdx) {
                    append(text.substring(lastIdx, safeStart))
                }
                if (safeEnd > safeStart) {
                    val styledText = text.substring(safeStart, safeEnd)
                    pushStyle(SpanStyle(color = highlightColor, fontWeight = highlightWeight))
                    append(styledText)
                    pop()
                }
                lastIdx = safeEnd
            }
            if (lastIdx < text.length) {
                append(text.substring(lastIdx))
            }
        }
    }
}
