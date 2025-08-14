package com.guardiankids.app.data

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object ChatCrypto {
    // Demo key (32 bytes). En producción genera y comparte por par de claves.
    private val demoKey: ByteArray = ByteArray(32) { 7 }

    fun encrypt(plain: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val key = SecretKeySpec(demoKey, "AES")
        val iv = ByteArray(12)
        SecureRandom().nextBytes(iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
        val enc = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(iv + enc, Base64.NO_WRAP)
    }

    fun decrypt(b64: String): String {
        val data = Base64.decode(b64, Base64.NO_WRAP)
        val iv = data.sliceArray(0 until 12)
        val body = data.sliceArray(12 until data.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val key = SecretKeySpec(demoKey, "AES")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        val dec = cipher.doFinal(body)
        return String(dec, Charsets.UTF_8)
    }
}
