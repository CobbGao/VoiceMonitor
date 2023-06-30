import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

object ApplicationDefaultScope: CoroutineScope {
    private const val TAG = "GlobalMainScope"

    override val coroutineContext: CoroutineContext
        get() = SupervisorJob() + Dispatchers.Default + CoroutineExceptionHandler { context, throwable ->
            println("[$TAG] GOT AN UNHANDLED EXCEPTION: $throwable.")
            throwable.printStackTrace()
        }
}