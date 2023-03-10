package com.jmolina.nftgateway.util

fun String.decodeHex(): ByteArray {
    check(length % 2 == 0) { "Must have an even length" }
    val str = if (startsWith("0x")) substring(2) else this
    return str.chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
}