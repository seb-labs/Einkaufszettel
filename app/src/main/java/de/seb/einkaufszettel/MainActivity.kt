package de.seb.einkaufszettel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import de.seb.einkaufszettel.ui.theme.EinkaufszettelTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EinkaufszettelApp()
        }
    }
}

@Composable
fun EinkaufszettelApp(viewModel: ShoppingViewModel = viewModel()) {
    val darkTheme = viewModel.state.darkThemeEnabled ?: false
    EinkaufszettelTheme(darkTheme = darkTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            ShoppingScreen(
                state = viewModel.state,
                onSelectList = viewModel::selectList,
                onSetCheckedVisibility = viewModel::setCheckedVisibility,
                onSetDarkThemeEnabled = viewModel::setDarkThemeEnabled,
                onAddList = viewModel::addList,
                onRenameList = viewModel::renameSelectedList,
                onDeleteList = viewModel::deleteSelectedList,
                onAddItem = viewModel::addItem,
                onUpdateItem = viewModel::updateItem,
                onToggleItem = viewModel::toggleItem,
                onDeleteItem = viewModel::deleteItem,
                onDeleteSuggestion = viewModel::deleteSuggestion,
                onClearCheckedItems = viewModel::clearCheckedItems,
                onLoadDemoData = viewModel::loadDemoData,
                onMoveOpenItem = viewModel::moveOpenItem,
            )
        }
    }
}
