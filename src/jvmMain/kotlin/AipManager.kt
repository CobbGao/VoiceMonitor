import com.baidu.aip.ocr.AipOcr
import com.baidu.aip.speech.AipSpeech
import java.lang.StringBuilder

private const val VTT_APP_ID = "35514218"
private const val VTT_APP_API_KEY = "z9E2rDOtT7I0zE2Hkx9BCMVc"
private const val VTT_APP_SECRET_KEY = "9B8K73TCSWtmEVkhlc8Ol7ZypX178t7c"

object AipManager {
    private val asrClient = AipSpeech(VTT_APP_ID, VTT_APP_API_KEY, VTT_APP_SECRET_KEY).apply {
        setConnectionTimeoutInMillis(2000)
        setSocketTimeoutInMillis(60000)
    }

    private val ocrClient = AipOcr(VTT_APP_ID, VTT_APP_API_KEY, VTT_APP_SECRET_KEY).apply {
        setConnectionTimeoutInMillis(2000)
        setSocketTimeoutInMillis(60000)
    }

    fun pcmAsr(data: ByteArray, rate: Int): String {
        return with(asrClient.asr(data, "pcm", rate, null)) {
            return@with optJSONArray("result")?.getString(0)?.format() ?: ""
        }
    }

    fun jpgOcr(path: String): String {
        val json = ocrClient.basicAccurateGeneral(path, HashMap())
        return with(json.optJSONArray("words_result")) {
            if (this == null) {
                return@with ""
            }
            val text = StringBuilder()
            for (i in 0 until length()) {
                text.append(this.getJSONObject(i).optString("words")).append("\n")
            }
            return@with text.toString()
        }
    }

    private fun String.format() = this.replace(Regex("\\p{P}"), "")
}