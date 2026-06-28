package de.seb.einkaufszettel

import android.app.Application
import android.content.res.Configuration
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import java.io.File
import java.util.UUID

class ShoppingViewModel(application: Application) : AndroidViewModel(application) {
    private val store = ShoppingStore(File(application.filesDir, STORE_FILE_NAME))
    private val systemDarkTheme =
        (application.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    var state by mutableStateOf(store.load().asState())
        private set

    init {
        if (state.darkThemeEnabled == null) {
            state = state.copy(darkThemeEnabled = systemDarkTheme)
            persist(state)
        }
    }

    fun selectList(listId: Long) {
        if (state.lists.any { it.id == listId }) {
            update { copy(selectedListId = listId) }
        }
    }

    fun setCheckedVisibility(visibility: CheckedVisibility) {
        update { copy(checkedVisibility = visibility) }
    }

    fun setDarkThemeEnabled(enabled: Boolean) {
        update { copy(darkThemeEnabled = enabled) }
    }

    fun addList(name: String) {
        val cleaned = name.cleanedName()
        if (cleaned.isBlank()) return
        val now = currentTimestamp()
        val nextList = ShoppingList(
            id = newId(),
            name = cleaned,
            createdAt = now,
            updatedAt = now,
            sortOrder = state.lists.maxOfOrNull { it.sortOrder }?.plus(1) ?: 0,
        )
        update {
            copy(
                lists = lists + nextList,
                selectedListId = nextList.id,
            )
        }
    }

    fun renameSelectedList(name: String) {
        val cleaned = name.cleanedName()
        val listId = state.selectedListId ?: return
        if (cleaned.isBlank()) return
        update {
            copy(
                lists = lists.map { list ->
                    if (list.id == listId) {
                        list.copy(name = cleaned, updatedAt = currentTimestamp())
                    } else {
                        list
                    }
                },
            )
        }
    }

    fun deleteSelectedList() {
        val listId = state.selectedListId ?: return
        val remainingLists = state.lists.filterNot { it.id == listId }
        val remainingItems = state.items.filterNot { it.listId == listId }
        val nextLists = if (remainingLists.isNotEmpty()) {
            remainingLists.mapIndexed { index, list -> list.copy(sortOrder = index) }
        } else {
            listOf(createFallbackList())
        }
        val nextSelectedId = nextLists.first().id
        update {
            copy(
                lists = nextLists,
                items = remainingItems,
                selectedListId = nextSelectedId,
            )
        }
    }

    fun addItem(name: String, quantity: String, category: String) {
        val listId = state.selectedListId ?: return
        val cleanedName = name.cleanedName()
        if (cleanedName.isBlank()) return
        val now = currentTimestamp()
        val itemsForList = state.items.filter { it.listId == listId }
        val newItem = ShoppingItem(
            id = newId(),
            listId = listId,
            name = cleanedName,
            quantity = quantity.cleanedText(),
            category = category.cleanedText(),
            isChecked = false,
            createdAt = now,
            updatedAt = now,
            sortOrder = nextSortOrder(itemsForList),
        )
        update {
            copy(
                items = items + newItem,
                frequentItems = updateFrequentItems(frequentItems, newItem),
            )
        }
    }

    fun updateItem(itemId: Long, name: String, quantity: String, category: String) {
        val cleanedName = name.cleanedName()
        if (cleanedName.isBlank()) return
        update {
            val updatedItems = items.map { item ->
                if (item.id == itemId) {
                    item.copy(
                        name = cleanedName,
                        quantity = quantity.cleanedText(),
                        category = category.cleanedText(),
                        updatedAt = currentTimestamp(),
                    )
                } else {
                    item
                }
            }
            copy(
                items = updatedItems,
                frequentItems = updateFrequentItems(frequentItems, updatedItems.firstOrNull { it.id == itemId }),
            )
        }
    }

    fun toggleItem(itemId: Long) {
        update {
            copy(
                items = items.map { item ->
                    if (item.id == itemId) item.copy(isChecked = !item.isChecked, updatedAt = currentTimestamp()) else item
                },
            )
        }
    }

    fun deleteItem(itemId: Long) {
        update {
            copy(items = items.filterNot { it.id == itemId })
        }
    }

    fun clearCheckedItems() {
        update {
            copy(items = items.filterNot { it.isChecked })
        }
    }

    fun loadDemoData() {
        val demo = createDefaultData(currentTimestamp()).asState().copy(
            darkThemeEnabled = state.darkThemeEnabled ?: systemDarkTheme,
        )
        state = demo
        persist(demo)
    }

    fun moveOpenItem(fromIndex: Int, toIndex: Int) {
        val listId = state.selectedListId ?: return
        val openItems = state.items.filter { it.listId == listId && !it.isChecked }.sortedWith(compareBy<ShoppingItem> { it.sortOrder }.thenBy { it.createdAt })
        if (fromIndex !in openItems.indices || toIndex !in openItems.indices || fromIndex == toIndex) return

        val moved = openItems.toMutableList()
        val item = moved.removeAt(fromIndex)
        moved.add(toIndex, item)

        val newOrderById = moved.mapIndexed { index, shoppingItem -> shoppingItem.id to index }.toMap()
        update {
            copy(
                items = items.map { item ->
                    if (item.listId == listId && !item.isChecked) {
                        item.copy(sortOrder = newOrderById[item.id] ?: item.sortOrder)
                    } else {
                        item
                    }
                },
            )
        }
    }

    private fun update(transform: ShoppingAppState.() -> ShoppingAppState) {
        val next = state.transform().normalize()
        state = next
        persist(next)
    }

    private fun ShoppingAppState.normalize(): ShoppingAppState {
        val lists = if (lists.isNotEmpty()) {
            lists.sortedWith(compareBy<ShoppingList> { it.sortOrder }.thenBy { it.createdAt })
        } else {
            listOf(createFallbackList())
        }
        val listIds = lists.map { it.id }.toSet()
        val items = items.filter { it.listId in listIds }
        val selected = selectedListId?.takeIf { it in listIds } ?: lists.first().id
        val normalizedLists = lists.mapIndexed { index, list -> list.copy(sortOrder = index) }
        val openCounts = items.groupBy { it.listId }
        val normalizedItems = items.map { item ->
            val sameList = openCounts[item.listId].orEmpty().sortedWith(compareBy<ShoppingItem> { it.isChecked }.thenBy { it.sortOrder }.thenBy { it.createdAt })
            val newSortOrder = sameList.indexOfFirst { it.id == item.id }.takeIf { it >= 0 } ?: item.sortOrder
            item.copy(sortOrder = newSortOrder)
        }
        return copy(
            lists = normalizedLists,
            items = normalizedItems,
            selectedListId = selected,
        )
    }

    private fun persist(state: ShoppingAppState) {
        runCatching {
            store.save(
                ShoppingData(
                    lists = state.lists,
                    items = state.items,
                    frequentItems = state.frequentItems,
                    selectedListId = state.selectedListId,
                    checkedVisibility = state.checkedVisibility,
                    darkThemeEnabled = state.darkThemeEnabled,
                ),
            )
        }
    }

    private fun updateFrequentItems(items: List<FrequentItem>, item: ShoppingItem?): List<FrequentItem> {
        if (item == null) return items
        val key = item.name.nameKey()
        val existing = items.firstOrNull { it.name.nameKey() == key }
        return if (existing == null) {
            items + FrequentItem(
                name = item.name,
                quantity = item.quantity,
                category = item.category.ifBlank { DEFAULT_CATEGORY },
                useCount = 1,
            )
        } else {
            items.map { frequent ->
                if (frequent.name.nameKey() == key) {
                    frequent.copy(
                        name = item.name,
                        quantity = item.quantity.ifBlank { frequent.quantity },
                        category = item.category.ifBlank { frequent.category.ifBlank { DEFAULT_CATEGORY } },
                        useCount = frequent.useCount + 1,
                    )
                } else {
                    frequent
                }
            }
        }
    }

    private fun createFallbackList(): ShoppingList {
        val now = currentTimestamp()
        return ShoppingList(
            id = newId(),
            name = "Wocheneinkauf",
            createdAt = now,
            updatedAt = now,
            sortOrder = 0,
        )
    }

    private fun newId(): Long = UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE

    private companion object {
        const val STORE_FILE_NAME = "shoppingzettel.json"
    }
}
