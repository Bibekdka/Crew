package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Worker
import com.example.ui.LabourViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AttendanceScreen(
    viewModel: LabourViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val workers by viewModel.workers.collectAsState()
    val allAttendance by viewModel.allAttendance.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val dateAttendance by viewModel.selectedDateAttendance.collectAsState()

    val calendar = remember { Calendar.getInstance() }
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    // Helper function to update the view model's date and local calendar state
    fun updateSelectedDate(newDateString: String) {
        viewModel.setDate(newDateString)
        try {
            val date = dateFormatter.parse(newDateString)
            if (date != null) {
                calendar.time = date
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Parse string to calendar on first load
    LaunchedEffect(selectedDate) {
        try {
            val date = dateFormatter.parse(selectedDate)
            if (date != null) {
                calendar.time = date
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("attendance_screen")
    ) {
        // Date selector Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = {
                    calendar.add(Calendar.DAY_OF_YEAR, -1)
                    updateSelectedDate(dateFormatter.format(calendar.time))
                }) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Previous Day",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Clickable Date display to open native DatePickerDialog
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val year = calendar.get(Calendar.YEAR)
                            val month = calendar.get(Calendar.MONTH)
                            val day = calendar.get(Calendar.DAY_OF_MONTH)

                            DatePickerDialog(
                                context,
                                { _, selectedYear, selectedMonth, selectedDay ->
                                    val newCalendar = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, selectedYear)
                                        set(Calendar.MONTH, selectedMonth)
                                        set(Calendar.DAY_OF_MONTH, selectedDay)
                                    }
                                    updateSelectedDate(dateFormatter.format(newCalendar.time))
                                },
                                year, month, day
                            ).show()
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Select Date",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = LabourViewModel.formatDateToDisplay(selectedDate),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                    updateSelectedDate(dateFormatter.format(calendar.time))
                }) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Next Day",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Quick Actions (Mark All Present/Absent)
        if (workers.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        workers.forEach { worker ->
                            viewModel.markAttendance(worker.id, selectedDate, "Present")
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("mark_all_present"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.DoneAll,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("All Present", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        workers.forEach { worker ->
                            viewModel.markAttendance(worker.id, selectedDate, "Absent")
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("mark_all_absent"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("All Absent", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Attendance list
        if (workers.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.PeopleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Workers Registered",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Add workers in the 'Workers' tab to mark attendance.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 32.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(workers) { worker ->
                    val attendanceRecord = dateAttendance.find { it.workerId == worker.id }
                    val currentStatus = attendanceRecord?.status ?: "Unmarked"
                    val currentNote = attendanceRecord?.note ?: ""

                    AttendanceWorkerCard(
                        worker = worker,
                        currentStatus = currentStatus,
                        currentNote = currentNote,
                        onStatusChange = { newStatus ->
                            viewModel.markAttendance(
                                workerId = worker.id,
                                dateString = selectedDate,
                                status = newStatus,
                                note = currentNote
                            )
                        },
                        onNoteChange = { newNote ->
                            viewModel.markAttendance(
                                workerId = worker.id,
                                dateString = selectedDate,
                                status = currentStatus,
                                note = newNote
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AttendanceWorkerCard(
    worker: Worker,
    currentStatus: String,
    currentNote: String,
    onStatusChange: (String) -> Unit,
    onNoteChange: (String) -> Unit
) {
    var showNoteInput by remember { mutableStateOf(currentNote.isNotEmpty()) }
    var noteText by remember(currentNote) { mutableStateOf(currentNote) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (currentStatus) {
                "Present" -> Color(0xFFE8F5E9)  // Very soft green
                "Half-Day" -> Color(0xFFFFF3E0) // Very soft orange
                "Absent" -> Color(0xFFFFEBEE)   // Very soft red
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Worker details
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                when (currentStatus) {
                                    "Present" -> Color(0xFF2E7D32)
                                    "Half-Day" -> Color(0xFFEF6C00)
                                    "Absent" -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = worker.name.take(2).uppercase(),
                            color = if (currentStatus == "Unmarked") MaterialTheme.colorScheme.primary else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = worker.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (currentStatus == "Unmarked") MaterialTheme.colorScheme.onSurface else Color.Black
                        )
                        Text(
                            text = "Daily wage: ₹${worker.dailyWage.toInt()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (currentStatus == "Unmarked") MaterialTheme.colorScheme.onSurfaceVariant else Color.DarkGray
                        )
                    }
                }

                IconButton(onClick = { showNoteInput = !showNoteInput }) {
                    Icon(
                        imageVector = if (currentNote.isNotEmpty()) Icons.Default.Note else Icons.Default.NoteAdd,
                        contentDescription = "Add Note",
                        tint = if (currentNote.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Attendance Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AttendanceButton(
                    label = "Present",
                    isSelected = currentStatus == "Present",
                    selectedColor = Color(0xFF2E7D32),
                    onClick = { onStatusChange("Present") },
                    modifier = Modifier.weight(1f)
                )

                AttendanceButton(
                    label = "Half-Day",
                    isSelected = currentStatus == "Half-Day",
                    selectedColor = Color(0xFFEF6C00),
                    onClick = { onStatusChange("Half-Day") },
                    modifier = Modifier.weight(1f)
                )

                AttendanceButton(
                    label = "Absent",
                    isSelected = currentStatus == "Absent",
                    selectedColor = MaterialTheme.colorScheme.error,
                    onClick = { onStatusChange("Absent") },
                    modifier = Modifier.weight(1f)
                )
            }

            // Optional note input
            AnimatedVisibility(visible = showNoteInput) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = {
                            noteText = it
                            onNoteChange(it)
                        },
                        label = { Text("Add attendance remarks") },
                        placeholder = { Text("e.g. Came late, overtime 2 hrs") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("attendance_note_${worker.id}"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun AttendanceButton(
    label: String,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isSelected) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = selectedColor),
            shape = RoundedCornerShape(12.dp),
            modifier = modifier.height(40.dp)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(12.dp),
            border = ButtonDefaults.outlinedButtonBorder.copy(),
            modifier = modifier.height(40.dp)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}
