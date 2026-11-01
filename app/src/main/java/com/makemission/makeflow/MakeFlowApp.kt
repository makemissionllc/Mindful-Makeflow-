package com.makemission.makeflow

import android.app.Application
import android.util.Log
import com.makemission.makeflow.BuildConfig
import com.makemission.makeflow.core.logging.AppLogger
import com.makemission.makeflow.core.update.GithubAppUpdateScheduler
import com.makemission.makeflow.data.local.AppDatabase
import kotlin.system.exitProcess

class MakeFlowApp : Application() {

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getDatabase(this)
        AppLogger.init(database)
        GithubAppUpdateScheduler.schedule(this)
        installCrashHandler()
        AppLogger.event(
            tag = "MakeFlowApp",
            event = "application_initialized",
            operationId = AppLogger.newOperationId(),
            details = mapOf(
                "version" to BuildConfig.VERSION_NAME,
                "build_type" to BuildConfig.BUILD_TYPE,
                "application_id" to BuildConfig.APPLICATION_ID
            )
        )
    }

    private fun installCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                AppLogger.writeSyncCrash(this, throwable)
            } catch (_: Exception) {
            }

            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, throwable)
            } else {
                Log.e("MakeFlowApp", "Uncaught exception on ${thread.name}", throwable)
                exitProcess(2)
            }
        }
    }
}
