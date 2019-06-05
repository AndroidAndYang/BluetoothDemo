package com.seabig.blelock.util

import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * @author: YJZ
 * @date: 2019/5/31 14:50
 * @Des: AES 加解密
 */
object AESUtil {

    /**
     * 加密
     *
     * @param src 密文
     * @param key 加密密钥
     * @return 加密后的数据
     */
    private fun encrypt(src: ByteArray, key: ByteArray): ByteArray? {
        return try {
            val secretKeySpec = SecretKeySpec(key, "AES")
            val cipher = Cipher.getInstance("AES/ECB/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
            cipher.doFinal(src)
        } catch (ex: Exception) {
            null
        }

    }

    fun defaultEncrypt(sSrc: ByteArray): ByteArray? {
        return encrypt(sSrc, HexUtil.hexStringToBytes(""))
    }

    /**
     * 解密
     *
     * @param src 密文
     * @param key 解密密钥
     * @return 解密后的数据
     */
    private fun decrypt(src: ByteArray, key: ByteArray): ByteArray? {
        return try {
            val secretKeySpec = SecretKeySpec(key, "AES")
            val cipher = Cipher.getInstance("AES/ECB/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
            cipher.doFinal(src)
        } catch (ex: Exception) {
            null
        }
    }

    fun defaultDecrypt(sSrc: ByteArray): ByteArray? {
        return decrypt(sSrc, HexUtil.hexStringToBytes(""))
    }
}
