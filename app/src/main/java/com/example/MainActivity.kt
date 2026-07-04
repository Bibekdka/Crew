package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ui.LabourViewModel
import com.example.ui.LabourViewModelFactory
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExpenseScreen
import com.example.ui.screens.WorkerScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  private val viewModel: LabourViewModel by viewModels {
    LabourViewModelFactory(application)
  }

  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        var selectedTab by remember { mutableIntStateOf(0) }

        Scaffold(
          modifier = Modifier.fillMaxSize(),
          topBar = {
            CenterAlignedTopAppBar(
              title = {
                Text(
                  text = when (selectedTab) {
                    0 -> "Labour Ledger"
                    1 -> "Daily Attendance"
                    2 -> "Workers & Advances"
                    else -> "Site Expenses"
                  },
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
                )
              },
              colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
              )
            )
          },
          bottomBar = {
            NavigationBar {
              NavigationBarItem(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                label = { Text("Dashboard") }
              )
              NavigationBarItem(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                icon = { Icon(Icons.Default.HowToReg, contentDescription = "Attendance") },
                label = { Text("Attendance") }
              )
              NavigationBarItem(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                icon = { Icon(Icons.Default.People, contentDescription = "Workers") },
                label = { Text("Workers") }
              )
              NavigationBarItem(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Expenses") },
                label = { Text("Expenses") }
              )
            }
          }
        ) { innerPadding ->
          when (selectedTab) {
            0 -> DashboardScreen(
              viewModel = viewModel,
              onNavigateToTab = { selectedTab = it },
              modifier = Modifier.padding(innerPadding)
            )
            1 -> AttendanceScreen(
              viewModel = viewModel,
              modifier = Modifier.padding(innerPadding)
            )
            2 -> WorkerScreen(
              viewModel = viewModel,
              modifier = Modifier.padding(innerPadding)
            )
            3 -> ExpenseScreen(
              viewModel = viewModel,
              modifier = Modifier.padding(innerPadding)
            )
          }
        }
      }
    }
  }
}

