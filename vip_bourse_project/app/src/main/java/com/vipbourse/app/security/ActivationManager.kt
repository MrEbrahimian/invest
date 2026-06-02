package com.vipbourse.app.security

import android.util.Base64
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object ActivationManager {
    // 32-byte secret key for AES-256
    private const val SECRET_KEY = "VIP_BOURSE_IRAN_SECRET_KEY_2026!"
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/ECB/PKCS5Padding"

    data class ActivationResult(
        val isValid: Boolean,
        val mobileNumber: String = "",
        val expiryDate: String = "",
        val errorMessage: String? = null
    )

    /**
     * Decrypts the offline AES payload.
     * Expected format: "mobile_number;expiry_date" (e.g., "09123456789;2027-12-31")
     */
    fun validateCode(encryptedCode: String): ActivationResult {
        if (encryptedCode.isBlank()) {
            return ActivationResult(false, errorMessage = "کد فعالسازی نمی‌تواند خالی باشد.")
        }
        return try {
            val keySpec = SecretKeySpec(SECRET_KEY.toByteArray(StandardCharsets.UTF_8), ALGORITHM)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, keySpec)

            val decodedBytes = Base64.decode(encryptedCode.trim(), Base64.DEFAULT)
            val decryptedBytes = cipher.doFinal(decodedBytes)
            val decryptedText = String(decryptedBytes, StandardCharsets.UTF_8)

            val parts = decryptedText.split(";")
            if (parts.size != 2) {
                return ActivationResult(false, errorMessage = "فرمت کد فعالسازی نامعتبر است.")
            }

            val mobile = parts[0].trim()
            val expiryStr = parts[1].trim()

            // Validate date format (YYYY-MM-DD)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            sdf.isLenient = false
            val expiryDate: Date
            try {
                expiryDate = sdf.parse(expiryStr) ?: return ActivationResult(false, errorMessage = "فرمت تاریخ نامعتبر است.")
            } catch (e: Exception) {
                return ActivationResult(false, errorMessage = "فرمت تاریخ کد باید YYYY-MM-DD باشد.")
            }

            // Check if expired (comparing with local device clock)
            val currentDate = Date()
            if (currentDate.after(expiryDate)) {
                return ActivationResult(
                    isValid = false,
                    mobileNumber = mobile,
                    expiryDate = expiryStr,
                    errorMessage = "کد فعالسازی در تاریخ $expiryStr منقضی شده است."
                )
            }

            ActivationResult(
                isValid = true,
                mobileNumber = mobile,
                expiryDate = expiryStr
            )
        } catch (e: Exception) {
            ActivationResult(
                isValid = false,
                errorMessage = "خطا در رمزگشایی کد. لطفاً صحت کپی کردن کد را بررسی کنید."
            )
        }
    }

    /**
     * Utility to generate code offline (ideal for the admin to create keys for purchasers).
     * Returns a Base64-encrypted string.
     */
    fun generateCode(mobile: String, expiryDate: String): String {
        return try {
            val plainText = "$mobile;$expiryDate"
            val keySpec = SecretKeySpec(SECRET_KEY.toByteArray(StandardCharsets.UTF_8), ALGORITHM)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)

            val encryptedBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
            Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            ""
        }
    }
}
