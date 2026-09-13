package com.natijeh.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.natijeh.data.model.OfficialStreamResolver
import com.natijeh.ui.viewmodel.SportsViewModel

private val allowedStreamHosts = setOf(
    "aparat.com", "www.aparat.com",
    "football360.ir", "www.football360.ir",
    "varzesh3.com", "www.varzesh3.com",
    "telewebion.net", "www.telewebion.net"
)

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficialStreamScreen(matchId: String, providerId: String, viewModel: SportsViewModel, onBack: () -> Unit) {
    val match by viewModel.getMatchFlow(matchId).collectAsStateWithLifecycle(initialValue = null)
    val stream = match?.let(OfficialStreamResolver::forMatch)?.firstOrNull { it.id == providerId }
    val title = stream?.title ?: "پخش رسمی"
    val url = stream?.url ?: "https://www.varzesh3.com/livescore"
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "بازگشت") } }
            )
        }
    ) { padding ->
        AndroidView(
            modifier = Modifier.fillMaxSize().padding(padding),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = true
                    settings.allowFileAccess = false
                    settings.allowContentAccess = false
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                            val host = request.url.host.orEmpty().lowercase()
                            return if (host in allowedStreamHosts) false else true
                        }
                    }
                    loadUrl(url)
                }
            }
        )
    }
}
