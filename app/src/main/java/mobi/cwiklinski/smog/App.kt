package mobi.cwiklinski.smog

import android.app.Application
import com.joanzapata.iconify.Iconify
import com.joanzapata.iconify.fonts.TypiconsModule
import com.orhanobut.hawk.Hawk
import com.orhanobut.hawk.HawkBuilder
import com.orhanobut.hawk.LogLevel
import de.greenrobot.event.EventBus
import mobi.cwiklinski.bloodline.ui.extension.DelegatesExt

class App : Application() {

    val eventBus = EventBus.getDefault()

    companion object {
        var instance: App by DelegatesExt.notNullSingleValue()
        fun get(): App {
            return instance
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Iconify.with(TypiconsModule())
        Hawk.init(this)
            .setEncryptionMethod(HawkBuilder.EncryptionMethod.MEDIUM)
            .setStorage(HawkBuilder.newSharedPrefStorage(this))
            .setLogLevel(LogLevel.FULL)
            .build()
    }
}