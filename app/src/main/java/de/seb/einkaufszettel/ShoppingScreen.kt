package de.seb.einkaufszettel

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ShoppingScreen(
    state: ShoppingAppState,
    onSelectList: (Long) -> Unit,
    onSetCheckedVisibility: (CheckedVisibility) -> Unit,
    onSetDarkThemeEnabled: (Boolean) -> Unit,
    onAddList: (String) -> Unit,
    onRenameList: (String) -> Unit,
    onDeleteList: () -> Unit,
    onAddItem: (String, String, String) -> Unit,
    onUpdateItem: (Long, String, String, String) -> Unit,
    onToggleItem: (Long) -> Unit,
    onDeleteItem: (Long) -> Unit,
    onDeleteSuggestion: (String) -> Unit,
    onClearCheckedItems: () -> Unit,
    onLoadDemoData: () -> Unit,
    onMoveOpenItem: (Int, Int) -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    var showListMenu by remember { mutableStateOf(false) }
    var showAddListDialog by remember { mutableStateOf(false) }
    var showRenameListDialog by remember { mutableStateOf(false) }
    var showDeleteListConfirm by remember { mutableStateOf(false) }
    var showClearCheckedConfirm by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showLoadDemoConfirm by remember { mutableStateOf(false) }
    var showAddItemDialog by remember { mutableStateOf(false) }
    var editItem by remember { mutableStateOf<ShoppingItem?>(null) }

    val openListState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Einkaufszettel", fontWeight = FontWeight.SemiBold)
                        Text(
                            text = state.selectedListName.ifBlank { "" },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menü")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Neue Liste") },
                            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                showAddListDialog = true
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Aktuelle Liste umbenennen") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                showRenameListDialog = true
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Aktuelle Liste löschen") },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                showDeleteListConfirm = true
                            },
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (state.checkedVisibility == CheckedVisibility.END) {
                                        "Erledigte ausblenden"
                                    } else {
                                        "Erledigte unten anzeigen"
                                    },
                                )
                            },
                            onClick = {
                                showMenu = false
                                onSetCheckedVisibility(
                                    if (state.checkedVisibility == CheckedVisibility.END) {
                                        CheckedVisibility.HIDDEN
                                    } else {
                                        CheckedVisibility.END
                                    },
                                )
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Erledigte Artikel entfernen") },
                            leadingIcon = { Icon(Icons.Default.DeleteSweep, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                showClearCheckedConfirm = true
                            },
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (state.darkThemeEnabled == true) {
                                        "Helles Design aktivieren"
                                    } else {
                                        "Dunkles Design aktivieren"
                                    },
                                )
                            },
                            onClick = {
                                showMenu = false
                                onSetDarkThemeEnabled(state.darkThemeEnabled != true)
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Demo-Daten laden") },
                            leadingIcon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                showLoadDemoConfirm = true
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Info") },
                            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                showInfoDialog = true
                            },
                        )
                    }
                },
            )
        },
        bottomBar = {
            Surface(tonalElevation = 2.dp) {
                Button(
                    onClick = { showAddItemDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .height(54.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Artikel hinzufügen")
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ListSummaryCard(
                state = state,
                onClick = { showListMenu = true },
            )
            DropdownMenu(expanded = showListMenu, onDismissRequest = { showListMenu = false }) {
                state.lists.forEach { list ->
                    DropdownMenuItem(
                        text = { Text(list.name) },
                        onClick = {
                            showListMenu = false
                            onSelectList(list.id)
                        },
                    )
                }
            }

            if (state.selectedList == null) {
                EmptyStateCard(
                    title = "Keine Liste verfügbar",
                    description = "Lege eine neue Liste an, damit du direkt loslegen kannst.",
                    actionLabel = "Liste anlegen",
                    onAction = { showAddListDialog = true },
                )
            } else {
                if (state.currentItemCount == 0) {
                    EmptyStateCard(
                        title = "Noch keine Artikel",
                        description = "Füge oben direkt den ersten Einkauf hinzu.",
                        actionLabel = "Artikel hinzufügen",
                        onAction = { showAddItemDialog = true },
                    )
                } else {
                    if (state.openItems.isNotEmpty()) {
                        SectionHeader(
                            title = "Offen",
                            subtitle = "${state.openCount} Positionen",
                        )
                        OpenItemList(
                            items = state.openItems,
                            listState = openListState,
                            onMoveOpenItem = onMoveOpenItem,
                            onToggleItem = onToggleItem,
                            onEditItem = { editItem = it },
                            onDeleteItem = onDeleteItem,
                        )
                    }

                    if (state.checkedVisibility == CheckedVisibility.END && state.checkedItems.isNotEmpty()) {
                        SectionHeader(
                            title = "Erledigt",
                            subtitle = "${state.checkedCount} Positionen",
                        )
                        CheckedItemList(
                            items = state.checkedItems,
                            onToggleItem = onToggleItem,
                            onEditItem = { editItem = it },
                            onDeleteItem = onDeleteItem,
                        )
                    }

                    if (state.checkedVisibility == CheckedVisibility.HIDDEN && state.checkedItems.isNotEmpty()) {
                        CompactHintCard(
                            text = "Erledigte Artikel sind ausgeblendet.",
                        )
                    }
                }
            }
        }
    }

    if (showAddListDialog) {
        ListEditorDialog(
            title = "Neue Liste",
            initialValue = "",
            confirmLabel = "Anlegen",
            onDismiss = { showAddListDialog = false },
            onConfirm = {
                onAddList(it)
                showAddListDialog = false
            },
        )
    }

    if (showRenameListDialog) {
        ListEditorDialog(
            title = "Liste umbenennen",
            initialValue = state.selectedListName,
            confirmLabel = "Speichern",
            onDismiss = { showRenameListDialog = false },
            onConfirm = {
                onRenameList(it)
                showRenameListDialog = false
            },
        )
    }

    if (showDeleteListConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteListConfirm = false },
            title = { Text("Liste löschen?") },
            text = { Text("Die aktuelle Liste wird entfernt. Die enthaltenen Artikel verschwinden ebenfalls.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteList()
                        showDeleteListConfirm = false
                    },
                ) { Text("Löschen") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteListConfirm = false }) { Text("Abbrechen") }
            },
        )
    }

    if (showClearCheckedConfirm) {
        AlertDialog(
            onDismissRequest = { showClearCheckedConfirm = false },
            title = { Text("Erledigte Artikel entfernen?") },
            text = { Text("Alle abgehakten Artikel der aktuellen Liste werden endgültig gelöscht.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearCheckedItems()
                        showClearCheckedConfirm = false
                    },
                ) { Text("Entfernen") }
            },
            dismissButton = {
                TextButton(onClick = { showClearCheckedConfirm = false }) { Text("Abbrechen") }
            },
        )
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("Über die App") },
            text = {
                Text(
                    "Die Daten werden nur lokal auf dem Gerät gespeichert. Es gibt keine Anmeldung, keine Cloud und keine zusätzlichen Berechtigungen.",
                )
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) { Text("OK") }
            },
        )
    }

    if (showLoadDemoConfirm) {
        AlertDialog(
            onDismissRequest = { showLoadDemoConfirm = false },
            title = { Text("Demo-Daten laden?") },
            text = { Text("Dabei werden deine aktuellen lokalen Daten durch die Demo-Wochen ersetzt.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onLoadDemoData()
                        showLoadDemoConfirm = false
                    },
                ) { Text("Laden") }
            },
            dismissButton = {
                TextButton(onClick = { showLoadDemoConfirm = false }) { Text("Abbrechen") }
            },
        )
    }

    if (showAddItemDialog) {
        ShoppingItemDialog(
            title = "Artikel hinzufügen",
            confirmLabel = "Hinzufügen",
            suggestions = state.suggestions,
            categoryOptions = state.categoryOptions,
            onDeleteSuggestion = onDeleteSuggestion,
            onDismiss = { showAddItemDialog = false },
            onConfirm = { name, quantity, category ->
                onAddItem(name, quantity, category)
                showAddItemDialog = false
            },
        )
    }

    editItem?.let { item ->
        ShoppingItemDialog(
            title = "Artikel bearbeiten",
            confirmLabel = "Speichern",
            initialName = item.name,
            initialQuantity = item.quantity,
            initialCategory = item.category,
            suggestions = state.suggestions,
            categoryOptions = state.categoryOptions,
            onDeleteSuggestion = onDeleteSuggestion,
            onDismiss = { editItem = null },
            onConfirm = { name, quantity, category ->
                onUpdateItem(item.id, name, quantity, category)
                editItem = null
            },
        )
    }
}

