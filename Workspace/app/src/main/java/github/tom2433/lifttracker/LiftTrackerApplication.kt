package github.tom2433.lifttracker

import android.app.Application
import github.tom2433.lifttracker.data.AppContainer
import github.tom2433.lifttracker.data.AppDataContainer

class LiftTrackerApplication : Application() {
    /**
     * AppContainer instance used by the rest of the classes to obtain dependencies
     */
    lateinit var container : AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}