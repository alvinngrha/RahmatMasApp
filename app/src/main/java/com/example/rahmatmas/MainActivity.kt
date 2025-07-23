package com.example.rahmatmas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.rahmatmas.ui.RahmatMasApp
import com.example.rahmatmas.ui.theme.RahmatMasTheme

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

