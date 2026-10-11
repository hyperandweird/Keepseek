package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator.PasswordStrengthCategory
import com.example.i18n.TranslationStrings
import com.example.viewmodel.MainViewModel

@Composable
fun GeneratorScreen(
    viewModel: MainViewModel,
    strings: TranslationStrings
) {
    val length by viewModel.generatorLength.collectAsState()
    val includeUpper by viewModel.includeUpper.collectAsState()
    val includeLower by viewModel.includeLower.collectAsState()
    val includeNumbers by viewModel.includeNumbers.collectAsState()
    val includeSymbols by viewModel.includeSymbols.collectAsState()
    val generatedResult by viewModel.generatedResult.collectAsState()

    val clipboardManager = LocalClipboardManager.current

    val strengthText = when (generatedResult.strength) {
        PasswordStrengthCategory.WEAK -> strings.strengthWeak
        PasswordStrengthCategory.MODERATE -> strings.strengthModerate
        PasswordStrengthCategory.STRONG -> strings.strengthStrong
        PasswordStrengthCategory.UNBREAKABLE -> strings.strengthUnbreakable
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Password Display Box
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = String.format(strings.generatorStrength, strengthText),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Text(
                            text = generatedResult.password,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (length > 32) 14.sp else 18.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("generated_password_display")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Regenerate
                    OutlinedButton(
                        onClick = { viewModel.regeneratePassword() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("regenerate_password_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = strings.generatorRegenerate,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.generatorRegenerate)
                    }

                    // Copy
                    FilledTonalButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(generatedResult.password))
                            viewModel.showToast(strings.copiedPasswordToast)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("copy_generated_password_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = strings.btnCopy,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.btnCopy)
                    }

                    // Save
                    Button(
                        onClick = { viewModel.openSaveGeneratedDialog() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_generated_password_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = strings.btnSavePassword,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.btnSave)
                    }
                }
            }
        }

        // Length Controls
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = String.format(strings.generatorLength, length),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = { viewModel.setGeneratorLength(length - 1) },
                        enabled = length > 4,
                        modifier = Modifier.testTag("decrease_length_button")
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease length")
                    }

                    Text(
                        text = "$length",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    IconButton(
                        onClick = { viewModel.setGeneratorLength(length + 1) },
                        enabled = length < 128,
                        modifier = Modifier.testTag("increase_length_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Increase length")
                    }
                }

                Slider(
                    value = length.toFloat(),
                    onValueChange = { viewModel.setGeneratorLength(it.toInt()) },
                    valueRange = 4f..128f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("generator_length_slider")
                )
            }
        }

        // Character Options Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SimpleCheckboxRow(
                    label = strings.generatorUppercase,
                    checked = includeUpper,
                    onCheckedChange = { viewModel.toggleIncludeUpper(it) },
                    tag = "toggle_upper"
                )

                SimpleCheckboxRow(
                    label = strings.generatorLowercase,
                    checked = includeLower,
                    onCheckedChange = { viewModel.toggleIncludeLower(it) },
                    tag = "toggle_lower"
                )

                SimpleCheckboxRow(
                    label = strings.generatorNumbers,
                    checked = includeNumbers,
                    onCheckedChange = { viewModel.toggleIncludeNumbers(it) },
                    tag = "toggle_numbers"
                )

                SimpleCheckboxRow(
                    label = strings.generatorSymbols,
                    checked = includeSymbols,
                    onCheckedChange = { viewModel.toggleIncludeSymbols(it) },
                    tag = "toggle_symbols"
                )
            }
        }

        Spacer(modifier = Modifier.height(72.dp))
    }
}

@Composable
fun SimpleCheckboxRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(tag)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
