package com.example.i18n

enum class AppLanguage(val code: String, val displayName: String, val flag: String) {
    ENGLISH("en", "English", "🇺🇸"),
    TURKISH("tr", "Türkçe", "🇹🇷"),
    GERMAN("de", "Deutsch", "🇩🇪"),
    FRENCH("fr", "Français", "🇫🇷"),
    ITALIAN("it", "Italiano", "🇮🇹");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }
    }
}
