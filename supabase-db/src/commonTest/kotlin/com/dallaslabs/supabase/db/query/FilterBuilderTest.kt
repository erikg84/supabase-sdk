package com.dallaslabs.supabase.db.query

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FilterBuilderTest {

    @Test
    fun `FilterBuilder should store eq operation`() {
        val builder = FilterBuilder()
        builder.eq("column", "value")

        assertEquals(1, builder.operations.size)
        assertTrue(builder.operations[0] is FilterOp.Eq)
        val op = builder.operations[0] as FilterOp.Eq
        assertEquals("column", op.column)
        assertEquals("value", op.value)
    }

    @Test
    fun `FilterBuilder should store multiple operations`() {
        val builder = FilterBuilder()
        builder.eq("id", "123")
        builder.gte("age", 18)
        builder.like("name", "%John%")

        assertEquals(3, builder.operations.size)
        assertTrue(builder.operations[0] is FilterOp.Eq)
        assertTrue(builder.operations[1] is FilterOp.Gte)
        assertTrue(builder.operations[2] is FilterOp.Like)
    }

    @Test
    fun `FilterBuilder should store all filter operation types`() {
        val builder = FilterBuilder()

        builder.eq("col1", "val1")
        builder.neq("col2", "val2")
        builder.gt("col3", 10)
        builder.gte("col4", 20)
        builder.lt("col5", 30)
        builder.lte("col6", 40)
        builder.like("col7", "pattern")
        builder.ilike("col8", "PATTERN")
        builder.exact("col9", true)
        builder.inList("col10", listOf(1, 2, 3))

        assertEquals(10, builder.operations.size)
    }

    @Test
    fun `FilterBuilder should handle null values`() {
        val builder = FilterBuilder()
        builder.eq("nullable_column", null)
        builder.isNull("another_null")

        assertEquals(2, builder.operations.size)
        val eqOp = builder.operations[0] as FilterOp.Eq
        assertEquals(null, eqOp.value)

        val exactOp = builder.operations[1] as FilterOp.Exact
        assertEquals(null, exactOp.value)
    }

    @Test
    fun `FilterBuilder DSL should work in block`() {
        val builder = FilterBuilder().apply {
            eq("id", "test-id")
            gte("age", 18)
            like("name", "%test%")
        }

        assertEquals(3, builder.operations.size)
    }
}
