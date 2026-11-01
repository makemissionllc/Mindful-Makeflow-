package com.makemission.makeflow

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.makemission.makeflow.data.local.UserPreferencesDataStore
import com.makemission.makeflow.data.local.userPrefDataStore
import com.makemission.makeflow.core.notifications.MpesaAutoImportNotification
import com.makemission.makeflow.core.update.GithubAppUpdateInstaller
import com.makemission.makeflow.domain.MpesaMessageParser
import com.makemission.makeflow.ui.navigation.AppNavigationEngine
import com.makemission.makeflow.ui.theme.MakeFlowTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {

    companion object {
        const val ACTION_INSTALL_APP_UPDATE = "com.makemission.makeflow.action.INSTALL_APP_UPDATE"
        const val EXTRA_UPDATE_FILE_NAME = "app_update_file_name"
    }

    var sharedText by mutableStateOf<String?>(null)
        private set

    private var pendingMpesaReview by mutableStateOf<MpesaMessageParser.MpesaParsedMessage?>(null)
    private var pendingSavedTransactionId by mutableStateOf<String?>(null)
    private var pendingUpdateFileName: String? = null
    private val installPermissionSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val fileName = pendingUpdateFileName
        pendingUpdateFileName = null
        if (fileName != null) {
            if (GithubAppUpdateInstaller.hasInstallPermission(this)) {
                if (!GithubAppUpdateInstaller.launchInstaller(this, fileName)) {
                    Toast.makeText(this, R.string.update_file_unavailable, Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, R.string.update_install_permission_denied, Toast.LENGTH_LONG).show()
            }
        }
    }

    private var splashReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition { !splashReady }
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            UserPreferencesDataStore(this@MainActivity.userPrefDataStore).isProfiled.first()
            splashReady = true
        }

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT
            )
        )
        handleShareIntent(intent)
        setContent {
            MakeFlowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigationEngine(
                        sharedText = sharedText,
                        onSharedTextConsumed = { sharedText = null },
                        importedMpesaMessage = pendingMpesaReview,
                        onImportedMpesaMessageConsumed = { pendingMpesaReview = null },
                        transactionIdToCategorize = pendingSavedTransactionId,
                        onTransactionToCategorizeConsumed = { pendingSavedTransactionId = null }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleShareIntent(intent)
    }

    private fun handleShareIntent(intent: Intent?) {
        when {
            intent?.action == MpesaAutoImportNotification.ACTION_CATEGORIZE_SAVED -> {
                pendingMpesaReview = null
                pendingSavedTransactionId = MpesaAutoImportNotification.readSavedTransactionId(intent)
            }
            intent?.action == MpesaAutoImportNotification.ACTION_REVIEW -> {
                pendingSavedTransactionId = null
                pendingMpesaReview = MpesaAutoImportNotification.readFromIntent(intent)
            }
            intent?.action == ACTION_INSTALL_APP_UPDATE -> {
                handleAppUpdateIntent(intent)
            }
            intent?.action == Intent.ACTION_SEND && intent.type == "text/plain" -> {
                sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            }
        }
    }

    private fun handleAppUpdateIntent(intent: Intent) {
        val fileName = intent.getStringExtra(EXTRA_UPDATE_FILE_NAME) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !GithubAppUpdateInstaller.hasInstallPermission(this)
        ) {
            pendingUpdateFileName = fileName
            try {
                installPermissionSettingsLauncher.launch(
                    Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                        .setData(Uri.parse("package:$packageName"))
                )
            } catch (_: Exception) {
                pendingUpdateFileName = null
                Toast.makeText(this, R.string.update_installer_unavailable, Toast.LENGTH_LONG).show()
            }
            Toast.makeText(this, R.string.update_install_permission, Toast.LENGTH_LONG).show()
        } else if (!GithubAppUpdateInstaller.launchInstaller(this, fileName)) {
            Toast.makeText(this, R.string.update_file_unavailable, Toast.LENGTH_LONG).show()
        }
    }
}
