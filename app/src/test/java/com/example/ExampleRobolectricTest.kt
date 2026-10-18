package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.AiOrganizationEngine
import com.example.data.model.ItemType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("QuickPilot AI", appName)
    }

    @Test
    fun `test compound natural language capture parsing`() {
        val input = "Remind me to finish my physics assignment tomorrow at 6 PM, buy milk after college, and call Rahul on Sunday."
        val items = AiOrganizationEngine.parseWithRuleEngine(input)

        // Should extract 3 distinct items
        assertEquals(3, items.size)

        // Item 1: Physics assignment (Study/Task/Reminder)
        assertTrue(items[0].title.contains("Physics", ignoreCase = true))
        assertNotNull(items[0].timeString)

        // Item 2: Buy milk (Shopping)
        assertEquals(ItemType.SHOPPING, items[1].itemType)
        assertTrue(items[1].title.contains("milk", ignoreCase = true))

        // Item 3: Call Rahul (Reminder)
        assertEquals(ItemType.REMINDER, items[2].itemType)
        assertTrue(items[2].title.contains("Rahul", ignoreCase = true))
    }

    @Test
    fun `test shopping list parsing`() {
        val input = "Milk, whole wheat bread, eggs, apples"
        val items = AiOrganizationEngine.parseWithRuleEngine(input)

        assertEquals(1, items.size)
        assertEquals(ItemType.SHOPPING, items[0].itemType)
        assertEquals(4, items[0].checklist.size)
    }

    @Test
    fun `test wifi bill note parsing`() {
        val input = "Remember that my Wi-Fi bill is ₹799"
        val items = AiOrganizationEngine.parseWithRuleEngine(input)

        assertEquals(1, items.size)
        assertEquals(ItemType.NOTE, items[0].itemType)
        assertEquals("Finance", items[0].category)
    }
}
