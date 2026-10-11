package com.example.crypto

enum class CipherAlgorithm(val id: String, val transformation: String, val displayName: String, val ivLength: Int) {
    AES_GCM("AES_GCM", "AES/GCM/NoPadding", "AES-256-GCM (Authenticated)", 12),
    AES_CBC("AES_CBC", "AES/CBC/PKCS5Padding", "AES-256-CBC (Standard)", 16)
}

enum class PbkdfRounds(val rounds: Int, val label: String) {
    ROUNDS_10K(10_000, "10,000 Rounds (Fast)"),
    ROUNDS_100K(100_000, "100,000 Rounds (Recommended)"),
    ROUNDS_250K(250_000, "250,000 Rounds (Enhanced)"),
    ROUNDS_500K(500_000, "500,000 Rounds (Military-Grade)")
}

enum class SaltSize(val bytes: Int, val label: String) {
    BYTES_16(16, "16 Bytes (128-bit Salt)"),
    BYTES_32(32, "32 Bytes (256-bit Salt)")
}

enum class AutoLockTimeout(val seconds: Int, val label: String) {
    IMMEDIATELY(0, "Immediately"),
    ONE_MINUTE(60, "1 Minute"),
    FIVE_MINUTES(300, "5 Minutes"),
    FIFTEEN_MINUTES(900, "15 Minutes"),
    NEVER(-1, "Manual Only")
}

data class EncryptionSettings(
    val algorithm: CipherAlgorithm = CipherAlgorithm.AES_GCM,
    val pbkdfRounds: PbkdfRounds = PbkdfRounds.ROUNDS_100K,
    val saltSize: SaltSize = SaltSize.BYTES_32,
    val autoLockTimeout: AutoLockTimeout = AutoLockTimeout.FIVE_MINUTES
)
