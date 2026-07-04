package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class WorkerFinancials(
    val worker: Worker,
    val totalPresent: Double,
    val totalAbsent: Int,
    val totalHalfDay: Int,
    val totalEarnings: Double,
    val totalAdvancePaid: Double,
    val outstandingBalance: Double
)

data class DashboardStats(
    val totalWorkersCount: Int,
    val presentTodayCount: Int,
    val halfDayTodayCount: Int,
    val absentTodayCount: Int,
    val totalEarningsAllTime: Double,
    val totalAdvancesPaid: Double,
    val totalGeneralExpenses: Double,
    val netFinancialObligation: Double
)

class LabourViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LabourRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = LabourRepository(database.labourDao())
    }

    // Date management
    private val _selectedDate = MutableStateFlow(getCurrentDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Workers flow
    val workers: StateFlow<List<Worker>> = repository.allWorkers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Attendance flow
    val allAttendance: StateFlow<List<Attendance>> = repository.allAttendance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Payments flow
    val allPayments: StateFlow<List<Payment>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All General Expenses flow
    val allExpenses: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active attendance list for the selected date
    val selectedDateAttendance: StateFlow<List<Attendance>> = _selectedDate
        .flatMapLatest { date -> repository.getAttendanceForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reactively compute financials per worker
    val workerFinancialsList: StateFlow<List<WorkerFinancials>> = combine(
        workers,
        allAttendance,
        allPayments
    ) { workerList, attendanceList, paymentList ->
        workerList.map { worker ->
            val workerAttendances = attendanceList.filter { it.workerId == worker.id }
            val presentCount = workerAttendances.count { it.status == "Present" }.toDouble()
            val halfDayCount = workerAttendances.count { it.status == "Half-Day" }.toDouble()
            val absentCount = workerAttendances.count { it.status == "Absent" }

            val effectiveDays = presentCount + (halfDayCount * 0.5)
            val totalEarnings = effectiveDays * worker.dailyWage

            val workerPayments = paymentList.filter { it.workerId == worker.id }
            val totalAdvancesAndSalary = workerPayments.sumOf { it.amount }

            WorkerFinancials(
                worker = worker,
                totalPresent = effectiveDays,
                totalAbsent = absentCount,
                totalHalfDay = halfDayCount.toInt(),
                totalEarnings = totalEarnings,
                totalAdvancePaid = totalAdvancesAndSalary,
                outstandingBalance = totalEarnings - totalAdvancesAndSalary
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reactively compute Dashboard metrics
    val dashboardStats: StateFlow<DashboardStats> = combine(
        workerFinancialsList,
        allAttendance,
        allExpenses,
        _selectedDate
    ) { financials, attendances, expenses, date ->
        val workersCount = financials.size
        
        val dayAttendances = attendances.filter { it.dateString == date }
        val presentToday = dayAttendances.count { it.status == "Present" }
        val halfDayToday = dayAttendances.count { it.status == "Half-Day" }
        val absentToday = dayAttendances.count { it.status == "Absent" }

        val totalEarnings = financials.sumOf { it.totalEarnings }
        val totalAdvances = financials.sumOf { it.totalAdvancePaid }
        val totalGenExpenses = expenses.sumOf { it.amount }

        DashboardStats(
            totalWorkersCount = workersCount,
            presentTodayCount = presentToday,
            halfDayTodayCount = halfDayToday,
            absentTodayCount = absentToday,
            totalEarningsAllTime = totalEarnings,
            totalAdvancesPaid = totalAdvances,
            totalGeneralExpenses = totalGenExpenses,
            netFinancialObligation = totalEarnings - totalAdvances
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats(0, 0, 0, 0, 0.0, 0.0, 0.0, 0.0))

    // Set selected date
    fun setDate(dateString: String) {
        _selectedDate.value = dateString
    }

    // Workers operations
    fun addWorker(name: String, phone: String, dailyWage: Double) {
        viewModelScope.launch {
            repository.insertWorker(Worker(name = name, phone = phone, dailyWage = dailyWage))
        }
    }

    fun updateWorker(worker: Worker) {
        viewModelScope.launch {
            repository.updateWorker(worker)
        }
    }

    fun deleteWorker(worker: Worker) {
        viewModelScope.launch {
            repository.deleteWorker(worker)
        }
    }

    // Attendance operations
    fun markAttendance(workerId: Int, dateString: String, status: String, note: String = "") {
        viewModelScope.launch {
            repository.deleteAttendanceForDateAndWorker(dateString, workerId)
            repository.insertAttendance(
                Attendance(
                    workerId = workerId,
                    dateString = dateString,
                    status = status,
                    note = note
                )
            )
        }
    }

    // Payments operations
    fun addPayment(workerId: Int, amount: Double, type: String, note: String = "") {
        viewModelScope.launch {
            repository.insertPayment(
                Payment(
                    workerId = workerId,
                    amount = amount,
                    type = type,
                    note = note
                )
            )
        }
    }

    fun deletePayment(payment: Payment) {
        viewModelScope.launch {
            repository.deletePayment(payment)
        }
    }

    // Expense operations
    fun addExpense(title: String, amount: Double, category: String, note: String = "") {
        viewModelScope.launch {
            repository.insertExpense(
                Expense(
                    title = title,
                    amount = amount,
                    category = category,
                    note = note
                )
            )
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    // Date Utilities
    companion object {
        fun getCurrentDateString(): String {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return formatter.format(Date())
        }

        fun formatDateToDisplay(dateString: String): String {
            return try {
                val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = parser.parse(dateString)
                val displayFormatter = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
                if (date != null) displayFormatter.format(date) else dateString
            } catch (e: Exception) {
                dateString
            }
        }

        fun formatEpochToDisplay(epoch: Long): String {
            val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            return formatter.format(Date(epoch))
        }
    }
}

class LabourViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LabourViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LabourViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
