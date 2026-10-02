package com.example

import com.example.util.IconHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IconInitialsTest {

    @Test
    fun testUserSpecifiedInitialsGeneration() {
        // Multi-word with honorific/title
        assertEquals("AS", IconHelper.generateInitials("Ashique Sir"))

        // Single word with French/Latin/consonant onset
        assertEquals("AQ", IconHelper.generateInitials("Ashique"))

        // Multi-word with ignored conjunction "and"
        assertEquals("DT", IconHelper.generateInitials("Digital and Tech"))

        // 3 words: First + Last
        assertEquals("JS", IconHelper.generateInitials("John Michael Smith"))

        // Single word with VCCV syllable break
        assertEquals("PN", IconHelper.generateInitials("Purny"))
    }

    @Test
    fun testIgnoredWordsFiltering() {
        assertEquals("DT", IconHelper.generateInitials("Digital and Tech"))
        assertEquals("DT", IconHelper.generateInitials("Digital & Tech"))
        assertEquals("AH", IconHelper.generateInitials("Apex Hardware Ltd"))
        assertEquals("AS", IconHelper.generateInitials("The Ashique Sir"))
    }

    @Test
    fun testSingleWordSyllableOnsetRules() {
        assertEquals("MT", IconHelper.generateInitials("Martin"))
        assertEquals("JD", IconHelper.generateInitials("Jordan"))
        assertEquals("DV", IconHelper.generateInitials("David"))
        assertEquals("KR", IconHelper.generateInitials("Kareem"))
        assertEquals("SL", IconHelper.generateInitials("Salim"))
        assertEquals("TQ", IconHelper.generateInitials("Tarique"))
        assertEquals("SQ", IconHelper.generateInitials("Shafique"))
    }

    @Test
    fun testResolveBestIconOrInitialsNeverLeavesEmpty() {
        // 1. In-app brand or keyword match takes precedence
        assertEquals("BankBkash", IconHelper.resolveBestIconOrInitials("bKash Account"))
        assertEquals("BankCity", IconHelper.resolveBestIconOrInitials("City Bank Savings"))
        assertEquals("LocalCafe", IconHelper.resolveBestIconOrInitials("Coffee & Tea"))

        // 2. When unavailable, generate initials with INITIALS: prefix
        assertEquals("INITIALS:AS", IconHelper.resolveBestIconOrInitials("Ashique Sir"))
        assertEquals("INITIALS:AQ", IconHelper.resolveBestIconOrInitials("Ashique"))
        assertEquals("INITIALS:DT", IconHelper.resolveBestIconOrInitials("Digital and Tech"))
        assertEquals("INITIALS:JS", IconHelper.resolveBestIconOrInitials("John Michael Smith"))
        assertEquals("INITIALS:PN", IconHelper.resolveBestIconOrInitials("Purny"))

        // 3. Null or blank never fails
        val emptyResult = IconHelper.resolveBestIconOrInitials("")
        assertTrue(emptyResult.startsWith("INITIALS:"))
        assertTrue(IconHelper.isInitialsIcon(emptyResult))
    }

    @Test
    fun testInitialsIconHelpers() {
        assertTrue(IconHelper.isInitialsIcon("INITIALS:AS"))
        assertTrue(IconHelper.isInitialsIcon("INITIALS:AQ"))
        assertFalse(IconHelper.isInitialsIcon("Wallet"))
        assertFalse(IconHelper.isInitialsIcon(null))

        assertEquals("AS", IconHelper.getInitialsFromIconName("INITIALS:AS"))
        assertEquals("AQ", IconHelper.getInitialsFromIconName("INITIALS:AQ"))
        assertEquals("DT", IconHelper.getInitialsFromIconName("INITIALS:DT"))
    }
}
