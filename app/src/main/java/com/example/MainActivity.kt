package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.MainAppContainer
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.BudgetViewModel
import com.example.util.DropboxAuthBridge
import com.example.util.ShortcutHelper

class MainActivity : FragmentActivity() {

    private var currentWidgetAction by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleAuthRedirect(intent)
        ShortcutHelper.setupShortcuts(this)
        currentWidgetAction = intent?.action

        setContent {
            val viewModel: BudgetViewModel = viewModel()
            val themeConfig by viewModel.themeConfig.collectAsStateWithLifecycle()

            MyApplicationTheme(themeConfig = themeConfig) {
                MainAppContainer(
                    viewModel = viewModel,
                    widgetAction = currentWidgetAction,
                    onWidgetActionHandled = { currentWidgetAction = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAuthRedirect(intent)
        currentWidgetAction = intent.action
    }

    private fun handleAuthRedirect(intent: Intent?) {
        val data: Uri = intent?.data ?: return
        if (data.scheme == "budgeter" && data.host == "dropbox-auth") {
            val code = data.getQueryParameter("code")
            val error = data.getQueryParameter("error")
            val errorDesc = data.getQueryParameter("error_description")
            if (!code.isNullOrBlank()) {
                DropboxAuthBridge.onAuthCodeReceived(code)
            } else if (!error.isNullOrBlank()) {
                DropboxAuthBridge.onAuthErrorReceived(errorDesc ?: error)
            }
        }
    }
}

