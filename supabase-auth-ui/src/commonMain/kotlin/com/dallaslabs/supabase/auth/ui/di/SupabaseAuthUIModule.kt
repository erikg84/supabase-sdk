package com.dallaslabs.supabase.auth.ui.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

/**
 * Koin module for Supabase Auth UI SDK.
 *
 * Uses annotation-based configuration with @KoinViewModel and @Single annotations.
 * Automatically scans and registers:
 * - AuthViewModel (@KoinViewModel)
 * - AuthStateManager (@Single)
 *
 * Apps must provide their own implementations of:
 * - AuthService (authentication service)
 * - AuthCallbacks (navigation and analytics callbacks)
 * - AuthStateCallbacks (optional, for AuthStateManager)
 *
 * Usage in your app's Koin setup:
 * ```kotlin
 * startKoin {
 *     modules(
 *         SupabaseAuthUIModule().module, // From SDK
 *         myAppAuthModule()               // Your implementations
 *     )
 * }
 * ```
 */
@Module
@ComponentScan("com.dallaslabs.supabase.auth.ui")
public class SupabaseAuthUIModule
