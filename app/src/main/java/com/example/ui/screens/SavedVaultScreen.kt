package com.example.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VaultEntity
import com.example.i18n.TranslationStrings
import com.example.viewmodel.MainViewModel

@Composable
fun SavedVaultScreen(
    viewModel: MainViewModel,
    strings: TranslationStrings,
    onNavigateToGenerator: () -> Unit
) {
    val items by viewModel.filteredVaultItems.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val revealedIds by viewModel.revealedPasswordIds.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Clean Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text(strings.searchPlaceholder) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_vault_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = strings.noSavedPasswords,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = strings.noSavedPasswordsHint,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = onNavigateToGenerator,
                            modifier = Modifier.testTag("btn_empty_state_go_generator")
                        ) {
                            Text(strings.tabGenerator)
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("saved_passwords_list")
                ) {
                    items(items, key = { it.id }) { item ->
                        val isRevealed = revealedIds.contains(item.id)
                        val decrypted = if (isRevealed) viewModel.getDecryptedPassword(item) else ""

                        SimpleVaultCard(
                            item = item,
                            isRevealed = isRevealed,
                            decryptedPassword = decrypted,
                            strings = strings,
                            onToggleReveal = {
                                viewModel.togglePasswordVisibility(item.id)
                            },
                            onCopyPassword = {
                                val pwd = viewModel.getDecryptedPassword(item)
                                clipboardManager.setText(AnnotatedString(pwd))
                                viewModel.showToast(strings.copiedPasswordToast)
                            },
                            onRequestEdit = {
                                viewModel.requestEdit(item)
                            },
                            onRequestDelete = {
                                viewModel.requestDelete(item)
                            }
                        )
                    }
                }
            }
        }

        // Clean Floating Action Button
        FloatingActionButton(
            onClick = { viewModel.openAddDialog() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp)
                .testTag("add_credential_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = strings.dialogAddTitle)
        }
    }
}

@Composable
fun SimpleVaultCard(
    item: VaultEntity,
    isRevealed: Boolean,
    decryptedPassword: String,
    strings: TranslationStrings,
    onToggleReveal: () -> Unit,
    onCopyPassword: () -> Unit,
    onRequestEdit: () -> Unit,
    onRequestDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .testTag("vault_card_${item.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Name
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Email / Username
            if (item.email.isNotBlank()) {
                Text(
                    text = item.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Website
            if (item.website.isNotBlank()) {
                Text(
                    text = item.website,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Password Bar (Masked / Revealed)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleReveal() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (isRevealed) decryptedPassword else "••••••••••••",
                        fontFamily = if (isRevealed) FontFamily.Monospace else FontFamily.Default,
                        fontWeight = FontWeight.Medium,
                        fontSize = if (isRevealed) 15.sp else 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("password_text_${item.id}")
                    )

                    // Show / Hide Password button
                    TextButton(
                        onClick = onToggleReveal,
                        modifier = Modifier.testTag("toggle_show_hide_${item.id}")
                    ) {
                        Icon(
                            imageVector = if (isRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isRevealed) strings.btnHidePassword else strings.btnShowPassword,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Copy, Edit, Delete
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Copy (no confirmation)
                OutlinedButton(
                    onClick = onCopyPassword,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("copy_password_btn_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = strings.btnCopy,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.btnCopy)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Edit (1 confirmation)
                OutlinedButton(
                    onClick = onRequestEdit,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("edit_credential_btn_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = strings.btnEdit,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.btnEdit)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Delete (2 confirmations)
                OutlinedButton(
                    onClick = onRequestDelete,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("delete_credential_btn_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = strings.btnDelete,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.btnDelete)
                }
            }
        }
    }
}
