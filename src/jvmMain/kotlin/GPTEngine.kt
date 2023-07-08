import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

object GPTEngine {
    private const val API_BASE = "https://api.closeai-proxy.xyz"
    private const val API_KEY = "sk-1OpHZCILEUm2kQ5IPDU3KgOk1JzRzh0JdIvMAW1Al4M91jMI"
    private const val MODEL = "gpt-3.5-turbo"

    var PREFIX_VOICE = PREFIX_VOICE_MAP["GBW"]!!
    var PREFIX_ALGO = PREFIX_ALGO_MAP["GBW"]!!

    val contentFlow = MutableStateFlow("")
    val messageFlow = MutableStateFlow("")

    private var currentJob: Job? = null

    fun forward(content: String) {
        currentJob?.cancel()
        currentJob = ApplicationDefaultScope.launch(Dispatchers.IO) {
            if (content.isBlank()) {
                contentFlow.value = "query: [无语音识别结果]:\n"
                return@launch
            }
            forwardInner(this, PREFIX_VOICE, content)
        }
    }

    fun algo(content: String) {
        currentJob?.cancel()
        currentJob = ApplicationDefaultScope.launch(Dispatchers.IO) {
            if (content.isBlank()) {
                contentFlow.value = "query: [OCR无文本内容]:\n"
                return@launch
            }
            forwardInner(this, PREFIX_ALGO, content)
        }
    }

    private fun forwardInner(scope: CoroutineScope, prefix: String, content: String) {
        contentFlow.value = "content: \n$content"
        messageFlow.value = ""
        val url = URL("$API_BASE/v1/chat/completions")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Authorization", "Bearer $API_KEY")
        val requestBody = JSONObject().apply {
            put("model", MODEL)
            put("stream", true)
            put("temperature", 0)
            put("top_p", 0)
            put("messages", JSONArray(arrayOf(
                JSONObject().apply {
                    put("role", "system")
                    put("content", prefix)
                },
                JSONObject().apply {
                    put("role", "user")
                    put("content", content)
                },
            )))
        }
        connection.doOutput = true
        connection.outputStream.use {
            it.write(requestBody.toString().toByteArray())
            it.flush()
        }
        val responseCode = connection.responseCode
        if (responseCode != HttpURLConnection.HTTP_OK) {
            messageFlow.value = "请求异常 [$responseCode]."
            connection.disconnect()
            return
        }
        val bytes = ByteArray(1024)
        var len: Int
        val inputStream = connection.inputStream
        var value = ""
        while (inputStream.read(bytes).also { len = it } != -1 && scope.isActive) {
            value += String(bytes, 0, len, StandardCharsets.UTF_8).trim()
            if (!value.endsWith('}')) {
                continue
            }
            messageFlow.value += value.extract()
            value = ""
        }
        connection.disconnect()
    }

    private fun String.extract(): String {
        return try {
            val data = JSONObject('{' + this.substringAfter('{'))
            val choices = data.getJSONArray("choices")
            val choice = choices.getJSONObject(0)
            val delta = choice.getJSONObject("delta")
            val content = delta.getString("content")
            content
        } catch (e: Throwable) {
            println(e)
            ""
        }
    }
}