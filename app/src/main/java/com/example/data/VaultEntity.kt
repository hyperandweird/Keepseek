package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vault_items")
data class VaultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val website: String,
    val email: String,
    val encryptedPassword: String,
    val iv: String,
    val salt: String,
    val algorithm: String,
    val description: String,
    val category: String = "Login",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
