package com.example.rahmatmas.ui.splash

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lottiefiles.dotlottie.core.compose.runtime.DotLottieController
import com.lottiefiles.dotlottie.core.compose.runtime.DotLottiePlayerState
import com.lottiefiles.dotlottie.core.compose.ui.DotLottieAnimation
import com.lottiefiles.dotlottie.core.util.DotLottieSource
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    checkAdminSession: suspend () -> Boolean,
    checkCustomerSession: suspend () -> Boolean,
    onNavigate: (String) -> Unit,
) {
    val controller = remember { DotLottieController() }
    val playerState by controller.currentState.collectAsState(initial = DotLottiePlayerState.INITIAL)
    var targetRoute by remember { mutableStateOf<String?>(null) }
    var hasNavigated by remember { mutableStateOf(false) }
    val latestNavigate by rememberUpdatedState(newValue = onNavigate)

    LaunchedEffect(Unit) {
        val adminSessionValid = checkAdminSession()
        if (adminSessionValid) {
            targetRoute = "homeadmin"
            return@LaunchedEffect
        }

        val customerLoggedIn = checkCustomerSession()
        targetRoute = if (customerLoggedIn) {
            "homecostumer"
        } else {
            "logincostumer"
        }
    }

    LaunchedEffect(playerState, targetRoute) {
        if (!hasNavigated && targetRoute != null) {
            val finished = playerState == DotLottiePlayerState.COMPLETED
            val failed = playerState == DotLottiePlayerState.ERROR
            if (finished || failed) {
                hasNavigated = true
                latestNavigate(targetRoute!!)
            }
        }
    }

    LaunchedEffect(targetRoute) {
        if (targetRoute != null) {
            delay(3000)
            if (!hasNavigated) {
                hasNavigated = true
                latestNavigate(targetRoute!!)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        DotLottieAnimation(
            modifier = Modifier.size(220.dp),
            source = DotLottieSource.Asset("PiggyBank.lottie"),
            autoplay = true,
            loop = false,
            controller = controller
        )

        if (playerState == DotLottiePlayerState.ERROR || playerState == DotLottiePlayerState.INITIAL) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}
