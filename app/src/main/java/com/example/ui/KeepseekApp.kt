package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.components.ConfirmDeleteStep1Dialog
import com.example.ui.components.ConfirmDeleteStep2Dialog
import com.example.ui.components.ConfirmEditDialog
import com.example.ui.components.CredentialEditorDialog
import com.example.ui.screens.GeneratorScreen
import com.example.ui.screens.LockScreen
import com.example.ui.screens.SavedVaultScreen
import com.example.ui.screens.SettingsScreen
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeepseekApp(viewModel: MainViewModel) {
    val isUnlocked by viewModel.isUnlocked.collectAsState()
    val strings = viewModel.getStrings()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) }

    // Auto-lock lifecycle observer
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP || event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkAutoLock()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Toast/Snackbar listener
    val toastMessage by viewModel.toastEvent.collectAsState()
    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(msg)
            }
            viewModel.clearToast()
        }
    }

    if (!isUnlocked) {
        LockScreen(
            viewModel = viewModel,
            strings = strings,
            onUnlocked = {
                // Return to vault
            }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = strings.appName,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    actions = {
                        // Clean, simple Lock Vault button
                        TextButton(
                            onClick = { viewModel.lockVault() },
                            modifier = Modifier.testTag("top_bar_lock_vault_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = strings.btnLockVault,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = strings.btnLockVault,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.testTag("main_bottom_nav_bar")
                ) {
                    // Tab 0: Saved
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            viewModel.recordUserActivity()
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = strings.tabSaved
                            )
                        },
                        label = { Text(strings.tabSaved) },
                        modifier = Modifier.testTag("nav_tab_saved")
                    )

                    // Tab 1: Generator
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            viewModel.recordUserActivity()
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Password,
                                contentDescription = strings.tabGenerator
                            )
                        },
                        label = { Text(strings.tabGenerator) },
                        modifier = Modifier.testTag("nav_tab_generator")
                    )

                    // Tab 2: Settings
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = {
                            selectedTab = 2
                            viewModel.recordUserActivity()
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = strings.tabSettings
                            )
                        },
                        label = { Text(strings.tabSettings) },
                        modifier = Modifier.testTag("nav_tab_settings")
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (selectedTab) {
                    0 -> SavedVaultScreen(
                        viewModel = viewModel,
                        strings = strings,
                        onNavigateToGenerator = { selectedTab = 1 }
                    )
                    1 -> GeneratorScreen(
                        viewModel = viewModel,
                        strings = strings
                    )
                    2 -> SettingsScreen(
                        viewModel = viewModel,
                        strings = strings
                    )
                }
            }
        }

        // --- Dialogs & Confirmation Flows ---

        // Confirm Edit Dialog (Requires 1 confirmation)
        val confirmEditItem by viewModel.confirmEditTarget.collectAsState()
        confirmEditItem?.let { item ->
            ConfirmEditDialog(
                item = item,
                strings = strings,
                onConfirm = { viewModel.confirmEditAccepted() },
                onDismiss = { viewModel.dismissConfirmEdit() }
            )
        }

        // Edit Credential Editor (after 1 confirmation is accepted)
        val editingItem by viewModel.editingEntity.collectAsState()
        editingItem?.let { item ->
            CredentialEditorDialog(
                initialItem = item,
                strings = strings,
                decryptedPasswordProvider = { viewModel.getDecryptedPassword(it) },
                onSave = { name, site, email, pwd, desc ->
                    viewModel.updateCredential(item.id, name, site, email, pwd, desc)
                },
                onDismiss = { viewModel.dismissEditSheet() }
            )
        }

        // Confirm Delete Step 1 (1st of 2 confirmations)
        val deleteStep1Item by viewModel.confirmDeleteStep1Target.collectAsState()
        deleteStep1Item?.let { item ->
            ConfirmDeleteStep1Dialog(
                item = item,
                strings = strings,
                onConfirm = { viewModel.confirmDeleteStep1Accepted() },
                onDismiss = { viewModel.dismissConfirmDeleteStep1() }
            )
        }

        // Confirm Delete Step 2 (2nd of 2 confirmations)
        val deleteStep2Item by viewModel.confirmDeleteStep2Target.collectAsState()
        deleteStep2Item?.let { item ->
            ConfirmDeleteStep2Dialog(
                item = item,
                strings = strings,
                onConfirmFinal = { viewModel.confirmDeleteStep2Accepted() },
                onDismiss = { viewModel.dismissConfirmDeleteStep2() }
            )
        }

        // Add New Credential Dialog
        val isAddOpen by viewModel.isAddDialogOpen.collectAsState()
        if (isAddOpen) {
            CredentialEditorDialog(
                strings = strings,
                onSave = { name, site, email, pwd, desc ->
                    viewModel.saveCredential(name, site, email, pwd, desc)
                },
                onDismiss = { viewModel.closeAddDialog() }
            )
        }

        // Save Generated Password Dialog (prepopulated from generator)
        val isSaveGenOpen by viewModel.isSaveGeneratedDialogOpen.collectAsState()
        val genResult by viewModel.generatedResult.collectAsState()
        if (isSaveGenOpen) {
            CredentialEditorDialog(
                prefilledPassword = genResult.password,
                strings = strings,
                onSave = { name, site, email, pwd, desc ->
                    viewModel.saveCredential(name, site, email, pwd, desc)
                },
                onDismiss = { viewModel.closeSaveGeneratedDialog() }
            )
        }
    }
}
