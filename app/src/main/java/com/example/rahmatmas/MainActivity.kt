package com.example.rahmatmas

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.rahmatmas.ui.RahmatMasApp
import com.example.rahmatmas.ui.theme.RahmatMasTheme
import com.example.rahmatmas.notifications.NotificationUtils.ensureChannel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Setup window untuk status bar
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Atur status bar
        @Suppress("DEPRECATION")
        window.statusBarColor = Color.Transparent.toArgb()

        // Pastikan ikon status bar terlihat (dark icons untuk background terang)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true // Dark icons untuk background terang
            isAppearanceLightNavigationBars = true // Dark navigation bar icons
        }

        // Create notification channel and request permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ensureChannel(this)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val perm = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                val launcher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> }
                launcher.launch(perm)
            }
        }

        setContent {
            RahmatMasTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    RahmatMasApp(
                        modifier = Modifier
                    )
                }
            }
        }
    }
}
