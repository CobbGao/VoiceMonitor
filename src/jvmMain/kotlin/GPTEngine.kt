import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

object GPTEngine {
    private const val ADDRESS = "https://api.texttools.cn/api/chat/stream"

    var PREFIX_VOICE = "假设场景：你是一个Java程序员，正在参加程序员的面试，现在我向你提出问题，请尝试从一个程序员的角度回答问题。" +
            "问题的文本内容来自于语音识别技术，当问题晦涩难懂时，尝试根据中文发音猜测对应英文词汇来理解问题，一些可能出现的英文单词有：Java、Spring Boot。" +
            "下面是问题："
    var PREFIX_ALGO = "解答下列问题，给出Java实现。优先给出性能更好的实现。尽量避免使用递归思想。先给出代码实现，再阐述解题思路。"

    val messageFlow = MutableStateFlow("")

    private var currentJob: Job? = null

    fun forward(content: String) {
        currentJob?.cancel()
        currentJob = ApplicationDefaultScope.launch(Dispatchers.IO) {
            if (content.isBlank()) {
                messageFlow.value = "query: [无语音识别结果]:\n"
                return@launch
            }
            forwardInner(this, PREFIX_VOICE, content)
        }
    }

    fun algo(content: String) {
        currentJob?.cancel()
        currentJob = ApplicationDefaultScope.launch(Dispatchers.IO) {
            if (content.isBlank()) {
                messageFlow.value = "query: [剪贴板无文本内容]:\n"
                return@launch
            }
            forwardInner(this, PREFIX_ALGO, content)
        }
    }

    private fun forwardInner(scope: CoroutineScope, prefix: String, content: String) {
        messageFlow.value = "content: $content:\n"
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