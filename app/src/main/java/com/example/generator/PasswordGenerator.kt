package com.example.generator

import java.security.SecureRandom
import kotlin.math.log2

enum class PasswordStrengthCategory {
    WEAK,
    MODERATE,
    STRONG,
    UNBREAKABLE
}

data class PasswordResult(
    val password: String,
    val length: Int,
    val entropyBits: Int,
    val strength: PasswordStrengthCategory,
    val poolSize: Int
)

object PasswordGenerator {
    private val secureRandom = SecureRandom()

    const val UPPERCASE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    const val LOWERCASE_CHARS = "abcdefghijklmnopqrstuvwxyz"
    const val NUMBER_CHARS = "0123456789"
    const val SYMBOL_CHARS = "!@#$%^&*()_+-=[]{}|;:,.<>?"

    fun generate(
        length: Int,
        includeUpper: Boolean,
        includeLower: Boolean,
        includeNumbers: Boolean,
        includeSymbols: Boolean
    ): PasswordResult {
        val safeLength = length.coerceIn(4, 128)
        val pools = mutableListOf<String>()
        val guaranteedChars = mutableListOf<Char>()

        if (includeUpper) {
            pools.add(UPPERCASE_CHARS)
            guaranteedChars.add(UPPERCASE_CHARS[secureRandom.nextInt(UPPERCASE_CHARS.length)])
        }
        if (includeLower) {
            pools.add(LOWERCASE_CHARS)
            guaranteedChars.add(LOWERCASE_CHARS[secureRandom.nextInt(LOWERCASE_CHARS.length)])
        }
        if (includeNumbers) {
            pools.add(NUMBER_CHARS)
            guaranteedChars.add(NUMBER_CHARS[secureRandom.nextInt(NUMBER_CHARS.length)])
        }
        if (includeSymbols) {
            pools.add(SYMBOL_CHARS)
            guaranteedChars.add(SYMBOL_CHARS[secureRandom.nextInt(SYMBOL_CHARS.length)])
        }

        // If none selected, default to lowercase + numbers
        if (pools.isEmpty()) {
            pools.add(LOWERCASE_CHARS)
            pools.add(NUMBER_CHARS)
            guaranteedChars.add(LOWERCASE_CHARS[secureRandom.nextInt(LOWERCASE_CHARS.length)])
            guaranteedChars.add(NUMBER_CHARS[secureRandom.nextInt(NUMBER_CHARS.length)])
        }

        val combinedPool = pools.joinToString("")
        val resultChars = mutableListOf<Char>()
        resultChars.addAll(guaranteedChars.take(safeLength))

        while (resultChars.size < safeLength) {
            val randomIndex = secureRandom.nextInt(combinedPool.length)
            resultChars.add(combinedPool[randomIndex])
        }

        // Shuffle securely
        for (i in resultChars.indices.reversed()) {
            val j = secureRandom.nextInt(i + 1)
            val temp = resultChars[i]
            resultChars[i] = resultChars[j]
            resultChars[j] = temp
        }

        val finalPassword = resultChars.joinToString("")
        val poolSize = combinedPool.toSet().size
        val entropy = if (poolSize > 1) (safeLength * log2(poolSize.toDouble())).toInt() else 0

        val strength = when {
            entropy < 45 || safeLength < 8 -> PasswordStrengthCategory.WEAK
            entropy < 65 || safeLength < 12 -> PasswordStrengthCategory.MODERATE
            entropy < 85 || safeLength < 16 -> PasswordStrengthCategory.STRONG
            else -> PasswordStrengthCategory.UNBREAKABLE
        }

        return PasswordResult(
            password = finalPassword,
            length = safeLength,
            entropyBits = entropy,
            strength = strength,
            poolSize = poolSize
        )
    }
}
