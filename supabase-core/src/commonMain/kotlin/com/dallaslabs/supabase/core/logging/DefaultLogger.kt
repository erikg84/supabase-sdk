package com.dallaslabs.supabase.core.logging

/**
 * Default logger implementation using println.
 * Used when no custom logger is provided and logging is enabled.
 *
 * For production use, consider implementing [SupabaseLogger] with:
 * - Android: android.util.Log
 * - iOS: os_log or print
 */
internal class DefaultLogger : SupabaseLogger {

    override fun debug(tag: String, message: String) {
        println("D/$tag: $message")
    }

    override fun info(tag: String, message: String) {
        println("I/$tag: $message")
    }

    override fun warn(tag: String, message: String) {
        println("W/$tag: $message")
    }

    override fun error(tag: String, message: String, throwable: Throwable?) {
        println("E/$tag: $message")
        throwable?.printStackTrace()
    }
}
