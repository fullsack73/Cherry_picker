package teamcherrypicker.com.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import teamcherrypicker.com.Screen
import teamcherrypicker.com.data.CardCategory
import teamcherrypicker.com.data.CreditCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCardsScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<CardCategory?>(null) }

    // Convert to mutable state
    val myCards = remember {
        mutableStateListOf(
            CreditCard("1", "Chase Sapphire Preferred", "Chase", CardCategory.TRAVEL, ""),
            CreditCard("2", "Amex Gold", "American Express", CardCategory.DINING, "")
        )
    }
    val allCards = remember {
        mutableStateListOf(
            CreditCard("3", "Citi Double Cash", "Citi", CardCategory.CASHBACK, ""),
            CreditCard("4", "Capital One Venture", "Capital One", CardCategory.TRAVEL, ""),
            CreditCard("5", "Discover It", "Discover", CardCategory.CASHBACK, ""),
            CreditCard("6", "Chase Freedom Flex", "Chase", CardCategory.SHOPPING, "")
        )
    }

    val onAddCard: (CreditCard) -> Unit = { card ->
        if (!myCards.contains(card)) {
            myCards.add(card)
            allCards.remove(card)
        }
    }

    val onRemoveCard: (CreditCard) -> Unit = { card ->
        if (myCards.contains(card)) {
            myCards.remove(card)
            allCards.add(card)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Cards") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.AddCardFormScreen.route) }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Card")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 16.dp)) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Cards") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            // Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(CardCategory.values()) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = if (selectedCategory == category) null else category },
                        label = { Text(category.displayName) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val filteredMyCards = myCards.filter { it.name.contains(searchQuery, ignoreCase = true) && (selectedCategory == null || it.category == selectedCategory) }
            val filteredAllCards = allCards.filter { it.name.contains(searchQuery, ignoreCase = true) && (selectedCategory == null || it.category == selectedCategory) }

            // "My Cards" List
            Text("My Cards", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                items(filteredMyCards) { card ->
                    CardListItem(card = card, buttonText = "Remove", onClick = { onRemoveCard(card) })
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // "All Cards" List
            Text("All Cards", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            LazyColumn {
                items(filteredAllCards) { card ->
                    CardListItem(card = card, buttonText = "Add", onClick = { onAddCard(card) })
                }
            }
        }
    }
}

@Composable
fun CardListItem(card: CreditCard, buttonText: String, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(card.name, fontWeight = FontWeight.Bold)
                Text(card.issuer, style = MaterialTheme.typography.bodySmall)
            }
            Button(onClick = onClick) {
                Text(buttonText)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardFormScreen(navController: NavController) {
    var cardName by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Card") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Enter your card details", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = cardName,
                onValueChange = { cardName = it },
                label = { Text("Card Name / Nickname") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = cardNumber,
                onValueChange = { cardNumber = it },
                label = { Text("Card Number") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = expiryDate,
                    onValueChange = { expiryDate = it },
                    label = { Text("MM/YY") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = cvv,
                    onValueChange = { cvv = it },
                    label = { Text("CVV") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { /* TODO: Save card logic */ navController.popBackStack() },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Save Card")
            }
        }
    }
}
