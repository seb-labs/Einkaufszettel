package de.seb.einkaufszettel

import kotlin.math.max

const val DEFAULT_CATEGORY = "Sonstiges"

val DEFAULT_CATEGORIES = listOf(
    "Obst & Gemüse",
    "Kühlung",
    "Tiefkühl",
    "Brot & Backwaren",
    "Getränke",
    "Vorrat",
    "Drogerie & Haushalt",
    DEFAULT_CATEGORY,
)

enum class CheckedVisibility {
    END,
    HIDDEN,
}

data class ShoppingList(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long,
    val sortOrder: Int,
)

data class ShoppingItem(
    val id: Long,
    val listId: Long,
    val name: String,
    val quantity: String = "",
    val category: String = "",
    val isChecked: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
    val sortOrder: Int,
)

data class FrequentItem(
    val name: String,
    val quantity: String = "",
    val category: String = "",
    val useCount: Int = 1,
)

data class ShoppingAppState(
    val lists: List<ShoppingList> = emptyList(),
    val items: List<ShoppingItem> = emptyList(),
    val frequentItems: List<FrequentItem> = emptyList(),
    val selectedListId: Long? = null,
    val checkedVisibility: CheckedVisibility = CheckedVisibility.END,
    val darkThemeEnabled: Boolean? = null,
) {
    val selectedList: ShoppingList? get() = lists.firstOrNull { it.id == selectedListId }
    val selectedListName: String get() = selectedList?.name.orEmpty()

    val currentItems: List<ShoppingItem>
        get() = items.filter { it.listId == selectedListId }.sortedWith(
            compareBy<ShoppingItem> { it.isChecked }
                .thenBy { it.sortOrder }
                .thenBy { it.createdAt },
        )

    val openItems: List<ShoppingItem>
        get() = currentItems.filterNot { it.isChecked }

    val checkedItems: List<ShoppingItem>
        get() = currentItems.filter { it.isChecked }

    val currentItemCount: Int get() = currentItems.size
    val openCount: Int get() = openItems.size
    val checkedCount: Int get() = checkedItems.size

    val suggestions: List<FrequentItem>
        get() = frequentItems.sortedWith(
            compareByDescending<FrequentItem> { it.useCount }
                .thenBy { it.name.lowercase() },
        ).take(6)
}

data class ShoppingData(
    val lists: List<ShoppingList>,
    val items: List<ShoppingItem>,
    val frequentItems: List<FrequentItem>,
    val selectedListId: Long?,
    val checkedVisibility: CheckedVisibility,
    val darkThemeEnabled: Boolean? = null,
)

