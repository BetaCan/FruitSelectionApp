package com.example.fruitselectionapp
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fruitselectionapp.ui.theme.FruitSelectionAppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the Room database instance
        val db = DatabaseInstance.getDatabase(applicationContext)
        val fruitDao = db.fruitDao()

        setContent {
            FruitSelectionAppTheme {
                // SnackbarHostState to manage Snackbar messages
                val snackbarHostState = remember { SnackbarHostState() }

                // Scaffold UI with SnackbarHost
                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    FruitListScreen(
                        fruitDao = fruitDao,
                        snackbarHostState = snackbarHostState,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun FruitListScreen(
    fruitDao: FruitDao,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    // Coroutine scope for database operations
    val coroutineScope = rememberCoroutineScope()

    // Mutable state list to store fruits from the database
    val fruits = remember { mutableStateListOf<Fruit>() }

    // State for the dropdown menu
    var expanded by remember { mutableStateOf(false) }
    var selectedFruit by remember { mutableStateOf<Pair<String, String>?>(null) }

    // Predefined list of fruits with their emojis
    val predefinedFruits = listOf(
        Pair("Apple", "🍎"),
        Pair("Banana", "🍌"),
        Pair("Orange", "🍊"),
        Pair("Grapes", "🍇"),
        Pair("Watermelon", "🍉")
    )

    // Load fruits from the database when the composable is first displayed
    LaunchedEffect(Unit) {
        fruits.addAll(fruitDao.getAllFruits())
    }

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        // Dropdown menu for predefined fruits
        Box {
            Button(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = selectedFruit?.first ?: "Select a Fruit",
                    modifier = Modifier.padding(8.dp)
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                predefinedFruits.forEach { fruit ->
                    DropdownMenuItem(
                        text = { Text("${fruit.second} ${fruit.first}") },
                        onClick = {
                            selectedFruit = fruit
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))


        Button(  // Button to add the selected fruit to the database
            onClick = {
                coroutineScope.launch {
                    selectedFruit?.let { fruit ->
                        if (fruits.none { it.name == fruit.first }) {
                            // Add the fruit if it doesn't exist
                            val newFruit = Fruit(name = fruit.first, emoji = fruit.second)
                            fruitDao.insertFruit(newFruit) // Add to database
                            fruits.clear()
                            fruits.addAll(fruitDao.getAllFruits()) // Refresh the list
                            snackbarHostState.showSnackbar("Fruit added: ${fruit.first}") // Success message
                        } else {
                            // Show a message that the fruit already exists
                            snackbarHostState.showSnackbar("Fruit already exists: ${fruit.first}")
                        }
                        selectedFruit = null // Reset to default message
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Selected Fruit")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // LazyColumn to display the list of fruits
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(fruits) { fruit ->
                FruitCard(
                    fruit = fruit,
                    onDelete = {
                        coroutineScope.launch {
                            fruitDao.deleteFruitByName(fruit.name) // Delete the fruit
                            fruits.clear()
                            fruits.addAll(fruitDao.getAllFruits()) // Refresh the list
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun FruitCard(fruit: Fruit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "${fruit.emoji} ${fruit.name}")
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Fruit"
                )
            }
        }
    }
}


