import GptHelper.PREFIX_ALGO
import GptHelper.PREFIX_VOICE
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
import java.awt.Robot
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.event.KeyEvent
import java.util.LinkedList
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED
import javax.sound.sampled.AudioSystem

private const val BUTTON_CODE_LEFT = 1

const val BUTTON_CODE_VOICE = 4
const val BUTTON_CODE_CLIPBOARD = 5

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
            val response by GptHelper.messageFlow.collectAsState()
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
    startClipboardMonitor()
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
                val audioFormat = AudioFormat(PCM_SIGNED, 16000f, 16, 1, (16 / 8) * 1, 16000f, false)
                val targetLine = AudioSystem.getTargetDataLine(audioFormat).apply { open(); start(); }
                val byteArrayList = LinkedList<ByteArray>()
                while (start && targetLine.read(ByteArray(1024 * 10).apply { byteArrayList.add(this) }, 0, 1024 * 10) > 0) {
                    // ignore
                }
                targetLine.close()
                val text = AipSpeechManager.pcmAsr(
                    ByteArray(1024 * 10 * byteArrayList.size).apply {
                        byteArrayList.forEachIndexed { index, bytes -> bytes.copyInto(this, index * 1024 * 10) }
                    },
                    16000
                )
                GptHelper.forward(text)
            }
        }

        private fun endCapture() {
            start = false
            println("mouse button [$BUTTON_CODE_VOICE] released, stop capture.")
        }
    })
}

private fun startClipboardMonitor() {
    GlobalScreen.addNativeMouseListener(object : NativeMouseListener {
        private val robot = Robot()
        private var time = 0L
        override fun nativeMousePressed(nativeEvent: NativeMouseEvent?) {
            when(nativeEvent?.button) {
                BUTTON_CODE_LEFT -> { time = System.currentTimeMillis() }
            }
        }

        override fun nativeMouseReleased(nativeEvent: NativeMouseEvent?) {
            when(nativeEvent?.button) {
                BUTTON_CODE_LEFT -> {
                    if (System.currentTimeMillis() - time > 500L) {
                        robot.keyPress(KeyEvent.VK_CONTROL)
                        robot.keyPress(KeyEvent.VK_C)
                        robot.keyRelease(KeyEvent.VK_C)
                        robot.keyRelease(KeyEvent.VK_CONTROL)
                    }
                }
            }
        }

        override fun nativeMouseClicked(nativeEvent: NativeMouseEvent?) {
            if (nativeEvent?.button != BUTTON_CODE_CLIPBOARD) {
                return
            }
            println("mouse button [$BUTTON_CODE_CLIPBOARD] clicked, query algo.")
            with(Toolkit.getDefaultToolkit().systemClipboard) {
                GptHelper.algo(getContents(null).getTransferData(DataFlavor.stringFlavor) as String)
            }
        }
    })
}


