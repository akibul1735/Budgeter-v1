package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.MainActivity
import com.example.R
import com.example.widget.WidgetUpdateHelper

object ShortcutHelper {

    fun setupShortcuts(context: Context) {
        try {
            val txIntent = Intent(context, MainActivity::class.java).apply {
                action = WidgetUpdateHelper.ACTION_ADD_TRANSACTION
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val txShortcut = ShortcutInfoCompat.Builder(context, "shortcut_add_tx")
                .setShortLabel(context.getString(R.string.shortcut_add_transaction_short))
                .setLongLabel(context.getString(R.string.shortcut_add_transaction_long))
                .setIcon(IconCompat.createWithResource(context, R.drawable.ic_widget_receipt))
                .setIntent(txIntent)
                .build()

            val wishIntent = Intent(context, MainActivity::class.java).apply {
                action = WidgetUpdateHelper.ACTION_ADD_WISHLIST
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val wishShortcut = ShortcutInfoCompat.Builder(context, "shortcut_add_wishlist")
                .setShortLabel(context.getString(R.string.shortcut_add_wishlist_short))
                .setLongLabel(context.getString(R.string.shortcut_add_wishlist_long))
                .setIcon(IconCompat.createWithResource(context, R.drawable.ic_widget_wishlist))
                .setIntent(wishIntent)
                .build()

            ShortcutManagerCompat.setDynamicShortcuts(context, listOf(txShortcut, wishShortcut))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
