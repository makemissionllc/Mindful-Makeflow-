package com.makemission.makeflow.core.logging

import android.content.Context
import com.makemission.makeflow.data.local.AppDatabase
import com.makemission.makeflow.data.local.entity.AppLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object AppLogger {

    private const val MAX_DB_ENTRIES = 1000
    private const val MAX_EXPORTED_ENTRIES = 500
    private const val MAX_REPORT_CHARACTERS = 350_000
    private const val CRASH_FILE_NAME = "crash_log.txt"

    private var appLogDao: com.makemission.makeflow.data.local.dao.AppLogDao? = null
    @Volatile private var processSessionId: String = UUID.randomUUID().toString().take(12)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun init(database: AppDatabase) {
        appLogDao = database.appLogDao()
        processSessionId = UUID.randomUUID().toString().take(12)
        pruneStale()
        event(
            tag = "AppLogger",
            event = "diagnostic_session_started",
            operationId = newOperationId()
        )
    }

    fun newOperationId(): String = UUID.randomUUID().toString().take(8)

    fun hashIdentifier(value: String?): String? {
        if (value.isNullOrBlank()) return null
        return MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .take(8)
            .joinToString("") { byte -> "%02x".format(Locale.ROOT, byte.toInt() and 0xff) }
    }

    fun event(
        tag: String,
        event: String,
        operationId: String,
        details: Map<String, Any?> = emptyMap(),
        level: String = "INFO"
    ) {
        val safeLevel = level.uppercase(Locale.ROOT).takeIf { it in setOf("INFO", "WARN", "ERROR") } ?: "INFO"
        val fields = details.toSortedMap().entries.joinToString(" ") { (key, value) ->
            "${formatField(key)}=\"${formatField(value?.toString() ?: "null")}\""
        }
        val message = buildString {
            append("event=").append(formatField(event))
            append(" operation=").append(formatField(operationId))
            if (fields.isNotEmpty()) append(' ').append(fields)
        }
        writeAsync(safeLevel, tag, message)
    }

    fun info(tag: String, message: String) {
        writeAsync("INFO", tag, message)
    }

    fun warn(tag: String, message: String) {
        writeAsync("WARN", tag, message)
    }

    fun error(tag: String, message: String, throwable: Throwable? = null) {
        val stackTrace = throwable?.let { formatStackTrace(it) }
        writeAsync("ERROR", tag, message, stackTrace)
    }

    fun writeSync(level: String, tag: String, message: String, stackTrace: String? = null) {
        val threadName = Thread.currentThread().name
        val log = AppLogEntity(
            timestamp = System.currentTimeMillis(),
            level = level,
            tag = tag,
            message = contextualMessage(message, threadName),
            stackTrace = stackTrace
        )
        try {
            appLogDao?.insertSync(log)
        } catch (_: Exception) {
        }
    }

    fun writeSyncCrash(context: Context, throwable: Throwable) {
        val timestamp = System.currentTimeMillis()
        val stackTrace = formatStackTrace(throwable)
        val formatted = buildString {
            appendLine("=== CRASH LOG ===")
            appendLine("Timestamp: ${formatTimestamp(timestamp)}")
            appendLine("Session: $processSessionId")
            appendLine("Thread: ${Thread.currentThread().name}")
            appendLine("Exception: ${throwable.javaClass.name}: ${throwable.message}")
            appendLine("Stack Trace:")
            appendLine(stackTrace)
            appendLine("=== END CRASH LOG ===")
            appendLine()
        }

        try {
            val file = File(context.filesDir, CRASH_FILE_NAME)
            file.appendText(formatted)
        } catch (_: Exception) {
        }

        writeSync("ERROR", "Crash", "${throwable.javaClass.name}: ${throwable.message}", stackTrace)
    }

    suspend fun getCrashLogContent(context: Context): String? {
        return try {
            val file = File(context.filesDir, CRASH_FILE_NAME)
            if (file.exists()) file.readText() else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun buildDiagnosticReport(context: Context): String {
        val logs = try {
            appLogDao?.getAll().orEmpty().sortedBy { it.timestamp }.takeLast(MAX_EXPORTED_ENTRIES)
        } catch (_: Exception) {
            emptyList()
        }
        val packageInfo = runCatching {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0)
        }.getOrNull()
        val versionName = packageInfo?.versionName ?: "unknown"
        val versionCode = packageInfo?.let {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                it.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                it.versionCode.toLong()
            }
        } ?: -1L
        val crashContent = getCrashLogContent(context)

        return buildString {
            appendLine("MakeFlow diagnostic report")
            appendLine("Generated: ${formatTimestamp(System.currentTimeMillis())}")
            appendLine("App: $versionName ($versionCode)")
            appendLine("Android: ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})")
            appendLine("Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
            appendLine("Current session: $processSessionId")
            appendLine("Entries: ${logs.size} latest local events")
            appendLine("Imported message bodies are excluded. Structured import events hash transaction references and account IDs; review the report before sharing.")
            appendLine()
            logs.forEach { log ->
                append(formatTimestamp(log.timestamp))
                append(" [").append(log.level).append("] [").append(log.tag).append("] ")
                appendLine(log.message)
                log.stackTrace?.takeIf(String::isNotBlank)?.let { trace ->
                    appendLine(trace.take(12_000))
                }
            }
            crashContent?.takeIf(String::isNotBlank)?.let { content ->
                appendLine()
                appendLine("Crash file")
                appendLine(content.take(40_000))
            }
        }.take(MAX_REPORT_CHARACTERS)
    }

    fun clearCrashFile(context: Context) {
        try {
            val file = File(context.filesDir, CRASH_FILE_NAME)
            if (file.exists()) file.delete()
        } catch (_: Exception) {
        }
    }

    private fun writeAsync(level: String, tag: String, message: String, stackTrace: String? = null) {
        val threadName = Thread.currentThread().name
        val sessionId = processSessionId
        scope.launch {
            val log = AppLogEntity(
                timestamp = System.currentTimeMillis(),
                level = level,
                tag = tag,
                message = contextualMessage(message, threadName, sessionId),
                stackTrace = stackTrace
            )
            try {
                appLogDao?.insert(log)
                pruneIfNeeded()
            } catch (_: Exception) {
            }
        }
    }

    private suspend fun pruneIfNeeded() {
        try {
            val dao = appLogDao ?: return
            val count = dao.count()
            if (count > MAX_DB_ENTRIES) {
                dao.pruneToMostRecent(MAX_DB_ENTRIES)
            }
        } catch (_: Exception) {
        }
    }

    private fun pruneStale() {
        scope.launch {
            try {
                val dao = appLogDao ?: return@launch
                val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
                dao.deleteOlderThan(thirtyDaysAgo)
                pruneIfNeeded()
            } catch (_: Exception) {
            }
        }
    }

    private fun formatStackTrace(throwable: Throwable): String {
        return throwable.stackTraceToString()
    }

    private fun contextualMessage(
        message: String,
        threadName: String,
        sessionId: String = processSessionId
    ): String = "session=$sessionId thread=${formatField(threadName)} $message"

    private fun formatField(value: String): String = value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\r", " ")
        .replace("\n", " ")

    fun formatTimestamp(timestamp: Long): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date(timestamp))
    }
}
