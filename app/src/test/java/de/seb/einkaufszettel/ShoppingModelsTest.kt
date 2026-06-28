package de.seb.einkaufszettel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShoppingModelsTest {
    @Test
    fun cleanedTextNormalizesWhitespace() {
        assertEquals("Milch 1L", "  Milch   1L  ".cleanedText())
    }

    @Test
    fun defaultDataCreatesWocheneinkaufList() {
        val data = createDefaultData(1234L)
        assertEquals(1, data.lists.size)
        assertEquals("Wocheneinkauf", data.lists.first().name)
        assertEquals(data.lists.first().id, data.selectedListId)
        assertTrue(data.items.isNotEmpty())
        assertTrue(data.frequentItems.isNotEmpty())
    }

    @Test
    fun appStateSortsSuggestionsByUsage() {
        val state = ShoppingAppState(
            frequentItems = listOf(
                FrequentItem("Brot", useCount = 2),
                FrequentItem("Milch", useCount = 5),
            ),
        )
        assertEquals("Milch", state.suggestions.first().name)
        assertTrue(state.suggestions.size <= 2)
    }
}