fun createDefaultData(now: Long): ShoppingData {
    val week1 = ShoppingList(
        id = now,
        name = "Wocheneinkauf",
        createdAt = now,
        updatedAt = now,
        sortOrder = 0,
    )
    val week2 = ShoppingList(
        id = now + 100,
        name = "Nächste Woche",
        createdAt = now + 100,
        updatedAt = now + 100,
        sortOrder = 1,
    )

    val demoItems = listOf(
        ShoppingItem(
            id = now + 1,
            listId = week1.id,
            name = "Milch",
            quantity = "2x 1L",
            category = "Kühlung",
            isChecked = false,
            createdAt = now + 1,
            updatedAt = now + 1,
            sortOrder = 0,
        ),
        ShoppingItem(
            id = now + 2,
            listId = week1.id,
            name = "Brot",
            quantity = "1 Laib",
            category = "Brot & Backwaren",
            isChecked = false,
            createdAt = now + 2,
            updatedAt = now + 2,
            sortOrder = 1,
        ),
        ShoppingItem(
            id = now + 3,
            listId = week1.id,
            name = "Eier",
            quantity = "10 Stück",
            category = "Kühlung",
            isChecked = false,
            createdAt = now + 3,
            updatedAt = now + 3,
            sortOrder = 2,
        ),
        ShoppingItem(
            id = now + 4,
            listId = week1.id,
            name = "Bananen",
            quantity = "1 Bund",
            category = "Obst & Gemüse",
            isChecked = false,
            createdAt = now + 4,
            updatedAt = now + 4,
            sortOrder = 3,
        ),
        ShoppingItem(
            id = now + 5,
            listId = week1.id,
            name = "Haferflocken",
            quantity = "1 Packung",
            category = "Vorrat",
            isChecked = false,
            createdAt = now + 5,
            updatedAt = now + 5,
            sortOrder = 4,
        ),
        ShoppingItem(
            id = now + 6,
            listId = week1.id,
            name = "Putzmittel",
            quantity = "",
            category = "Drogerie & Haushalt",
            isChecked = false,
            createdAt = now + 6,
            updatedAt = now + 6,
            sortOrder = 5,
        ),
        ShoppingItem(
            id = now + 101,
            listId = week2.id,
            name = "Kaffee",
            quantity = "1 Packung",
            category = "Vorrat",
            isChecked = false,
            createdAt = now + 101,
            updatedAt = now + 101,
            sortOrder = 0,
        ),
        ShoppingItem(
            id = now + 102,
            listId = week2.id,
            name = "Joghurt",
            quantity = "6 Becher",
            category = "Kühlung",
            isChecked = false,
            createdAt = now + 102,
            updatedAt = now + 102,
            sortOrder = 1,
        ),
        ShoppingItem(
            id = now + 103,
            listId = week2.id,
            name = "Tomaten",
            quantity = "500 g",
            category = "Obst & Gemüse",
            isChecked = false,
            createdAt = now + 103,
            updatedAt = now + 103,
            sortOrder = 2,
        ),
        ShoppingItem(
            id = now + 104,
            listId = week2.id,
            name = "Käse",
            quantity = "1 Stück",
            category = "Kühlung",
            isChecked = true,
            createdAt = now + 104,
            updatedAt = now + 104,
            sortOrder = 3,
        ),
        ShoppingItem(
            id = now + 105,
            listId = week2.id,
            name = "Müsli",
            quantity = "1 Packung",
            category = "Vorrat",
            isChecked = false,
            createdAt = now + 105,
            updatedAt = now + 105,
            sortOrder = 4,
        ),
    )

    return ShoppingData(
        lists = listOf(week1, week2),
        items = demoItems,
        frequentItems = listOf(
            FrequentItem("Milch", quantity = "2x 1L", category = "Kühlung", useCount = 4),
            FrequentItem("Brot", quantity = "1 Laib", category = "Brot & Backwaren", useCount = 3),
            FrequentItem("Bananen", quantity = "1 Bund", category = "Obst & Gemüse", useCount = 2),
            FrequentItem("Kaffee", quantity = "1 Packung", category = "Vorrat", useCount = 2),
        ),
        selectedListId = week1.id,
        checkedVisibility = CheckedVisibility.END,
        darkThemeEnabled = null,
    )
}

fun ShoppingData.asState(): ShoppingAppState = ShoppingAppState(
    lists = lists.sortedWith(compareBy<ShoppingList> { it.sortOrder }.thenBy { it.createdAt }),
    items = items.sortedWith(compareBy<ShoppingItem> { it.listId }.thenBy { it.isChecked }.thenBy { it.sortOrder }.thenBy { it.createdAt }),
    frequentItems = frequentItems,
    selectedListId = selectedListId,
    checkedVisibility = checkedVisibility,
    darkThemeEnabled = darkThemeEnabled,
)

fun String.cleanedText(): String = trim().replace(Regex("\\s+"), " ")

fun String.cleanedName(): String = cleanedText()

fun String.nameKey(): String = cleanedText().lowercase()

fun currentTimestamp(): Long = System.currentTimeMillis()

fun nextSortOrder(existing: List<ShoppingItem>): Int = max(existing.maxOfOrNull { it.sortOrder } ?: -1, -1) + 1
