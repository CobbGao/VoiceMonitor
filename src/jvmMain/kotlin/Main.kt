import GptEngine.PREFIX_ALGO
import GptEngine.PREFIX_VOICE
import androidx.compose.material.MaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent
import com.github.kwhat.jnativehook.mouse.NativeMouseListener
import com.github.kwhat.jnativehook.mouse.NativeMouseWheelEvent
import com.github.kwhat.jnativehook.mouse.NativeMouseWheelListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.awt.Rectangle
import java.awt.Robot
import java.io.File
import javax.imageio.ImageIO

const val BUTTON_CODE_VOICE = 4
const val BUTTON_CODE_SCREENSHOT = 5

fun main() = application {
    Window(onCloseRequest = ::exitApplication, alwaysOnTop = true) {
        App()
    }
}

@Composable
@Preview
fun App() {
    val scrollState = rememberScrollState()
    startMonitor(scrollState)

    MaterialTheme {
        Column {
            var text1 by remember { mutableStateOf(PREFIX_VOICE) }
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = text1,
                onValueChange = { PREFIX_VOICE = it; text1 = it },
                textStyle = TextStyle(fontSize = 10.sp),
                singleLine = true,
            )
            var text2 by remember { mutableStateOf(PREFIX_ALGO) }
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = text2,
                onValueChange = { PREFIX_ALGO = it; text2 = it },
                textStyle = TextStyle(fontSize = 10.sp),
                singleLine = true,
            )
            val response by GptEngine.messageFlow.collectAsState()
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                text = response,
                fontSize = 10.sp,
            )
        }
    }
}

private fun startMonitor(scrollState: ScrollState) {
    GlobalScreen.registerNativeHook()

    adaptScroll(scrollState)

    startVoiceMonitor()
    startScreenshotMonitor()
}

fun adaptScroll(scrollState: ScrollState) {
    GlobalScreen.addNativeMouseWheelListener(object : NativeMouseWheelListener {
        override fun nativeMouseWheelMoved(nativeEvent: NativeMouseWheelEvent?) {
            val direction = nativeEvent?.wheelRotation ?: return
            ApplicationDefaultScope.launch {
                scrollState.scrollBy(direction * 5f)
            }
        }
    })
}

private fun startVoiceMonitor() {
    GlobalScreen.addNativeMouseListener(object : NativeMouseListener {
        private var start: Boolean = false
        private var job: Job? = null

        override fun nativeMousePressed(nativeEvent: NativeMouseEvent?) {
            if (nativeEvent?.button != BUTTON_CODE_VOICE) {
                return
            }
            startCapture()
        }

        override fun nativeMouseReleased(nativeEvent: NativeMouseEvent?) {
            if (nativeEvent?.button != BUTTON_CODE_VOICE) {
                return
            }
            endCapture()
        }

        private fun startCapture() {
            start = true
            println("find mouse button [$BUTTON_CODE_VOICE] pressed, start to capture system audio output stream...")
            job?.cancel()
            job = ApplicationDefaultScope.launch(Dispatchers.IO) {
                val data = VoiceCollector.collectPcmData { start }
                val text = AipManager.pcmAsr(data, 16000)
                GptEngine.forward(text)
            }
        }

        private fun endCapture() {
            start = false
            println("mouse button [$BUTTON_CODE_VOICE] released, stop capture.")
        }
    })
}

private fun startScreenshotMonitor() {
    GlobalScreen.addNativeMouseListener(object : NativeMouseListener {
        private val robot = Robot()
        private var start = 0 to 0

        override fun nativeMousePressed(nativeEvent: NativeMouseEvent?) {
            if (nativeEvent?.button != BUTTON_CODE_SCREENSHOT) {
                return
            }
            start = nativeEvent.x to nativeEvent.y
        }

        override fun nativeMouseReleased(nativeEvent: NativeMouseEvent?) {
            if (nativeEvent?.button != BUTTON_CODE_SCREENSHOT) {
                return
            }
            val end = nativeEvent.x to nativeEvent.y
            val rect = Rectangle(start.first, start.second, end.first - start.first, end.second - start.second)
            val bufferedImage = robot.createScreenCapture(rect)
            val jpgFile = File.createTempFile("screenshot", ".jpg")
            ImageIO.write(bufferedImage, "jpg", jpgFile)
            val ocrString = AipManager.jpgOcr(jpgFile.absolutePath)
            jpgFile.delete()
            GptEngine.algo(ocrString)
        }
    })
}


