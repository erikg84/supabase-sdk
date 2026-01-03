package com.dallaslabs.supabase.core

/**
 * Platform identifier for iOS.
 */
public actual val platform: Platform = Platform.IOS

/**
 * Platform enumeration.
 */
public actual enum class Platform {
    Android,
    IOS
}
