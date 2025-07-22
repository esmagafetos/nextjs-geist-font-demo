package com.apkprotector.app.stub.model

data class PayloadHeader(
    val magic: Int,
    val version: Int,
    val expireTs: Long,
    val flags: Int,
    val originalSize: Int,
    val encryptedSize: Int,
    val iv: ByteArray,
    val reserved: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as PayloadHeader
        
        if (magic != other.magic) return false
        if (version != other.version) return false
        if (expireTs != other.expireTs) return false
        if (flags != other.flags) return false
        if (originalSize != other.originalSize) return false
        if (encryptedSize != other.encryptedSize) return false
        if (!iv.contentEquals(other.iv)) return false
        if (!reserved.contentEquals(other.reserved)) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = magic
        result = 31 * result + version
        result = 31 * result + expireTs.hashCode()
        result = 31 * result + flags
        result = 31 * result + originalSize
        result = 31 * result + encryptedSize
        result = 31 * result + iv.contentHashCode()
        result = 31 * result + reserved.contentHashCode()
        return result
    }
}
