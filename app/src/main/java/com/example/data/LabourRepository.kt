package com.example.data

import kotlinx.coroutines.flow.Flow

class LabourRepository(private val labourDao: LabourDao) {

    // Worker operations
    val allWorkers: Flow<List<Worker>> = labourDao.getAllWorkers()

    suspend fun insertWorker(worker: Worker) {
        labourDao.insertWorker(worker)
    }

    suspend fun updateWorker(worker: Worker) {
        labourDao.updateWorker(worker)
    }

    suspend fun deleteWorker(worker: Worker) {
        labourDao.deleteWorker(worker)
    }

    // Attendance operations
    val allAttendance: Flow<List<Attendance>> = labourDao.getAllAttendance()

    fun getAttendanceForDate(dateString: String): Flow<List<Attendance>> {
        return labourDao.getAttendanceForDate(dateString)
    }

    suspend fun insertAttendance(attendance: Attendance) {
        labourDao.insertAttendance(attendance)
    }

    suspend fun insertAttendanceList(attendanceList: List<Attendance>) {
        labourDao.insertAttendanceList(attendanceList)
    }

    suspend fun deleteAttendanceForDateAndWorker(dateString: String, workerId: Int) {
        labourDao.deleteAttendanceForDateAndWorker(dateString, workerId)
    }

    // Payment operations
    val allPayments: Flow<List<Payment>> = labourDao.getAllPayments()

    fun getPaymentsForWorker(workerId: Int): Flow<List<Payment>> {
        return labourDao.getPaymentsForWorker(workerId)
    }

    suspend fun insertPayment(payment: Payment) {
        labourDao.insertPayment(payment)
    }

    suspend fun deletePayment(payment: Payment) {
        labourDao.deletePayment(payment)
    }

    // Expense operations
    val allExpenses: Flow<List<Expense>> = labourDao.getAllExpenses()

    suspend fun insertExpense(expense: Expense) {
        labourDao.insertExpense(expense)
    }

    suspend fun deleteExpense(expense: Expense) {
        labourDao.deleteExpense(expense)
    }
}
