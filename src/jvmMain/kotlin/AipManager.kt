import com.baidu.aip.ocr.AipOcr
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

private const val VTT_APP_ID = "35514218"
private const val VTT_APP_API_KEY = "z9E2rDOtT7I0zE2Hkx9BCMVc"
private const val VTT_APP_SECRET_KEY = "9B8K73TCSWtmEVkhlc8Ol7ZypX178t7c"

object AipManager {
    private val ocrClient = AipOcr(VTT_APP_ID, VTT_APP_API_KEY, VTT_APP_SECRET_KEY).apply {
        setConnectionTimeoutInMillis(2000)
        setSocketTimeoutInMillis(60000)
    }

    fun pcmAsr(data: ByteArray, rate: Int): String {
        val folder = ClassLoader.getSystemClassLoader().getResource("sherpa-ncnn.exe")?.toURI()?.path?.removeRange(0, 1)?.replace("sherpa-ncnn.exe", "")!!
        val exe = folder + "sherpa-ncnn.exe"
        val model = folder + "model"
        return with(data.toWavFile()) {
            exec(
                exe,
                "$model/tokens.txt",
                "$model/encoder_jit_trace-pnnx.ncnn.param",
                "$model/encoder_jit_trace-pnnx.ncnn.bin",
                "$model/decoder_jit_trace-pnnx.ncnn.param",
                "$model/decoder_jit_trace-pnnx.ncnn.bin",
                "$model/joiner_jit_trace-pnnx.ncnn.param",
                "$model/joiner_jit_trace-pnnx.ncnn.bin",
                absolutePath,
                "1",
                "modified_beam_search",
            ).also { this.delete() }
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

    private fun exec(exe: String, vararg params: String): String {
        val builder = StringBuilder()
        var br: BufferedReader? = null
        var brError: BufferedReader? = null
        var line: String? = null
        var lineError: String? = null
        try {
            val process = ProcessBuilder(listOf(exe) + params.asList()).start()
            br = BufferedReader(InputStreamReader(process.inputStream))
            brError = BufferedReader(InputStreamReader(process.errorStream))
            while (true) {
                line = br.readLine()
                lineError = brError.readLine()
                if (line == null && lineError == null) {
                    break
                }
                if (line != null) builder.append(line).append("\n")
                if (lineError != null) builder.append(lineError).append("\n")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (br != null) {
                try {
                    br.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        println(builder.toString())
        return builder.toString().split("\n").firstOrNull { it.startsWith("text: ") }?.drop(6) ?: ""
    }
}