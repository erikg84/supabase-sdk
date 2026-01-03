package com.dallaslabs.supabase.core

/**
 * Platform identifier for Android.
 */
public actual val platform: Platform = Platform.Android

/**
 * Platform enumeration.
 */
public actual enum class Platform {
    Android,
    IOS
}
