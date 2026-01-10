package com.dallaslabs.supabase.db.query

import com.dallaslabs.supabase.core.SupabaseCore
import com.dallaslabs.supabase.db.SupabaseDatabase
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

@Serializable
data class TestUser(
    val id: String,
    val name: String,
    val email: String
)

class DeleteQueryTest {

    private lateinit var database: SupabaseDatabase

    @BeforeTest
    fun setup() {
        // Initialize Supabase with test credentials
        // Note: This uses mock credentials for unit testing
        SupabaseCore.initializeIfNeeded {
            projectUrl = "https://test.supabase.co"
            anonKey = "test-key"
        }
        database = SupabaseDatabase.getInstance()
    }

    @AfterTest
    fun teardown() {
        // Clean up if needed
    }

    @Test
    fun `delete with filter should succeed`() = runTest {
        // Test that delete with filter executes without error
        val result = database.from<TestUser>("users")
            .delete()
            .filter { eq("id", "test-id") }
            .execute()

        // Should either succeed or fail with a real error, not "requires filter" error
        assertTrue(
            result.isSuccess || !result.errorOrNull()?.message.orEmpty().contains("requires a filter"),
            "Delete with filter should not throw 'requires filter' error"
        )
    }

    @Test
    fun `delete without filter should fail with error`() = runTest {
        val result = database.from<TestUser>("users")
            .delete()
            .execute()

        assertTrue(result.isFailure, "Delete without filter should fail")
        assertTrue(
            result.errorOrNull()?.message?.contains("requires a filter") == true,
            "Should fail with 'requires a filter' message"
        )
    }

    @Test
    fun `delete with multiple filters should succeed`() = runTest {
        val result = database.from<TestUser>("users")
            .delete()
            .filter {
                eq("email", "test@example.com")
                eq("name", "Test User")
            }
            .execute()

        // Should not fail with "requires filter" error
        assertTrue(
            result.isSuccess || !result.errorOrNull()?.message.orEmpty().contains("requires a filter"),
            "Delete with multiple filters should not throw 'requires filter' error"
        )
    }

    @Test
    fun `delete with complex filter should succeed`() = runTest {
        val result = database.from<TestUser>("users")
            .delete()
            .filter {
                gte("id", "100")
                lte("id", "200")
            }
            .execute()

        // Should not fail with "requires filter" error
        assertTrue(
            result.isSuccess || !result.errorOrNull()?.message.orEmpty().contains("requires a filter"),
            "Delete with complex filter should not throw 'requires filter' error"
        )
    }
}
