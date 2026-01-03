package com.dallaslabs.supabase.core

/**
 * The current platform.
 */
public expect val platform: Platform

/**
 * Platform enumeration.
 */
public expect enum class Platform {
    Android,
    IOS
}
