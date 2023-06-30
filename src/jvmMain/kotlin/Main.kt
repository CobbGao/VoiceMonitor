import androidx.compose.material.MaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent
import com.github.kwhat.jnativehook.mouse.NativeMouseListener

const val CONFIG_BUTTON_CODE = 4
const val VTT_APP_ID = 35514218
const val VTT_APP_API_KEY = "z9E2rDOtT7I0zE2Hkx9BCMVc"
const val VTT_APP_SECRET_KEY = "9B8K73TCSWtmEVkhlc8Ol7ZypX178t7c"

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}

@Composable
@Preview
fun App() {
    var text by remember { mutableStateOf("Hello, World!") }

    startMonitor()

    MaterialTheme {
        Button(onClick = {
            text = "Hello, Desktop!"
        }) {
            Text(text)
        }
    }
}

private fun startMonitor() {
    GlobalScreen.registerNativeHook()
    GlobalScreen.addNativeMouseListener(object : NativeMouseListener {
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
            println("find mouse button [$CONFIG_BUTTON_CODE] pressed, start to capture system audio output stream...")
            // https://blog.csdn.net/qq_41054313/article/details/89640209
        }

        private fun endCapture() {
            println("mouse button [$CONFIG_BUTTON_CODE] released, stop capture.")
            // https://ai.baidu.com/ai-doc/SPEECH/plbxfq24s
            // chat gpt大模型相关API调用
        }
    })
}


