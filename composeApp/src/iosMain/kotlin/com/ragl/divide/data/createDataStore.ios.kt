import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.ragl.divide.data.DATA_STORE_FILE_NAME
import com.ragl.divide.data.createDataStore
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
fun createDataStore(): DataStore<Preferences> {
    return createDataStore {
        val directory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null
        )
        requireNotNull(directory).path() + "/$DATA_STORE_FILE_NAME"
    }
}