@Composable
private fun ListSummaryCard(
    state: ShoppingAppState,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.selectedListName.ifBlank { "Einkaufslisten" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${state.lists.size} Listen · ${state.currentItemCount} Artikel",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                IconButton(onClick = onClick, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Liste auswählen")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SuggestionChip(
                    onClick = onClick,
                    label = { Text("Wechseln") },
                )
                SuggestionChip(
                    onClick = onClick,
                    label = { Text("${state.openCount} offen") },
                )
                SuggestionChip(
                    onClick = onClick,
                    label = { Text("${state.checkedCount} erledigt") },
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private sealed interface OpenRow {
    data class Header(val category: String, val count: Int) : OpenRow
    data class Item(val item: ShoppingItem, val openIndex: Int) : OpenRow
}

@Composable
private fun OpenItemList(
    items: List<ShoppingItem>,
    listState: LazyListState,
    onMoveOpenItem: (Int, Int) -> Unit,
    onToggleItem: (Long) -> Unit,
    onEditItem: (ShoppingItem) -> Unit,
    onDeleteItem: (Long) -> Unit,
) {
    var draggingItemId by remember { mutableStateOf<Long?>(null) }
    var draggingRowIndex by remember { mutableStateOf<Int?>(null) }
    var draggedDistance by remember { mutableFloatStateOf(0f) }

    val rows = remember(items) {
        buildList {
            var openIndex = 0
            items.groupBy { it.categoryDisplayName() }.forEach { (category, groupedItems) ->
                add(OpenRow.Header(category, groupedItems.size))
                groupedItems.forEach { item ->
                    add(OpenRow.Item(item = item, openIndex = openIndex++))
                }
            }
        }
    }

    LaunchedEffect(items, rows) {
        if (draggingItemId != null) {
            val current = draggingItemId
            draggingRowIndex = rows.indexOfFirst { row -> row is OpenRow.Item && row.item.id == current }
                .takeIf { it >= 0 }
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(bottom = 2.dp),
    ) {
        itemsIndexed(rows, key = { index, row ->
            when (row) {
                is OpenRow.Header -> "header-${row.category}-$index"
                is OpenRow.Item -> row.item.id
            }
        }) { rowIndex, row ->
            when (row) {
                is OpenRow.Header -> SectionHeader(
                    title = row.category,
                    subtitle = "${row.count} Artikel",
                )
                is OpenRow.Item -> {
                    val dragging = draggingItemId == row.item.id
                    ShoppingItemCard(
                        item = row.item,
                        checked = false,
                        showDragHandle = true,
                        isDragging = dragging,
                        dragOffsetY = if (dragging) draggedDistance.roundToInt() else 0,
                        onToggle = onToggleItem,
                        onEdit = { onEditItem(row.item) },
                        onDelete = onDeleteItem,
                        onDragStart = {
                            draggingItemId = row.item.id
                            draggingRowIndex = rowIndex
                            draggedDistance = 0f
                        },
                        onDrag = { delta ->
                            val currentRowIndex = draggingRowIndex ?: return@ShoppingItemCard
                            val currentRow = rows.getOrNull(currentRowIndex) as? OpenRow.Item ?: return@ShoppingItemCard
                            draggedDistance += delta
                            val draggedInfo = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == currentRowIndex }
                                ?: return@ShoppingItemCard
                            val draggedCenter = draggedInfo.offset + draggedInfo.size / 2 + draggedDistance
                            val targetInfo = listState.layoutInfo.visibleItemsInfo.firstOrNull { info ->
                                info.index != currentRowIndex && draggedCenter > info.offset && draggedCenter < info.offset + info.size
                            } ?: return@ShoppingItemCard
                            val targetRow = rows.getOrNull(targetInfo.index) as? OpenRow.Item ?: return@ShoppingItemCard
                            onMoveOpenItem(currentRow.openIndex, targetRow.openIndex)
                            draggingRowIndex = targetInfo.index
                            draggedDistance = 0f
                        },
                        onDragEnd = {
                            draggingItemId = null
                            draggingRowIndex = null
                            draggedDistance = 0f
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun CheckedItemList(
    items: List<ShoppingItem>,
    onToggleItem: (Long) -> Unit,
    onEditItem: (ShoppingItem) -> Unit,
    onDeleteItem: (Long) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.forEach { item ->
            ShoppingItemCard(
                item = item,
                checked = true,
                showDragHandle = false,
                isDragging = false,
                dragOffsetY = 0,
                onToggle = onToggleItem,
                onEdit = { onEditItem(item) },
                onDelete = onDeleteItem,
                onDragStart = {},
                onDrag = {},
                onDragEnd = {},
            )
        }
    }
}

@Composable
private fun ShoppingItemCard(
    item: ShoppingItem,
    checked: Boolean,
    showDragHandle: Boolean,
    isDragging: Boolean,
    dragOffsetY: Int,
    onToggle: (Long) -> Unit,
    onEdit: () -> Unit,
    onDelete: (Long) -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
) {
    val elevation by animateFloatAsState(if (isDragging) 8f else 0f, label = "card_elevation")
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(if (isDragging) 1f else 0f)
            .graphicsLayer {
                translationY = dragOffsetY.toFloat()
                shadowElevation = elevation
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                checked -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                else -> MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = item.isChecked, onCheckedChange = { onToggle(item.id) })
            Spacer(modifier = Modifier.width(3.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = if (item.isChecked) FontWeight.Normal else FontWeight.SemiBold,
                        color = if (item.isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (item.quantity.isNotBlank() || item.category.isNotBlank()) {
                    Text(
                        text = listOfNotNull(
                            item.quantity.takeIf { it.isNotBlank() },
                            item.categoryDisplayName().takeIf { it.isNotBlank() },
                        ).joinToString(" · "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (showDragHandle) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .pointerInput(item.id) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { onDragStart() },
                                onDragEnd = { onDragEnd() },
                                onDragCancel = { onDragEnd() },
                                onDrag = { _, dragAmount ->
                                    onDrag(dragAmount.y)
                                },
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("⋮⋮", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.width(2.dp))
            }
            IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Bearbeiten", modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = { onDelete(item.id) }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Löschen", modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun EmptyStateCard(
    title: String,
    description: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
private fun CompactHintCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun ListEditorDialog(
    title: String,
    initialValue: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var value by remember(initialValue) { mutableStateOf(initialValue) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = value,
                    onValueChange = {
                        value = it
                        error = null
                    },
                    label = { Text("Listenname") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val cleaned = value.cleanedName()
                if (cleaned.isBlank()) {
                    error = "Bitte einen Listenname eingeben."
                } else {
                    onConfirm(cleaned)
                }
            }) { Text(confirmLabel) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        },
    )
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun ShoppingItemDialog(
    title: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit,
    suggestions: List<FrequentItem>,
    categoryOptions: List<String>,
    onDeleteSuggestion: (String) -> Unit,
    initialName: String = "",
    initialQuantity: String = "",
    initialCategory: String = "",
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    var quantity by remember(initialQuantity) { mutableStateOf(initialQuantity) }
    var category by remember(initialCategory) { mutableStateOf(initialCategory) }
    var error by remember { mutableStateOf<String?>(null) }
    var categoryMenuOpen by remember { mutableStateOf(false) }
    var showMoreSuggestions by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        error = null
                    },
                    label = { Text("Artikelname") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Menge (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Kategorie (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (categoryOptions.isNotEmpty()) {
                    Text(
                        text = "Vorschläge",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    androidx.compose.foundation.layout.FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val visibleSuggestions = if (showMoreSuggestions) suggestions else suggestions.take(3)
                        visibleSuggestions.forEach { suggestion ->
                            SuggestionChip(
                                onClick = {
                                    name = suggestion.name
                                    quantity = suggestion.quantity
                                    category = suggestion.category
                                    error = null
                                },
                                label = { Text(suggestion.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            )
                        }
                        if (suggestions.size > 3) {
                            SuggestionChip(
                                onClick = { showMoreSuggestions = !showMoreSuggestions },
                                label = {
                                    Text(
                                        if (showMoreSuggestions) {
                                            "Weniger"
                                        } else {
                                            "Mehr +${suggestions.size - 3}"
                                        },
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                            )
                        }
                    }
                    if (showMoreSuggestions && suggestions.size > 3) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Weitere Vorschläge",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            suggestions.drop(3).forEach { suggestion ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    SuggestionChip(
                                        onClick = {
                                            name = suggestion.name
                                            quantity = suggestion.quantity
                                            category = suggestion.category
                                            error = null
                                        },
                                        label = {
                                            Text(
                                                suggestion.name,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        },
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = { onDeleteSuggestion(suggestion.name) },
                                        modifier = Modifier.size(28.dp),
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Vorschlag entfernen",
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Box {
                        TextButton(onClick = { categoryMenuOpen = true }) {
                            Text(if (category.isBlank()) "Kategorie wählen" else "Kategorie ändern")
                        }
                        DropdownMenu(expanded = categoryMenuOpen, onDismissRequest = { categoryMenuOpen = false }) {
                            categoryOptions.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item) },
                                    onClick = {
                                        category = item
                                        categoryMenuOpen = false
                                    },
                                )
                            }
                        }
                    }
                }
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val cleaned = name.cleanedName()
                if (cleaned.isBlank()) {
                    error = "Bitte einen Artikelnamen eingeben."
                } else {
                    onConfirm(cleaned, quantity, category)
                }
            }) { Text(confirmLabel) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        },
    )
}
