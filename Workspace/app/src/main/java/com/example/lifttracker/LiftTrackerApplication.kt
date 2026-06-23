package com.example.lifttracker

import android.app.Application
import com.example.lifttracker.data.AppContainer
import com.example.lifttracker.data.AppDataContainer

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