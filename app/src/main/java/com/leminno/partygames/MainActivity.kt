package com.leminno.partygames

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.leminno.partygames.ui.navigation.PartyGamesAppNavHost
import com.leminno.partygames.ui.theme.PartyGamesTheme

class MainActivity : ComponentActivity() {

    private var initialRoomCode by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleDeepLink(intent)

        setContent {
            PartyGamesTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PartyGamesAppNavHost(initialRoomCode = initialRoomCode)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val uri: Uri? = intent?.data
        if (uri != null) {
            // Check partygames://join/AZ789K or https://partygames.leminno.com/join/AZ789K
            val code = when {
                uri.scheme == "partygames" && uri.host == "join" -> uri.lastPathSegment
                uri.host == "partygames.leminno.com" && uri.pathSegments.firstOrNull() == "join" -> uri.lastPathSegment
                else -> null
            }
            if (!code.isNullOrBlank()) {
                initialRoomCode = code.uppercase()
            }
        }
    }
}
