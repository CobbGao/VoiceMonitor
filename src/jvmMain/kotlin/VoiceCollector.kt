import java.util.*
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED
import javax.sound.sampled.AudioSystem

object VoiceCollector {
    fun collectPcmData(check: () -> Boolean): ByteArray {
        val audioFormat = AudioFormat(PCM_SIGNED, 16000f, 16, 1, (16 / 8) * 1, 16000f, false)
        val targetLine = AudioSystem.getTargetDataLine(audioFormat).apply { open(); start(); }
        val byteArrayList = LinkedList<ByteArray>()
        while (check() && targetLine.read(ByteArray(1024 * 10).apply { byteArrayList.add(this) }, 0, 1024 * 10) > 0) {
            // ignore
        }
        targetLine.close()
        return ByteArray(1024 * 10 * byteArrayList.size).apply {
            byteArrayList.forEachIndexed { index, bytes -> bytes.copyInto(this, index * 1024 * 10) }
        }
    }
}