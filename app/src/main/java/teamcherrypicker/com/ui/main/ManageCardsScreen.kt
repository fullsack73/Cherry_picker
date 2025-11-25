package teamcherrypicker.com.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import teamcherrypicker.com.Screen
import teamcherrypicker.com.data.CardSummary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ManageCardsScreen(navController: NavController) {
    val viewModel: CardsViewModel = viewModel(factory = CardsViewModel.provideFactory())
    val uiState by viewModel.uiState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    val savedCardIds = remember { mutableStateListOf<Int>() }

    LaunchedEffect(uiState.cards) {
        if (uiState.cards.isNotEmpty() && savedCardIds.isEmpty()) {
            savedCardIds.addAll(uiState.cards.take(2).map { it.id })
        }
    }

    val availableCategories = remember(uiState.cards) {
        uiState.cards
            .flatMap { it.normalizedCategories }
            .map { it.uppercase() }
            .distinct()
            .sorted()
    }

    val filteredCards = remember(uiState.cards, searchQuery, selectedCategory) {
        uiState.cards.filter { card ->
            val matchesQuery = card.name.contains(searchQuery, ignoreCase = true) ||
                card.issuer.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == null ||
                card.normalizedCategories.any { it.equals(selectedCategory, ignoreCase = true) }
            matchesQuery && matchesCategory
        }
    }

    val myCards = filteredCards.filter { savedCardIds.contains(it.id) }
    val discoverCards = filteredCards.filter { !savedCardIds.contains(it.id) }

    val onAddCard: (CardSummary) -> Unit = { card ->
        if (!savedCardIds.contains(card.id)) {
            savedCardIds.add(card.id)
        }
    }

    val onRemoveCard: (CardSummary) -> Unit = { card ->
        savedCardIds.remove(card.id)
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
        Column(modifier = Modifier
            .padding(padding)
            .padding(horizontal = 16.dp)) {
            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
            }

            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

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
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All") }
                    )
                }
                items(availableCategories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = {
                            selectedCategory = if (selectedCategory == category) null else category
                        },
                        label = { Text(category.toCategoryLabel()) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // "My Cards" List
            Text("My Cards", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (myCards.isEmpty()) {
                Text(
                    text = "No cards saved yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    items(myCards, key = { it.id }) { card ->
                        ManageCardListItem(
                            card = card,
                            buttonText = "Remove",
                            onClick = { onRemoveCard(card) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // "All Cards" List
            Text("All Cards", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            LazyColumn {
                items(discoverCards, key = { it.id }) { card ->
                    ManageCardListItem(
                        card = card,
                        buttonText = "Add",
                        onClick = { onAddCard(card) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ManageCardListItem(card: CardSummary, buttonText: String, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(card.name, fontWeight = FontWeight.Bold)
                Text(card.issuer, style = MaterialTheme.typography.bodySmall)
                if (card.normalizedCategories.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        card.normalizedCategories.forEach { category ->
                            AssistChip(onClick = {}, label = { Text(category.toCategoryLabel()) })
                        }
                    }
                }
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
