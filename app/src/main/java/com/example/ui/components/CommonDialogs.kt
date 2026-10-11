package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.VaultEntity
import com.example.i18n.TranslationStrings

@Composable
fun ConfirmEditDialog(
    item: VaultEntity,
    strings: TranslationStrings,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = strings.confirmEditTitle,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = strings.confirmEditMessage,
                    style = MaterialTheme.typography.bodyMedium
                )
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = item.name,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (item.website.isNotBlank()) {
                            Text(
                                text = item.website,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier.testTag("confirm_edit_button")
            ) {
                Text(strings.btnProceedEdit)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_edit_button")
            ) {
                Text(strings.btnCancel)
            }
        }
    )
}

@Composable
fun ConfirmDeleteStep1Dialog(
    item: VaultEntity,
    strings: TranslationStrings,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = strings.confirmDeleteTitleStep1,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Text(
                text = String.format(strings.confirmDeleteMessageStep1, item.name),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("confirm_delete_step1_button")
            ) {
                Text(strings.btnDelete)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_delete_step1_button")
            ) {
                Text(strings.btnCancel)
            }
        }
    )
}

@Composable
fun ConfirmDeleteStep2Dialog(
    item: VaultEntity,
    strings: TranslationStrings,
    onConfirmFinal: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.DeleteForever,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = strings.confirmDeleteTitleStep2,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = strings.confirmDeleteMessageStep2,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Target: ${item.name}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmFinal,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("confirm_delete_step2_button")
            ) {
                Text(strings.btnDeletePermanently)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_delete_step2_button")
            ) {
                Text(strings.btnCancel)
            }
        }
    )
}

@Composable
fun CredentialEditorDialog(
    initialItem: VaultEntity? = null,
    prefilledPassword: String = "",
    strings: TranslationStrings,
    decryptedPasswordProvider: ((VaultEntity) -> String)? = null,
    onSave: (name: String, website: String, email: String, password: String, desc: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialItem?.name ?: "") }
    var website by remember { mutableStateOf(initialItem?.website ?: "") }
    var email by remember { mutableStateOf(initialItem?.email ?: "") }
    var password by remember {
        mutableStateOf(
            if (initialItem != null && decryptedPasswordProvider != null) {
                decryptedPasswordProvider(initialItem)
            } else {
                prefilledPassword
            }
        )
    }
    var description by remember { mutableStateOf(initialItem?.description ?: "") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isEditing = initialItem != null
    val dialogTitle = if (isEditing) strings.dialogEditTitle else strings.dialogAddTitle

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = dialogTitle,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(strings.fieldName) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_credential_name")
                )

                OutlinedTextField(
                    value = website,
                    onValueChange = { website = it },
                    label = { Text(strings.fieldWebsite) },
                    singleLine = true,
                    placeholder = { Text("https://example.com") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_credential_website")
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(strings.fieldEmail) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_credential_email")
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(strings.fieldPassword) },
                    singleLine = false,
                    maxLines = 3,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(
                            onClick = { passwordVisible = !passwordVisible },
                            modifier = Modifier.testTag("toggle_editor_password_visibility")
                        ) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) strings.btnHidePassword else strings.btnShowPassword
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_credential_password")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(strings.fieldDescription) },
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_credential_description")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(name, website, email, password, description)
                    onDismiss()
                },
                enabled = name.isNotBlank() || password.isNotBlank(),
                modifier = Modifier.testTag("save_credential_button")
            ) {
                Text(strings.btnSave)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_credential_button")
            ) {
                Text(strings.btnCancel)
            }
        }
    )
}
