import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

private fun ByteArray.toWavFile(): File {
    val file = File.createTempFile("wav-", ".wav")
    var fos: FileOutputStream? = null
    try {
        fos = FileOutputStream(file)
        val header = WaveHeader(size + (44 - 8), 16, 16, 1, 0x0001, 16000, (1 * 16 / 8).toShort(), (16000 * 1 * 16 / 8).toShort(), size)
        val headerBytes: ByteArray = header.header
        assert(headerBytes.size == 44) // WAV标准，头部应该是44字节
        val byteResult = ByteArray(headerBytes.size + size)
        System.arraycopy(headerBytes, 0, byteResult, 0, headerBytes.size)
        System.arraycopy(this, 0, byteResult, headerBytes.size, size)
        fos.write(byteResult, 0, byteResult.size)
        fos.flush()
        fos.close()
    } catch (e: Exception) {
        if (fos != null) {
            try {
                fos.close()
            } catch (e1: IOException) {
                e1.printStackTrace()
            }
        }
    }
    return file
}

private data class WaveHeader(
    private val fileLength: Int = 0,
    private val fmtHdrLength: Int = 0,
    private val formatTag: Short = 0,
    private val channels: Short = 0,
    private val samplesPerSec: Int = 0,
    private val avgBytesPerSec: Int = 0,
    private val blockAlign: Short = 0,
    private val bitsPerSample: Short = 0,
    private val dataHdrLength: Int = 0,
) {
    @get:Throws(IOException::class)
    val header: ByteArray
        get() = ByteArrayOutputStream().use {
            writeChar(it, FILE_ID)
            writeInt(it, fileLength)
            writeChar(it, WAV_TAG)
            writeChar(it, FMT_HDR_ID)
            writeInt(it, fmtHdrLength)
            writeShort(it, formatTag.toInt())
            writeShort(it, channels.toInt())
            writeInt(it, samplesPerSec)
            writeInt(it, avgBytesPerSec)
            writeShort(it, blockAlign.toInt())
            writeShort(it, bitsPerSample.toInt())
            writeChar(it, DATA_HDR_ID)
            writeInt(it, dataHdrLength)
            it.flush()
            return@use it.toByteArray()
        }

    @Throws(IOException::class)
    private fun writeShort(bos: ByteArrayOutputStream, s: Int) {
        val bytes = ByteArray(2)
        bytes[1] = (s shl 16 shr 24).toByte()
        bytes[0] = (s shl 24 shr 24).toByte()
        bos.write(bytes)
    }

    @Throws(IOException::class)
    private fun writeInt(bos: ByteArrayOutputStream, n: Int) {
        val buf = ByteArray(4)
        buf[3] = (n shr 24).toByte()
        buf[2] = (n shl 8 shr 24).toByte()
        buf[1] = (n shl 16 shr 24).toByte()
        buf[0] = (n shl 24 shr 24).toByte()
        bos.write(buf)
    }

    private fun writeChar(bos: ByteArrayOutputStream, id: CharArray) {
        for (i in id.indices) {
            val c = id[i]
            bos.write(c.code)
        }
    }

    private companion object {
        val FILE_ID = charArrayOf('R', 'I', 'F', 'F')
        val WAV_TAG = charArrayOf('W', 'A', 'V', 'E')
        val FMT_HDR_ID = charArrayOf('f', 'm', 't', ' ')
        val DATA_HDR_ID = charArrayOf('d', 'a', 't', 'a')
    }
}