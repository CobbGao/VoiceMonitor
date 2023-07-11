import GPTEngine.PREFIX_ALGO
import GPTEngine.PREFIX_VOICE
import androidx.compose.material.MaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.*
import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent.*
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent
import com.github.kwhat.jnativehook.mouse.NativeMouseListener
import com.github.kwhat.jnativehook.mouse.NativeMouseWheelEvent
import com.github.kwhat.jnativehook.mouse.NativeMouseWheelListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.awt.Rectangle
import java.awt.Robot
import java.awt.Toolkit
import java.io.File
import javax.imageio.ImageIO

const val BUTTON_CODE_VOICE = 4
const val BUTTON_CODE_SCREENSHOT = 5

@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
    val scrollState = rememberScrollState()
    startMonitor(scrollState)

    var settingsDialogVisible by remember { mutableStateOf(false) }
    val windowState = rememberWindowState(
        size = DpSize(400.dp, (Toolkit.getDefaultToolkit().screenSize.height - 100).dp),
    )
    val currentState by rememberUpdatedState(windowState)
    GlobalScreen.addNativeKeyListener(object : NativeKeyListener {
        override fun nativeKeyPressed(nativeEvent: NativeKeyEvent?) {
            if (nativeEvent == null) {
                return
            }
            when (nativeEvent.rawCode) {
                27 /* ESC */  -> currentState.isMinimized = !currentState.isMinimized
                37 /* <- */   -> currentState.position = WindowPosition(currentState.position.x - 10.dp, currentState.position.y)
                39 /* -> */   -> currentState.position = WindowPosition(currentState.position.x + 10.dp, currentState.position.y)
                38 /* up */   -> currentState.position = WindowPosition(currentState.position.x, currentState.position.y - 10.dp)
                40 /* down */ -> currentState.position = WindowPosition(currentState.position.x, currentState.position.y + 10.dp)
            }
        }
    })
    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "DB Browser",
        alwaysOnTop = true,
        onKeyEvent = { event -> when(event.key) {
            Key.F12 -> {
                settingsDialogVisible = true
                true
            }
            else -> false
        } },
        focusable = false,
    ) {

        App(scrollState)

        Dialog(
            onCloseRequest = { settingsDialogVisible = false },
            visible = settingsDialogVisible,
            title = "settings",
        ) {
            val dialogScrollState = rememberScrollState()
            Column(
                modifier = Modifier.verticalScroll(dialogScrollState),
            ) {
                var text1 by remember { mutableStateOf(PREFIX_VOICE ?: "") }
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = text1,
                    onValueChange = { PREFIX_VOICE = it; text1 = it },
                    textStyle = TextStyle(fontSize = 10.sp),
                )
                var text2 by remember { mutableStateOf(PREFIX_ALGO ?: "") }
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = text2,
                    onValueChange = { PREFIX_ALGO = it; text2 = it },
                    textStyle = TextStyle(fontSize = 16.sp),
                )
            }
        }
    }
}

@Composable
@Preview
fun App(scrollState: ScrollState) {
    MaterialTheme {
        Column {
            val content by GPTEngine.contentFlow.collectAsState()
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray),
                text = content,
                fontSize = 10.sp,
            )
            val message by GPTEngine.messageFlow.collectAsState()
            Text(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                text = message,
                fontSize = 16.sp,
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

private fun adaptScroll(scrollState: ScrollState) {
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
                val text = AipManager.pcmAsr(data)
                GPTEngine.forward(text)
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
            GPTEngine.algo(ocrString)
        }
    })
}


