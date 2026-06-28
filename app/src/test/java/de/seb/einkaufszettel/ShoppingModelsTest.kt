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
    fun defaultDataCreatesTwoDemoWeeks() {
        val data = createDefaultData(1234L)
        assertEquals(2, data.lists.size)
        assertEquals("Wocheneinkauf", data.lists.first().name)
        assertEquals("Nächste Woche", data.lists[1].name)
        assertEquals(data.lists.first().id, data.selectedListId)
        assertTrue(data.items.any { it.listId == data.lists.first().id })
        assertTrue(data.items.any { it.listId == data.lists[1].id })
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
