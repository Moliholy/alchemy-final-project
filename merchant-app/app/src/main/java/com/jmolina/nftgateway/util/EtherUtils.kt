package com.jmolina.nftgateway.util

import okhttp3.internal.and
import org.web3j.crypto.ECDSASignature
import org.web3j.crypto.Keys
import org.web3j.crypto.Sign
import org.web3j.crypto.Sign.SignatureData
import org.web3j.utils.Numeric
import java.math.BigInteger
import java.util.*


object EthersUtils {
    private const val MESSAGE_PREFIX = "\u0019Ethereum Signed Message:\n"
    fun verifyMessage(message: String, signature: String): String? {
        return recoverAddress(message, signature)
    }

    private fun recoverAddress(digest: String, signature: String): String? {
        val signatureData = getSignatureData(signature)
        var header = 0
        for (b in signatureData.v) {
            header = (header shl 8) + (b.and(0xFF))
        }
        if (header < 27 || header > 34) {
            return null
        }
        val recId = header - 27
        val key = Sign.recoverFromSignature(
            recId,
            ECDSASignature(
                BigInteger(1, signatureData.r), BigInteger(1, signatureData.s)
            ),
            Numeric.hexStringToByteArray(digest)
        ) ?: return null
        return ("0x" + Keys.getAddress(key)).trim { it <= ' ' }
    }

    private fun getSignatureData(signature: String): SignatureData {
        val signatureBytes = Numeric.hexStringToByteArray(signature)
        var v = signatureBytes[64]
        if (v < 27) {
            v = (v + 27).toByte()
        }
        val r = Arrays.copyOfRange(signatureBytes, 0, 32) as ByteArray
        val s = Arrays.copyOfRange(signatureBytes, 32, 64) as ByteArray
        return SignatureData(v, r, s)
    }
}