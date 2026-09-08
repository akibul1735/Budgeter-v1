package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.MainAppContainer
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.BudgetViewModel
import com.example.util.DropboxAuthBridge

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleAuthRedirect(intent)
        setContent {
            val viewModel: BudgetViewModel = viewModel()
            val themeConfig by viewModel.themeConfig.collectAsStateWithLifecycle()

            MyApplicationTheme(themeConfig = themeConfig) {
                MainAppContainer(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAuthRedirect(intent)
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

