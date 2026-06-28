package de.seb.einkaufszettel

import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.io.File

class ShoppingStore(private val file: File) {
    fun load(): ShoppingData = runCatching {
        if (!file.exists()) return createDefaultData(currentTimestamp())

        val raw = file.readText()
        val parsed = when (val value = JSONTokener(raw).nextValue()) {
            is JSONObject -> value
            else -> null
        } ?: return createDefaultData(currentTimestamp())

        val lists = parsed.optJSONArray(KEY_LISTS)?.toShoppingLists().orEmpty()
        val items = parsed.optJSONArray(KEY_ITEMS)?.toShoppingItems().orEmpty()
        val frequentItems = parsed.optJSONArray(KEY_FREQUENT)?.toFrequentItems().orEmpty()
        val customCategories = parsed.optJSONArray(KEY_CUSTOM_CATEGORIES)?.toCustomCategories().orEmpty()
        val selectedListId = parsed.optLong(KEY_SELECTED_LIST_ID, -1L).takeIf { it > 0 }
        val visibility = runCatching {
            CheckedVisibility.valueOf(parsed.optString(KEY_CHECKED_VISIBILITY, CheckedVisibility.END.name))
        }.getOrDefault(CheckedVisibility.END)

        val normalized = normalize(
            ShoppingData(
                lists = lists,
                items = items,
                frequentItems = frequentItems,
                customCategories = customCategories,
                selectedListId = selectedListId,
                checkedVisibility = visibility,
            ),
        )
        if (normalized.shouldSeedDemoData()) createDefaultData(currentTimestamp()) else normalized
    }.getOrElse {
        createDefaultData(currentTimestamp())
    }

    fun save(data: ShoppingData) {
        file.parentFile?.mkdirs()
        file.writeText(data.toJson().toString())
    }

    private fun normalize(data: ShoppingData): ShoppingData {
        val now = currentTimestamp()
        val lists = data.lists.sortedWith(compareBy<ShoppingList> { it.sortOrder }.thenBy { it.createdAt })
        val listIds = lists.map { it.id }.toSet()
        val validItems = data.items.filter { it.listId in listIds }
        val selectedListId = data.selectedListId?.takeIf { it in listIds } ?: lists.firstOrNull()?.id
            ?: return createDefaultData(now)
        val categories = data.customCategories.map { it.cleanedText() }.filter { it.isNotBlank() }

        return data.copy(
            lists = lists,
            items = validItems,
            selectedListId = selectedListId,
            checkedVisibility = data.checkedVisibility,
            customCategories = categories,
        )
    }

    private fun ShoppingData.shouldSeedDemoData(): Boolean =
        lists.size <= 1 &&
            items.isEmpty() &&
            frequentItems.isEmpty()

    private fun ShoppingData.toJson(): JSONObject = JSONObject()
        .put(KEY_CUSTOM_CATEGORIES, JSONArray().also { arr -> customCategories.forEach { arr.put(it) } })
        .put(KEY_SELECTED_LIST_ID, selectedListId ?: JSONObject.NULL)
        .put(KEY_CHECKED_VISIBILITY, checkedVisibility.name)
        .put(KEY_LISTS, JSONArray().also { arr -> lists.forEach { arr.put(it.toJson()) } })
        .put(KEY_ITEMS, JSONArray().also { arr -> items.forEach { arr.put(it.toJson()) } })
        .put(KEY_FREQUENT, JSONArray().also { arr -> frequentItems.forEach { arr.put(it.toJson()) } })

    private fun ShoppingList.toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("name", name)
        .put("createdAt", createdAt)
        .put("updatedAt", updatedAt)
        .put("sortOrder", sortOrder)

    private fun ShoppingItem.toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("listId", listId)
        .put("name", name)
        .put("quantity", quantity)
        .put("category", category)
        .put("isChecked", isChecked)
        .put("createdAt", createdAt)
        .put("updatedAt", updatedAt)
        .put("sortOrder", sortOrder)

    private fun FrequentItem.toJson(): JSONObject = JSONObject()
        .put("name", name)
        .put("quantity", quantity)
        .put("category", category)
        .put("useCount", useCount)

    private fun JSONArray.toCustomCategories(): List<String> = buildList {
        for (index in 0 until length()) {
            add(getString(index).cleanedText())
        }
    }.filter { it.isNotBlank() }

    private fun JSONArray.toShoppingLists(): List<ShoppingList> = buildList {
        for (index in 0 until length()) {
            val obj = getJSONObject(index)
            add(
                ShoppingList(
                    id = obj.optLong("id", currentTimestamp() + index),
                    name = obj.optString("name", "").cleanedName(),
                    createdAt = obj.optLong("createdAt", currentTimestamp()),
                    updatedAt = obj.optLong("updatedAt", currentTimestamp()),
                    sortOrder = obj.optInt("sortOrder", index),
                ),
            )
        }
    }.filter { it.name.isNotBlank() }

    private fun JSONArray.toShoppingItems(): List<ShoppingItem> = buildList {
        for (index in 0 until length()) {
            val obj = getJSONObject(index)
            add(
                ShoppingItem(
                    id = obj.optLong("id", currentTimestamp() + index),
                    listId = obj.optLong("listId", -1L),
                    name = obj.optString("name", "").cleanedName(),
                    quantity = obj.optString("quantity", "").cleanedText(),
                    category = obj.optString("category", "").cleanedText(),
                    isChecked = obj.optBoolean("isChecked", false),
                    createdAt = obj.optLong("createdAt", currentTimestamp()),
                    updatedAt = obj.optLong("updatedAt", currentTimestamp()),
                    sortOrder = obj.optInt("sortOrder", index),
                ),
            )
        }
    }.filter { it.name.isNotBlank() && it.listId > 0 }

    private fun JSONArray.toFrequentItems(): List<FrequentItem> = buildList {
        for (index in 0 until length()) {
            val obj = getJSONObject(index)
            add(
                FrequentItem(
                    name = obj.optString("name", "").cleanedName(),
                    quantity = obj.optString("quantity", "").cleanedText(),
                    category = obj.optString("category", "").cleanedText(),
                    useCount = obj.optInt("useCount", 1).coerceAtLeast(1),
                ),
            )
        }
    }.filter { it.name.isNotBlank() }

    private companion object {
        const val KEY_LISTS = "lists"
        const val KEY_ITEMS = "items"
        const val KEY_FREQUENT = "frequentItems"
        const val KEY_CUSTOM_CATEGORIES = "customCategories"
        const val KEY_SELECTED_LIST_ID = "selectedListId"
        const val KEY_CHECKED_VISIBILITY = "checkedVisibility"
    }
}
