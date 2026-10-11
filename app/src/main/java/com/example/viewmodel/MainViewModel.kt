package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.crypto.AutoLockTimeout
import com.example.crypto.CipherAlgorithm
import com.example.crypto.CryptoEngine
import com.example.crypto.EncryptionSettings
import com.example.crypto.PbkdfRounds
import com.example.crypto.SaltSize
import com.example.data.VaultDatabase
import com.example.data.VaultEntity
import com.example.generator.PasswordGenerator
import com.example.generator.PasswordResult
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.i18n.TranslationStrings
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("keepseek_prefs", Context.MODE_PRIVATE)
    private val db = VaultDatabase.getInstance(application)
    private val dao = db.vaultDao()

    // Auth & Lock State
    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private val _masterPin = MutableStateFlow(prefs.getString("master_pin", "1234") ?: "1234")
    val masterPin: StateFlow<String> = _masterPin.asStateFlow()

    private var lastActiveTime: Long = SystemClock.elapsedRealtime()

    // App Preferences: Language & Theme
    private val _currentLanguage = MutableStateFlow(
        AppLanguage.fromCode(prefs.getString("app_language", AppLanguage.ENGLISH.code) ?: AppLanguage.ENGLISH.code)
    )
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _currentTheme = MutableStateFlow(
        AppThemeMode.entries.find { it.id == prefs.getString("app_theme", AppThemeMode.CYBER_DARK.id) }
            ?: AppThemeMode.CYBER_DARK
    )
    val currentTheme: StateFlow<AppThemeMode> = _currentTheme.asStateFlow()

    // Encryption Settings
    private val _encryptionSettings = MutableStateFlow(loadEncryptionSettings())
    val encryptionSettings: StateFlow<EncryptionSettings> = _encryptionSettings.asStateFlow()

    // Generator State
    private val _generatorLength = MutableStateFlow(prefs.getInt("gen_length", 20).coerceIn(4, 128))
    val generatorLength: StateFlow<Int> = _generatorLength.asStateFlow()

    private val _includeUpper = MutableStateFlow(prefs.getBoolean("gen_upper", true))
    val includeUpper: StateFlow<Boolean> = _includeUpper.asStateFlow()

    private val _includeLower = MutableStateFlow(prefs.getBoolean("gen_lower", true))
    val includeLower: StateFlow<Boolean> = _includeLower.asStateFlow()

    private val _includeNumbers = MutableStateFlow(prefs.getBoolean("gen_numbers", true))
    val includeNumbers: StateFlow<Boolean> = _includeNumbers.asStateFlow()

    private val _includeSymbols = MutableStateFlow(prefs.getBoolean("gen_symbols", true))
    val includeSymbols: StateFlow<Boolean> = _includeSymbols.asStateFlow()

    private val _generatedResult = MutableStateFlow(
        PasswordGenerator.generate(
            _generatorLength.value,
            _includeUpper.value,
            _includeLower.value,
            _includeNumbers.value,
            _includeSymbols.value
        )
    )
    val generatedResult: StateFlow<PasswordResult> = _generatedResult.asStateFlow()

    // Saved Passwords & Filter/Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _revealedPasswordIds = MutableStateFlow<Set<Long>>(emptySet())
    val revealedPasswordIds: StateFlow<Set<Long>> = _revealedPasswordIds.asStateFlow()

    val vaultItems: StateFlow<List<VaultEntity>> = dao.getAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredVaultItems: StateFlow<List<VaultEntity>> = combine(vaultItems, _searchQuery) { items, query ->
        if (query.isBlank()) {
            items
        } else {
            val q = query.lowercase().trim()
            items.filter {
                it.name.lowercase().contains(q) ||
                        it.website.lowercase().contains(q) ||
                        it.email.lowercase().contains(q) ||
                        it.description.lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dialog & Confirmation Flows
    private val _confirmEditTarget = MutableStateFlow<VaultEntity?>(null)
    val confirmEditTarget: StateFlow<VaultEntity?> = _confirmEditTarget.asStateFlow()

    private val _editingEntity = MutableStateFlow<VaultEntity?>(null)
    val editingEntity: StateFlow<VaultEntity?> = _editingEntity.asStateFlow()

    private val _confirmDeleteStep1Target = MutableStateFlow<VaultEntity?>(null)
    val confirmDeleteStep1Target: StateFlow<VaultEntity?> = _confirmDeleteStep1Target.asStateFlow()

    private val _confirmDeleteStep2Target = MutableStateFlow<VaultEntity?>(null)
    val confirmDeleteStep2Target: StateFlow<VaultEntity?> = _confirmDeleteStep2Target.asStateFlow()

    private val _isAddDialogOpen = MutableStateFlow(false)
    val isAddDialogOpen: StateFlow<Boolean> = _isAddDialogOpen.asStateFlow()

    private val _isSaveGeneratedDialogOpen = MutableStateFlow(false)
    val isSaveGeneratedDialogOpen: StateFlow<Boolean> = _isSaveGeneratedDialogOpen.asStateFlow()

    private val _toastEvent = MutableStateFlow<String?>(null)
    val toastEvent: StateFlow<String?> = _toastEvent.asStateFlow()

    init {
        // Ensure no sample account exists and database starts empty
        viewModelScope.launch {
            dao.deleteByEmailOrName("user@example.com", "Google Account")
        }
    }

    fun getStrings(): TranslationStrings {
        return AppStrings.get(_currentLanguage.value)
    }

    // Auth actions
    fun unlockVault() {
        _isUnlocked.value = true
        lastActiveTime = SystemClock.elapsedRealtime()
    }

    fun lockVault() {
        _isUnlocked.value = false
        _revealedPasswordIds.value = emptySet()
        showToast(getStrings().vaultLockedNotification)
    }

    fun checkAutoLock() {
        val timeoutSec = _encryptionSettings.value.autoLockTimeout.seconds
        if (timeoutSec >= 0 && _isUnlocked.value) {
            val elapsedSec = (SystemClock.elapsedRealtime() - lastActiveTime) / 1000
            if (elapsedSec >= timeoutSec) {
                _isUnlocked.value = false
                _revealedPasswordIds.value = emptySet()
            }
        }
    }

    fun recordUserActivity() {
        lastActiveTime = SystemClock.elapsedRealtime()
    }

    fun setMasterPin(newPin: String) {
        _masterPin.value = newPin
        prefs.edit().putString("master_pin", newPin).apply()
        showToast("Master PIN updated!")
    }

    // Language & Theme
    fun setLanguage(lang: AppLanguage) {
        _currentLanguage.value = lang
        prefs.edit().putString("app_language", lang.code).apply()
    }

    fun setTheme(theme: AppThemeMode) {
        _currentTheme.value = theme
        prefs.edit().putString("app_theme", theme.id).apply()
    }

    // Encryption Settings
    private fun loadEncryptionSettings(): EncryptionSettings {
        val algoId = prefs.getString("enc_algo", CipherAlgorithm.AES_GCM.id)
        val algo = CipherAlgorithm.entries.find { it.id == algoId } ?: CipherAlgorithm.AES_GCM

        val roundsVal = prefs.getInt("enc_rounds", PbkdfRounds.ROUNDS_100K.rounds)
        val rounds = PbkdfRounds.entries.find { it.rounds == roundsVal } ?: PbkdfRounds.ROUNDS_100K

        val saltVal = prefs.getInt("enc_salt", SaltSize.BYTES_32.bytes)
        val salt = SaltSize.entries.find { it.bytes == saltVal } ?: SaltSize.BYTES_32

        val timeoutVal = prefs.getInt("enc_timeout", AutoLockTimeout.FIVE_MINUTES.seconds)
        val timeout = AutoLockTimeout.entries.find { it.seconds == timeoutVal } ?: AutoLockTimeout.FIVE_MINUTES

        return EncryptionSettings(algo, rounds, salt, timeout)
    }

    fun updateEncryptionSettings(
        algorithm: CipherAlgorithm? = null,
        pbkdfRounds: PbkdfRounds? = null,
        saltSize: SaltSize? = null,
        autoLockTimeout: AutoLockTimeout? = null
    ) {
        val current = _encryptionSettings.value
        val updated = current.copy(
            algorithm = algorithm ?: current.algorithm,
            pbkdfRounds = pbkdfRounds ?: current.pbkdfRounds,
            saltSize = saltSize ?: current.saltSize,
            autoLockTimeout = autoLockTimeout ?: current.autoLockTimeout
        )
        _encryptionSettings.value = updated
        prefs.edit()
            .putString("enc_algo", updated.algorithm.id)
            .putInt("enc_rounds", updated.pbkdfRounds.rounds)
            .putInt("enc_salt", updated.saltSize.bytes)
            .putInt("enc_timeout", updated.autoLockTimeout.seconds)
            .apply()
    }

    // Password Generator
    fun setGeneratorLength(length: Int) {
        _generatorLength.value = length.coerceIn(4, 128)
        prefs.edit().putInt("gen_length", _generatorLength.value).apply()
        regeneratePassword()
    }

    fun toggleIncludeUpper(include: Boolean) {
        _includeUpper.value = include
        prefs.edit().putBoolean("gen_upper", include).apply()
        regeneratePassword()
    }

    fun toggleIncludeLower(include: Boolean) {
        _includeLower.value = include
        prefs.edit().putBoolean("gen_lower", include).apply()
        regeneratePassword()
    }

    fun toggleIncludeNumbers(include: Boolean) {
        _includeNumbers.value = include
        prefs.edit().putBoolean("gen_numbers", include).apply()
        regeneratePassword()
    }

    fun toggleIncludeSymbols(include: Boolean) {
        _includeSymbols.value = include
        prefs.edit().putBoolean("gen_symbols", include).apply()
        regeneratePassword()
    }

    fun regeneratePassword() {
        _generatedResult.value = PasswordGenerator.generate(
            _generatorLength.value,
            _includeUpper.value,
            _includeLower.value,
            _includeNumbers.value,
            _includeSymbols.value
        )
    }

    // Saved Passwords management
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun togglePasswordVisibility(id: Long) {
        val current = _revealedPasswordIds.value
        _revealedPasswordIds.value = if (current.contains(id)) {
            current - id
        } else {
            current + id
        }
    }

    fun isPasswordRevealed(id: Long): Boolean {
        return _revealedPasswordIds.value.contains(id)
    }

    fun getDecryptedPassword(entity: VaultEntity): String {
        return CryptoEngine.decrypt(
            ciphertextBase64 = entity.encryptedPassword,
            ivBase64 = entity.iv,
            saltBase64 = entity.salt,
            algorithmId = entity.algorithm,
            iterations = _encryptionSettings.value.pbkdfRounds.rounds
        )
    }

    fun saveCredential(
        name: String,
        website: String,
        email: String,
        passwordPlain: String,
        description: String
    ) {
        viewModelScope.launch {
            val enc = CryptoEngine.encrypt(passwordPlain, _encryptionSettings.value)
            val entity = VaultEntity(
                name = name.ifBlank { "Untitled" },
                website = website,
                email = email,
                encryptedPassword = enc.ciphertext,
                iv = enc.iv,
                salt = enc.salt,
                algorithm = enc.algorithm,
                description = description
            )
            dao.insertItem(entity)
            showToast("Saved '${entity.name}' to Vault!")
        }
    }

    fun updateCredential(
        id: Long,
        name: String,
        website: String,
        email: String,
        passwordPlain: String,
        description: String
    ) {
        viewModelScope.launch {
            val enc = CryptoEngine.encrypt(passwordPlain, _encryptionSettings.value)
            val updated = VaultEntity(
                id = id,
                name = name.ifBlank { "Untitled" },
                website = website,
                email = email,
                encryptedPassword = enc.ciphertext,
                iv = enc.iv,
                salt = enc.salt,
                algorithm = enc.algorithm,
                description = description,
                updatedAt = System.currentTimeMillis()
            )
            dao.updateItem(updated)
            showToast("Updated '${updated.name}'!")
        }
    }

    // Confirmation flows:
    // Edit requires 1 confirmation
    fun requestEdit(item: VaultEntity) {
        _confirmEditTarget.value = item
    }

    fun confirmEditAccepted() {
        val target = _confirmEditTarget.value
        _confirmEditTarget.value = null
        _editingEntity.value = target
    }

    fun dismissConfirmEdit() {
        _confirmEditTarget.value = null
    }

    fun dismissEditSheet() {
        _editingEntity.value = null
    }

    // Delete requires 2 confirmations
    fun requestDelete(item: VaultEntity) {
        _confirmDeleteStep1Target.value = item
    }

    fun confirmDeleteStep1Accepted() {
        val target = _confirmDeleteStep1Target.value
        _confirmDeleteStep1Target.value = null
        _confirmDeleteStep2Target.value = target
    }

    fun dismissConfirmDeleteStep1() {
        _confirmDeleteStep1Target.value = null
    }

    fun confirmDeleteStep2Accepted() {
        val target = _confirmDeleteStep2Target.value
        _confirmDeleteStep2Target.value = null
        if (target != null) {
            viewModelScope.launch {
                dao.deleteById(target.id)
                showToast("Permanently removed '${target.name}'")
            }
        }
    }

    fun dismissConfirmDeleteStep2() {
        _confirmDeleteStep2Target.value = null
    }

    // Dialog toggles
    fun openAddDialog() {
        _isAddDialogOpen.value = true
    }

    fun closeAddDialog() {
        _isAddDialogOpen.value = false
    }

    fun openSaveGeneratedDialog() {
        _isSaveGeneratedDialogOpen.value = true
    }

    fun closeSaveGeneratedDialog() {
        _isSaveGeneratedDialogOpen.value = false
    }

    fun showToast(message: String) {
        _toastEvent.value = message
    }

    fun clearToast() {
        _toastEvent.value = null
    }
}
