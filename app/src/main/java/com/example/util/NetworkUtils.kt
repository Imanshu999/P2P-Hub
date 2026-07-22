package com.example.util

import java.net.NetworkInterface
import java.security.MessageDigest
import java.util.Collections
import kotlin.random.Random

object NetworkUtils {

    /**
     * Attempts to find the device's IPv4 address from network interfaces.
     * Falls back to a standard local subnet address if unavailable (e.g. on emulator).
     */
    fun getLocalIpAddress(): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                if (intf.isLoopback || !intf.isUp) continue
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress) {
                        val sAddr = addr.hostAddress ?: continue
                        val isIPv4 = sAddr.indexOf(':') < 0
                        if (isIPv4 && (sAddr.startsWith("192.168.") || sAddr.startsWith("10.") || sAddr.startsWith("172."))) {
                            return sAddr
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // Fallback default local subnet IP for preview/emulator environment
        return "192.168.1.142"
    }

    /**
     * Generates a high-security connection code formatted as "AETH-XXXX-XXXX" or "842-915"
     * based on local IP, current timestamp, and SHA-256 hash.
     */
    fun generateConnectionCode(ip: String): String {
        val rawInput = "$ip:${System.currentTimeMillis()}:${Random.nextInt(1000, 9999)}"
        val bytes = MessageDigest.getInstance("SHA-256").digest(rawInput.toByteArray())
        val hex = bytes.joinToString("") { "%02X".format(it) }
        val seg1 = hex.substring(0, 4)
        val seg2 = hex.substring(4, 8)
        return "AETH-$seg1-$seg2"
    }

    /**
     * Formats MAC/SHA fingerprint for security verification badge.
     */
    fun generateFingerprint(ip: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(ip.toByteArray())
        val hex = bytes.joinToString("") { "%02x".format(it) }
        return "SHA256:" + hex.substring(0, 8).uppercase() + "..." + hex.substring(hex.length - 8).uppercase()
    }
}
