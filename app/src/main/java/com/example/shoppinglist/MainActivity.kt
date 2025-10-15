package com.example.shoppinglist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.shoppinglist.Component.ItemInput
import com.example.shoppinglist.Component.SearchInput
import com.example.shoppinglist.Component.Title
import com.example.shoppinglist.components.ShoppingList
import com.example.shoppinglist.ui.theme.ShoppingListTheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ShoppingListTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) { ShoppingListApp()
            }
        }
        }
    }
}
enum class MainScreen {
    Home,
    Profile,
    Settings
}

enum class BottomNavTab(val label: String, val iconText: String, val screen: MainScreen) {
    Home("Home", "🏠", MainScreen.Home),
    Profile("Profile", "👤", MainScreen.Profile)
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListApp() {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var currentScreenName by rememberSaveable { mutableStateOf(MainScreen.Home.name) }
    val currentScreen = MainScreen.valueOf(currentScreenName)

    var newItemText by rememberSaveable { mutableStateOf("") }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val shoppingItems = remember { mutableStateListOf<String>() }

    val filteredItems by remember(searchQuery, shoppingItems) {
        derivedStateOf {
            if (searchQuery.isBlank()) {
                shoppingItems
            } else {
                shoppingItems.filter {
                    it.contains(searchQuery, ignoreCase = true)
                }
            }
        }
    }
    val tabs = listOf(BottomNavTab.Home, BottomNavTab.Profile)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = "Menu",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
                NavigationDrawerItem(
                    label = { Text(text = "Settings") },
                    selected = currentScreen == MainScreen.Settings,
                    onClick = {
                        currentScreenName = MainScreen.Settings.name
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                TopAppBar(
                    title = { Text(text = "Shopping List") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Buka menu")
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentScreen == tab.screen,
                            onClick = { currentScreenName = tab.screen.name },
                            icon = { Text(text = tab.iconText) },
                            label = { Text(text = tab.label) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        val direction = when {
                            targetState.ordinal > initialState.ordinal -> 1
                            targetState.ordinal < initialState.ordinal -> -1
                            else -> 0
                        }
                        if (direction == 0) {
                            fadeIn(tween(250)) togetherWith fadeOut(tween(250))
                        } else {
                            (slideInHorizontally(animationSpec = tween(300)) { it * direction } +
                                    fadeIn(animationSpec = tween(300))) togetherWith
                                    (slideOutHorizontally(animationSpec = tween(300)) { -it * direction } +
                                            fadeOut(animationSpec = tween(300)))
                        }
                    },
                    label = "ScreenTransition"
                ) { screen ->
                    when (screen) {
                        MainScreen.Home -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(WindowInsets.safeDrawing.asPaddingValues())
                                    .padding(horizontal = 16.dp)
                            ) {
                                Title()
                                ItemInput(
                                    text = newItemText,
                                    onTextChange = { newItemText = it },
                                    onAddItem = {
                                        if (newItemText.isNotBlank()) {
                                            shoppingItems.add(newItemText)
                                            newItemText = ""
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                SearchInput(
                                    query = searchQuery,
                                    onQueryChange = { searchQuery = it }
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                ShoppingList(items = filteredItems)
                            }
                        }
                        MainScreen.Profile -> {
                            val profileDetails = listOf(
                                "Nama" to "M Satria Gemilang",
                                "NIM" to "2111522008",
                                "Hobi" to "Bridge",
                                "TTL" to "Payakumbuh, 5 November 2000",
                                "Minat" to "Web Proggramming"
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                Text(
                                    text = "Profil Pengguna",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Image(
                                    painter = painterResource(id = R.drawable.bjm),
                                    contentDescription = "Foto Profil",
                                    modifier = Modifier
                                        .size(160.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )

                                Card(
                                    modifier = Modifier.fillMaxSize(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(20.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        profileDetails.forEachIndexed { index, (title, value) ->
                                            ProfileDetailRow(title = title, value = value)
                                            if (index != profileDetails.lastIndex) {
                                                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        MainScreen.Settings -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp)
                            ) {
                                Text(
                                    text = "Ini halaman Pengaturan",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Tambahkan opsi pengaturan lainnya di sini.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileDetailRow(title: String, value: String) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}