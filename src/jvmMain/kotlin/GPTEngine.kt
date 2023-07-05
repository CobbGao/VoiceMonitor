import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

object GPTEngine {
    private const val ADDRESS = "https://api.texttools.cn/api/chat/stream"

    var PREFIX_VOICE = PREFIX_VOICE_MAP["MY"]!!
    var PREFIX_ALGO = PREFIX_ALGO_MAP["MY"]!!

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
        val url = URL(ADDRESS)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("apikey", "textAxVT0iq3uFfmDmDHr2n7cHeISPXmPljykOBL5VbPxeJv")
        connection.setRequestProperty("Accept", "text/event-stream")
        val requestBody = JSONObject().apply { put("content", "$prefix$content") }
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
        while (inputStream.read(bytes).also { len = it } != -1 && scope.isActive) {
            messageFlow.value += String(bytes, 0, len, StandardCharsets.UTF_8).replace("\n\n", "\n")
        }
        connection.disconnect()
    }
}