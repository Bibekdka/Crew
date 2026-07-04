package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workers")
data class Worker(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val dailyWage: Double,
    val joiningDate: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

@Entity(tableName = "attendance")
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workerId: Int,
    val dateString: String, // format "yyyy-MM-dd"
    val status: String,     // "Present", "Absent", "Half-Day"
    val note: String = ""
)

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workerId: Int,
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val type: String, // "Advance", "Salary Paid"
    val note: String = ""
)

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val category: String, // "Food", "Transport", "Material", "Tools", "Other"
    val note: String = ""
)
