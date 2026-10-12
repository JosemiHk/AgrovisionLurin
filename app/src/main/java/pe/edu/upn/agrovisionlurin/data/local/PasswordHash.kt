package pe.edu.upn.agrovisionlurin.data.local

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHash {
    private const val ITERATIONS = 210_000
    fun salt(): String = ByteArray(16).also { SecureRandom().nextBytes(it) }.toHex()
    fun hash(password: String, salt: String): String {
        val spec = PBEKeySpec(password.toCharArray(), salt.fromHex(), ITERATIONS, 256)
        return try { SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).encoded.toHex() }
        finally { spec.clearPassword() }
    }
    fun verify(password: String, salt: String, expected: String): Boolean =
        salt.isNotEmpty() && expected.isNotEmpty() && MessageDigest.isEqual(hash(password, salt).toByteArray(), expected.toByteArray())
    private fun ByteArray.toHex() = joinToString("") { "%02x".format(it) }
    private fun String.fromHex() = chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}
