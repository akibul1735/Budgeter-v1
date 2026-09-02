package com.example.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object IconHelper {

    fun getIconByName(iconName: String?): ImageVector {
        return when (iconName) {
            "AccountBalance" -> Icons.Default.AccountBalance
            "Savings" -> Icons.Default.Savings
            "Wallet", "AccountBalanceWallet" -> Icons.Default.AccountBalanceWallet
            "CreditCard" -> Icons.Default.CreditCard
            "TrendingUp" -> Icons.AutoMirrored.Filled.TrendingUp
            "ReceiptLong" -> Icons.AutoMirrored.Filled.ReceiptLong
            "ShoppingCart" -> Icons.Default.ShoppingCart
            "Restaurant" -> Icons.Default.Restaurant
            "Home" -> Icons.Default.Home
            "Bolt" -> Icons.Default.Bolt
            "DirectionsCar" -> Icons.Default.DirectionsCar
            "Movie" -> Icons.Default.Movie
            "FitnessCenter" -> Icons.Default.FitnessCenter
            "Checkroom" -> Icons.Default.Checkroom
            "Payments" -> Icons.Default.Payments
            "Work" -> Icons.Default.Work
            "ShowChart" -> Icons.AutoMirrored.Filled.ShowChart
            "Shield" -> Icons.Default.Shield
            "Flight" -> Icons.Default.Flight
            "SwapHoriz" -> Icons.Default.SwapHoriz
            "LocalCafe" -> Icons.Default.LocalCafe
            "LocalGasStation" -> Icons.Default.LocalGasStation
            "School" -> Icons.Default.School
            "MedicalServices" -> Icons.Default.MedicalServices
            "Pets" -> Icons.Default.Pets
            "CardGiftcard" -> Icons.Default.CardGiftcard
            "AttachMoney" -> Icons.Default.AttachMoney
            "Euro" -> Icons.Default.Euro
            "Star" -> Icons.Default.Star
            else -> Icons.Default.Category
        }
    }

    val availableIcons = listOf(
        "ShoppingCart", "Restaurant", "Home", "Bolt", "DirectionsCar",
        "Movie", "FitnessCenter", "Checkroom", "Payments", "Work",
        "ShowChart", "Shield", "Flight", "LocalCafe", "LocalGasStation",
        "School", "MedicalServices", "Pets", "CardGiftcard", "Savings",
        "AccountBalance", "CreditCard", "AccountBalanceWallet"
    )

    val availableColors = listOf(
        "#1A73E8", "#10B981", "#EF4444", "#F59E0B", "#8B5CF6",
        "#06B6D4", "#EC4899", "#F97316", "#14B8A6", "#6366F1",
        "#84CC16", "#64748B"
    )

    fun parseColor(hex: String, defaultColor: Color = Color(0xFF1A73E8)): Color {
        return try {
            val cleanHex = if (hex.startsWith("#")) hex.substring(1) else hex
            if (cleanHex.length == 6) {
                Color(android.graphics.Color.parseColor("#$cleanHex"))
            } else if (cleanHex.length == 8) {
                Color(android.graphics.Color.parseColor("#$cleanHex"))
            } else {
                defaultColor
            }
        } catch (e: Exception) {
            defaultColor
        }
    }
}
