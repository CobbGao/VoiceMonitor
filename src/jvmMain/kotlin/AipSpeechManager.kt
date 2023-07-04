import com.baidu.aip.speech.AipSpeech

private const val VTT_APP_ID = "35514218"
private const val VTT_APP_API_KEY = "z9E2rDOtT7I0zE2Hkx9BCMVc"
private const val VTT_APP_SECRET_KEY = "9B8K73TCSWtmEVkhlc8Ol7ZypX178t7c"

object AipSpeechManager {
    private val client = AipSpeech(VTT_APP_ID, VTT_APP_API_KEY, VTT_APP_SECRET_KEY).apply {
        setConnectionTimeoutInMillis(2000)
        setSocketTimeoutInMillis(60000)
    }

    fun pcmAsr(pcmData: ByteArray, rate: Int): String {
        return with(client.asr(pcmData, "pcm", rate, null)) {
            return@with optJSONArray("result")?.getString(0)?.format() ?: ""
        }
    }

    private fun String.format() = this.replace(Regex("\\p{P}"), "")
}