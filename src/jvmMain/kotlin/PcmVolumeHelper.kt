import kotlin.math.exp
import kotlin.math.sign

object PcmVolumeHelper {
    private const val N_SHORTS = 0xffff
    private val VOLUME_NORM_LUT = ShortArray(N_SHORTS)
    private const val MAX_NEGATIVE_AMPLITUDE = 0x8000

    init {
        precomputeVolumeNormLUT()
    }

    private fun normalizeVolume(audioSamples: ByteArray, start: Int, len: Int) {
        var i = start
        while (i < start + len) {
            // convert byte pair to int
            var s1 = audioSamples[i + 1].toShort()
            var s2 = audioSamples[i].toShort()
            s1 = (s1.toInt() and 0xff shl 8).toShort()
            s2 = (s2.toInt() and 0xff).toShort()
            var res = (s1.toInt() or s2.toInt()).toShort()
            res = VOLUME_NORM_LUT[res + MAX_NEGATIVE_AMPLITUDE]
            audioSamples[i] = res.toByte()
            audioSamples[i + 1] = (res.toInt() shr 8).toByte()
            i += 2
        }
    }

    private fun precomputeVolumeNormLUT() {
        for (s in 0 until N_SHORTS) {
            val v = (s - MAX_NEGATIVE_AMPLITUDE).toDouble()
            val sign = sign(v)
            // Non-linear volume boost function
            // fitted exponential through (0,0), (10000, 25000), (32767, 32767)
            VOLUME_NORM_LUT[s] = (sign * (1.240769e-22 - -4.66022 / 0.0001408133 * (1 - exp(-0.0001408133 * v * sign)))).toInt().toShort()
        }
    }
}