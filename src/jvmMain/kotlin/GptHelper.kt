import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

object GptHelper {
    private const val ADDRESS = "https://api.texttools.cn/api/chat/stream"

    var PREFIX = "假设场景：你是一个Java程序员，正在参加程序员的面试，现在我向你提出问题，请尝试从一个程序员的角度回答问题。" +
            "由于我提出的问题文本来自于语音识别技术，当问题晦涩难懂时，尝试根据中文发音猜测对应英文词汇来理解问题，一些可能对应的英文单词有：Java、Spring Boot。" +
            "下面是问题："

    val messageFlow = MutableStateFlow("")

    private var currentJob: Job? = null

    fun forward(content: String) {
        currentJob?.cancel()
        currentJob = ApplicationDefaultScope.launch(Dispatchers.IO) {
            if (content.isBlank()) {
                messageFlow.value = "prefix: $PREFIX\nquery: [无语音识别结果]:\n"
                return@launch
            }
            forwardInner(this, content)
        }
    }

    private fun forwardInner(scope: CoroutineScope, content: String) {
        messageFlow.value = "content: $content:\n"
        val url = URL(ADDRESS)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("apikey", "textAxVT0iq3uFfmDmDHr2n7cHeISPXmPljykOBL5VbPxeJv")
        connection.setRequestProperty("Accept", "text/event-stream")
        val requestBody = "{\"content\": \"$PREFIX$content\"}"
        connection.doOutput = true
        connection.outputStream.use {
            it.write(requestBody.toByteArray())
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
            messageFlow.value += String(bytes, 0, len, StandardCharsets.UTF_8)
        }
        connection.disconnect()
    }
}