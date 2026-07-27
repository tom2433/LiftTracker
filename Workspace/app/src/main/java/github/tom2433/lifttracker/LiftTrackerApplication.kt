package github.tom2433.lifttracker

import android.app.Application
import github.tom2433.lifttracker.data.AppContainer
import github.tom2433.lifttracker.data.AppDataContainer

/**
 * class inheriting Application to override the default Application class. The purpose of this class
 * is to supply the Context needed for the AppContainer to define its repositories.
 *
 * @property container instantiates all repositories to inject into view models
 */
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