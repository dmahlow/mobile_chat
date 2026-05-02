package dev.chungjungsoo.gptmobile.presentation.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.chungjungsoo.gptmobile.data.model.SharedContentHolder
import dev.chungjungsoo.gptmobile.presentation.common.LocalDynamicTheme
import dev.chungjungsoo.gptmobile.presentation.common.LocalThemeMode
import dev.chungjungsoo.gptmobile.presentation.common.Route
import dev.chungjungsoo.gptmobile.presentation.common.SetupNavGraph
import dev.chungjungsoo.gptmobile.presentation.common.ThemeSettingProvider
import dev.chungjungsoo.gptmobile.presentation.theme.GPTMobileTheme
import java.io.File
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                !mainViewModel.isReady.value
            }
        }
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        handleShareIntent(intent)

        window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        setContent {
            val navController = rememberNavController()
            navController.checkForExistingSettings()

            ThemeSettingProvider {
                GPTMobileTheme(
                    dynamicTheme = LocalDynamicTheme.current,
                    themeMode = LocalThemeMode.current
                ) {
                    SetupNavGraph(navController)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleShareIntent(intent)
    }

    private fun handleShareIntent(intent: Intent?) {
        if (intent == null) return
        when (intent.action) {
            Intent.ACTION_SEND -> {
                val mimeType = intent.type ?: return
                when {
                    mimeType.startsWith("text/") -> {
                        val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                            ?: intent.getStringExtra(Intent.EXTRA_SUBJECT)
                        if (!text.isNullOrBlank()) {
                            SharedContentHolder.pendingText = text
                        }
                    }
                    mimeType.startsWith("image/") || mimeType == "application/pdf" -> {
                        val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                        uri?.let { copySharedFileToLocal(it) }
                    }
                }
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                val uris = intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
                uris?.forEach { copySharedFileToLocal(it) }
            }
        }
    }

    private fun copySharedFileToLocal(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri) ?: return
            val fileName = "shared_${System.currentTimeMillis()}_${uri.lastPathSegment?.takeLast(20) ?: "file"}"
            val attachmentsDir = File(filesDir, "attachments")
            attachmentsDir.mkdirs()
            val targetFile = File(attachmentsDir, fileName)
            inputStream.use { input ->
                targetFile.outputStream().use { output -> input.copyTo(output) }
            }
            SharedContentHolder.pendingImagePaths = SharedContentHolder.pendingImagePaths + targetFile.absolutePath
        } catch (_: Exception) {
        }
    }

    private fun NavHostController.checkForExistingSettings() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                mainViewModel.event.collect { event ->
                    when (event) {
                        MainViewModel.SplashEvent.OpenIntro -> {
                            navigate(Route.GET_STARTED) {
                                popUpTo(Route.CHAT_LIST) { inclusive = true }
                            }
                        }

                        MainViewModel.SplashEvent.OpenMigrate -> {
                            navigate(Route.MIGRATE_V2) {
                                popUpTo(Route.CHAT_LIST) { inclusive = true }
                            }
                        }

                        else -> {}
                    }
                }
            }
        }
    }
}
