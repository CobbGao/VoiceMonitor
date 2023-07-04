import GptHelper.PREFIX
import androidx.compose.material.MaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent
import com.github.kwhat.jnativehook.mouse.NativeMouseListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.LinkedList
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED
import javax.sound.sampled.AudioSystem

const val CONFIG_BUTTON_CODE = 4

fun main() = application {
    Window(onCloseRequest = ::exitApplication, alwaysOnTop = true) {
        App()
    }
}

@Composable
@Preview
fun App() {
    startMonitor()

    MaterialTheme {
        Column {
            var text by remember { mutableStateOf(PREFIX) }
            TextField(
                value = text,
                onValueChange = { PREFIX = it; text = it },
            )
            val response by GptHelper.messageFlow.collectAsState()
            Text(text = response)
        }
    }
}

private fun startMonitor() {
    GlobalScreen.registerNativeHook()
    GlobalScreen.addNativeMouseListener(object : NativeMouseListener {
        private var start: Boolean = false
        private var job: Job? = null

        override fun nativeMousePressed(nativeEvent: NativeMouseEvent?) {
            if (nativeEvent?.button != CONFIG_BUTTON_CODE) {
                return
            }
            startCapture()
        }

        override fun nativeMouseReleased(nativeEvent: NativeMouseEvent?) {
            if (nativeEvent?.button != CONFIG_BUTTON_CODE) {
                return
            }
            endCapture()
        }

        private fun startCapture() {
            start = true
            println("find mouse button [$CONFIG_BUTTON_CODE] pressed, start to capture system audio output stream...")
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
            println("mouse button [$CONFIG_BUTTON_CODE] released, stop capture.")
        }
    })
}


