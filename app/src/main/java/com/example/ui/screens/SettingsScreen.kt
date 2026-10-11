package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crypto.AutoLockTimeout
import com.example.crypto.CipherAlgorithm
import com.example.crypto.PbkdfRounds
import com.example.crypto.SaltSize
import com.example.i18n.AppLanguage
import com.example.i18n.TranslationStrings
import com.example.ui.theme.AppThemeMode
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    strings: TranslationStrings
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val currentTheme by viewModel.currentTheme.collectAsState()
    val encSettings by viewModel.encryptionSettings.collectAsState()

    var algoDropdownOpen by remember { mutableStateOf(false) }
    var pbkdfDropdownOpen by remember { mutableStateOf(false) }
    var saltDropdownOpen by remember { mutableStateOf(false) }
    var autoLockDropdownOpen by remember { mutableStateOf(false) }
    var themeDropdownOpen by remember { mutableStateOf(false) }
    var langDropdownOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Lock Vault Button
        Button(
            onClick = { viewModel.lockVault() },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("settings_lock_vault_button")
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = strings.btnLockVault,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = strings.btnLockVault,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Language
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = strings.settingsLanguageTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                ExposedDropdownMenuBox(
                    expanded = langDropdownOpen,
                    onExpandedChange = { langDropdownOpen = it }
                ) {
                    OutlinedTextField(
                        value = "${currentLang.flag} ${currentLang.displayName}",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = langDropdownOpen) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .testTag("language_selector_input")
                    )

                    ExposedDropdownMenu(
                        expanded = langDropdownOpen,
                        onDismissRequest = { langDropdownOpen = false }
                    ) {
                        AppLanguage.entries.forEach { lang ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(lang.flag, fontSize = 18.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(lang.displayName)
                                        if (lang == currentLang) {
                                            Spacer(modifier = Modifier.weight(1f))
                                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                },
                                onClick = {
                                    viewModel.setLanguage(lang)
                                    langDropdownOpen = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Theme
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = strings.settingsThemeTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                val themeName = when (currentTheme) {
                    AppThemeMode.CYBER_DARK -> strings.themeCyberDark
                    AppThemeMode.OBSIDIAN_MIDNIGHT -> strings.themeObsidianMidnight
                    AppThemeMode.EMERALD_VAULT -> strings.themeEmeraldVault
                    AppThemeMode.CLEAN_LIGHT -> strings.themeCleanLight
                    AppThemeMode.SYSTEM -> strings.themeSystem
                }

                ExposedDropdownMenuBox(
                    expanded = themeDropdownOpen,
                    onExpandedChange = { themeDropdownOpen = it }
                ) {
                    OutlinedTextField(
                        value = themeName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = themeDropdownOpen) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .testTag("theme_selector_input")
                    )

                    ExposedDropdownMenu(
                        expanded = themeDropdownOpen,
                        onDismissRequest = { themeDropdownOpen = false }
                    ) {
                        AppThemeMode.entries.forEach { mode ->
                            val label = when (mode) {
                                AppThemeMode.CYBER_DARK -> strings.themeCyberDark
                                AppThemeMode.OBSIDIAN_MIDNIGHT -> strings.themeObsidianMidnight
                                AppThemeMode.EMERALD_VAULT -> strings.themeEmeraldVault
                                AppThemeMode.CLEAN_LIGHT -> strings.themeCleanLight
                                AppThemeMode.SYSTEM -> strings.themeSystem
                            }
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(label)
                                        if (mode == currentTheme) {
                                            Spacer(modifier = Modifier.weight(1f))
                                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                },
                                onClick = {
                                    viewModel.setTheme(mode)
                                    themeDropdownOpen = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Encryption Settings
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = strings.settingsEncryptionTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Algorithm Mode
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = strings.settingsAlgorithm, style = MaterialTheme.typography.labelMedium)
                    ExposedDropdownMenuBox(
                        expanded = algoDropdownOpen,
                        onExpandedChange = { algoDropdownOpen = it }
                    ) {
                        OutlinedTextField(
                            value = encSettings.algorithm.displayName,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = algoDropdownOpen) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .testTag("algorithm_selector_input")
                        )

                        ExposedDropdownMenu(
                            expanded = algoDropdownOpen,
                            onDismissRequest = { algoDropdownOpen = false }
                        ) {
                            CipherAlgorithm.entries.forEach { algo ->
                                DropdownMenuItem(
                                    text = { Text(algo.displayName) },
                                    onClick = {
                                        viewModel.updateEncryptionSettings(algorithm = algo)
                                        algoDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }
                }

                // PBKDF2 Rounds
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = strings.settingsPbkdfIterations, style = MaterialTheme.typography.labelMedium)
                    ExposedDropdownMenuBox(
                        expanded = pbkdfDropdownOpen,
                        onExpandedChange = { pbkdfDropdownOpen = it }
                    ) {
                        OutlinedTextField(
                            value = encSettings.pbkdfRounds.label,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = pbkdfDropdownOpen) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .testTag("pbkdf_selector_input")
                        )

                        ExposedDropdownMenu(
                            expanded = pbkdfDropdownOpen,
                            onDismissRequest = { pbkdfDropdownOpen = false }
                        ) {
                            PbkdfRounds.entries.forEach { round ->
                                DropdownMenuItem(
                                    text = { Text(round.label) },
                                    onClick = {
                                        viewModel.updateEncryptionSettings(pbkdfRounds = round)
                                        pbkdfDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Salt Size
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = strings.settingsSaltLength, style = MaterialTheme.typography.labelMedium)
                    ExposedDropdownMenuBox(
                        expanded = saltDropdownOpen,
                        onExpandedChange = { saltDropdownOpen = it }
                    ) {
                        OutlinedTextField(
                            value = encSettings.saltSize.label,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = saltDropdownOpen) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .testTag("salt_selector_input")
                        )

                        ExposedDropdownMenu(
                            expanded = saltDropdownOpen,
                            onDismissRequest = { saltDropdownOpen = false }
                        ) {
                            SaltSize.entries.forEach { salt ->
                                DropdownMenuItem(
                                    text = { Text(salt.label) },
                                    onClick = {
                                        viewModel.updateEncryptionSettings(saltSize = salt)
                                        saltDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Auto-Lock Timeout
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = strings.settingsAutoLock, style = MaterialTheme.typography.labelMedium)
                    ExposedDropdownMenuBox(
                        expanded = autoLockDropdownOpen,
                        onExpandedChange = { autoLockDropdownOpen = it }
                    ) {
                        val autoLockLabel = when (encSettings.autoLockTimeout) {
                            AutoLockTimeout.IMMEDIATELY -> strings.autoLockImmediately
                            AutoLockTimeout.ONE_MINUTE -> strings.autoLock1Min
                            AutoLockTimeout.FIVE_MINUTES -> strings.autoLock5Min
                            AutoLockTimeout.FIFTEEN_MINUTES -> strings.autoLock15Min
                            AutoLockTimeout.NEVER -> strings.autoLockNever
                        }
                        OutlinedTextField(
                            value = autoLockLabel,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = autoLockDropdownOpen) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .testTag("autolock_selector_input")
                        )

                        ExposedDropdownMenu(
                            expanded = autoLockDropdownOpen,
                            onDismissRequest = { autoLockDropdownOpen = false }
                        ) {
                            AutoLockTimeout.entries.forEach { timeout ->
                                val label = when (timeout) {
                                    AutoLockTimeout.IMMEDIATELY -> strings.autoLockImmediately
                                    AutoLockTimeout.ONE_MINUTE -> strings.autoLock1Min
                                    AutoLockTimeout.FIVE_MINUTES -> strings.autoLock5Min
                                    AutoLockTimeout.FIFTEEN_MINUTES -> strings.autoLock15Min
                                    AutoLockTimeout.NEVER -> strings.autoLockNever
                                }
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        viewModel.updateEncryptionSettings(autoLockTimeout = timeout)
                                        autoLockDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(72.dp))
    }
}
