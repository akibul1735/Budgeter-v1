package com.example

import com.example.util.SearchMatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchMatcherTest {

    @Test
    fun testNormalize_BanglaDigitsAndThousandSeparators() {
        val input = "৳ ১,৫০০.৫০"
        val normalized = SearchMatcher.normalize(input)
        assertTrue(normalized.contains("1500.50"))
    }

    @Test
    fun testNormalize_ZeroWidthJoinerAndNfc() {
        val input = "ব্যা\u200Dংক"
        val normalized = SearchMatcher.normalize(input)
        assertEquals("ব্যাংক", normalized)
    }

    @Test
    fun testEmptyQuery() {
        val tokens = SearchMatcher.tokenize("")
        assertTrue(tokens.isEmpty())
        val score = SearchMatcher.computeScore(
            tokens,
            listOf(SearchMatcher.SearchField("Groceries", SearchMatcher.FieldWeight.PRIMARY.weight))
        )
        assertEquals(0.0, score, 0.001)
    }

    @Test
    fun testPrefixMatch() {
        val tokens = SearchMatcher.tokenize("groc")
        val fields = listOf(
            SearchMatcher.SearchField("Groceries Store", SearchMatcher.FieldWeight.PRIMARY.weight)
        )
        val score = SearchMatcher.computeScore(tokens, fields)
        assertTrue("Prefix match score should be greater than 0", score > 0.0)
    }

    @Test
    fun testPartialMatch() {
        val tokens = SearchMatcher.tokenize("store")
        val fields = listOf(
            SearchMatcher.SearchField("Super Store Bangladesh", SearchMatcher.FieldWeight.PRIMARY.weight)
        )
        val score = SearchMatcher.computeScore(tokens, fields)
        assertTrue("Partial word match should score > 0", score > 0.0)
    }

    @Test
    fun testMultiWordOutOfOrder() {
        val tokens = SearchMatcher.tokenize("bKash groceries 1500")
        val fields = listOf(
            SearchMatcher.SearchField("Daily Groceries", SearchMatcher.FieldWeight.PRIMARY.weight),
            SearchMatcher.SearchField("Paid via bKash personal", SearchMatcher.FieldWeight.SECONDARY.weight),
            SearchMatcher.SearchField("1500 BDT", SearchMatcher.FieldWeight.SECONDARY.weight)
        )
        val score = SearchMatcher.computeScore(tokens, fields)
        assertTrue("Multi-word out of order match across multiple fields should succeed", score > 0.0)
    }

    @Test
    fun testTypoTolerance_OneLetterDistance() {
        // "resturant" vs "restaurant" (1 substitution/deletion)
        val tokens = SearchMatcher.tokenize("resturant")
        val fields = listOf(
            SearchMatcher.SearchField("Fine Restaurant", SearchMatcher.FieldWeight.PRIMARY.weight)
        )
        val score = SearchMatcher.computeScore(tokens, fields)
        assertTrue("Typo tolerant search should match single typo in 4+ char word", score > 0.0)
    }

    @Test
    fun testBanglaEnglishMix() {
        val tokens = SearchMatcher.tokenize("বাজার বাজার bkash")
        val fields = listOf(
            SearchMatcher.SearchField("বাজার খরচ (Bazaar)", SearchMatcher.FieldWeight.PRIMARY.weight),
            SearchMatcher.SearchField("bKash payment", SearchMatcher.FieldWeight.SECONDARY.weight)
        )
        val score = SearchMatcher.computeScore(tokens, fields)
        assertTrue("Mixed language query should match", score > 0.0)
    }

    @Test
    fun testBanglaDigitQueryMatchesAsciiData() {
        val tokens = SearchMatcher.tokenize("১৫০০")
        val fields = listOf(
            SearchMatcher.SearchField("Rent 1500", SearchMatcher.FieldWeight.PRIMARY.weight)
        )
        val score = SearchMatcher.computeScore(tokens, fields)
        assertTrue("Bangla digit token should match ASCII number in field", score > 0.0)
    }

    @Test
    fun testRelevanceWeightRanking() {
        val tokens = SearchMatcher.tokenize("bKash")
        val titleMatch = listOf(
            SearchMatcher.SearchField("bKash Wallet", SearchMatcher.FieldWeight.PRIMARY.weight)
        )
        val noteMatch = listOf(
            SearchMatcher.SearchField("Cash Account", SearchMatcher.FieldWeight.PRIMARY.weight),
            SearchMatcher.SearchField("Transfer to bKash", SearchMatcher.FieldWeight.SECONDARY.weight)
        )
        val scoreTitle = SearchMatcher.computeScore(tokens, titleMatch)
        val scoreNote = SearchMatcher.computeScore(tokens, noteMatch)
        assertTrue("Title match with higher weight should score higher than note match", scoreTitle > scoreNote)
    }
}
