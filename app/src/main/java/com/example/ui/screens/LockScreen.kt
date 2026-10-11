package com.example.ui.screens

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.i18n.TranslationStrings
import com.example.viewmodel.MainViewModel

@Composable
fun LockScreen(
    viewModel: MainViewModel,
    strings: TranslationStrings,
    onUnlocked: () -> Unit
) {
    val context = LocalContext.current
    val keyguardManager = remember {
        context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
    }
    val isDeviceSecure = remember {
        keyguardManager?.isDeviceSecure == true
    }

    val deviceCredentialLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.unlockVault()
            onUnlocked()
        }
    }

    fun launchDeviceAuth() {
        if (keyguardManager != null && isDeviceSecure) {
            val intent = keyguardManager.createConfirmDeviceCredentialIntent(
                strings.appName,
                strings.unlockPrompt
            )
            if (intent != null) {
                deviceCredentialLauncher.launch(intent)
            } else {
                viewModel.unlockVault()
                onUnlocked()
            }
        } else {
            // If device doesn't have a passcode, open directly
            viewModel.unlockVault()
            onUnlocked()
        }
    }

    // Immediately ask for device passcode upon launch.
    // If device doesn't have a passcode, open directly.
    LaunchedEffect(Unit) {
        if (!isDeviceSecure) {
            viewModel.unlockVault()
            onUnlocked()
        } else {
            launchDeviceAuth()
        }
    }

    // Clean, simple, uncluttered fallback screen if user dismissed or cancelled prompt
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = strings.vaultLocked,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = strings.unlockPrompt,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { launchDeviceAuth() },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(50.dp)
                    .testTag("unlock_device_credential_button")
            ) {
                Text(
                    text = strings.btnUnlockDevice,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
