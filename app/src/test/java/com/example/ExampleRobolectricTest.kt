package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.crypto.CryptoEngine
import com.example.crypto.EncryptionSettings
import com.example.generator.PasswordGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Keepseek", appName)
    }

    @Test
    fun `test password generator generates correct length up to 128`() {
        val gen16 = PasswordGenerator.generate(16, true, true, true, true)
        assertEquals(16, gen16.password.length)

        val gen128 = PasswordGenerator.generate(128, true, true, true, true)
        assertEquals(128, gen128.password.length)
    }

    @Test
    fun `test crypto engine encrypt and decrypt`() {
        val settings = EncryptionSettings()
        val plain = "SecretP@ssword123!"
        val encrypted = CryptoEngine.encrypt(plain, settings)
        assertNotNull(encrypted.ciphertext)

        val decrypted = CryptoEngine.decrypt(
            encrypted.ciphertext,
            encrypted.iv,
            encrypted.salt,
            encrypted.algorithm,
            settings.pbkdfRounds.rounds
        )
        assertEquals(plain, decrypted)
    }
}
