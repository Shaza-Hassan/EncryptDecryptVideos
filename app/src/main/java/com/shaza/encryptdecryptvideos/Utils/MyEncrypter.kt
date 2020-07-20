package com.shaza.encryptdecryptvideos.Utils

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Created by Shaza Hassan on 16-Jul-20.
 */
object MyEncrypter {
    private val defaultReadWriteBlockBufferSize = 1024
    private val algoVideoEncryptor = "AES/CBC/PKCS5Padding"
    private val algoSecretKey = "AES"

    @Throws(
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        IOException::class,
        InvalidAlgorithmParameterException::class
    )
    fun encryptToFile(
        key: String,
        spec: String,
        inputStream: InputStream,
        outputStream: OutputStream
    ) {
        var outputStream = outputStream
        try {
            val iv = IvParameterSpec(spec.toByteArray(charset("UTF-8")))
            val keySpec = SecretKeySpec(key.toByteArray(charset("UTF-8")), algoSecretKey)
            val c = Cipher.getInstance(algoVideoEncryptor)
            c.init(Cipher.ENCRYPT_MODE, keySpec, iv)
            outputStream = CipherOutputStream(outputStream, c)
            val buffer = ByteArray(defaultReadWriteBlockBufferSize)
            var bytesRead: Int = 0
            while (inputStream.read(buffer).also { bytesRead = it } > 0) {
                outputStream.write(buffer,0,bytesRead)
            }
        } finally {
            outputStream.close()
        }
    }

    fun decryptToFile(key:String,spec:String, inputStream: InputStream,outputStream: OutputStream){
        var outputStream = outputStream
        try {
            val iv = IvParameterSpec(spec.toByteArray(charset("UTF-8")))
            val keySpec = SecretKeySpec(key.toByteArray(charset("UTF-8")), algoSecretKey)
            val c = Cipher.getInstance(algoVideoEncryptor)
            c.init(Cipher.DECRYPT_MODE,keySpec,iv)
            outputStream = CipherOutputStream(outputStream,c)
            val buffer = ByteArray(defaultReadWriteBlockBufferSize)
            var bytesRead: Int = 0
            while (inputStream.read(buffer).also { bytesRead = it } > 0){
                outputStream.write(buffer,0,bytesRead)
            }
        } finally {
            outputStream.close()
        }
    }
}