package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var db: AppDatabase
  private lateinit var dao: LabourDao

  @Before
  fun createDb() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    dao = db.labourDao()
  }

  @After
  fun closeDb() {
    db.close()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Labour Ledger", appName)
  }

  @Test
  fun `test Room persistence for workers attendance and payments`() = runBlocking {
    // 1. Insert a new worker
    val worker = Worker(id = 1, name = "Rajesh Kumar", phone = "9876543210", dailyWage = 500.0)
    dao.insertWorker(worker)

    var workersList = dao.getAllWorkers().first()
    assertEquals(1, workersList.size)
    assertEquals("Rajesh Kumar", workersList[0].name)
    assertEquals(500.0, workersList[0].dailyWage, 0.01)

    // 2. Edit worker name and wage
    val updatedWorker = worker.copy(name = "Rajesh Sharma", dailyWage = 550.0)
    dao.updateWorker(updatedWorker)

    workersList = dao.getAllWorkers().first()
    assertEquals(1, workersList.size)
    assertEquals("Rajesh Sharma", workersList[0].name)
    assertEquals(550.0, workersList[0].dailyWage, 0.01)

    // 3. Mark Attendance
    val attendance = Attendance(id = 1, workerId = 1, dateString = "2026-07-04", status = "Present", note = "On Time")
    dao.insertAttendance(attendance)

    val attendanceList = dao.getAllAttendance().first()
    assertEquals(1, attendanceList.size)
    assertEquals("Present", attendanceList[0].status)
    assertEquals("2026-07-04", attendanceList[0].dateString)

    // 4. Record and Edit Payment
    val payment = Payment(id = 1, workerId = 1, amount = 1000.0, type = "Advance", note = "Initial Advance")
    dao.insertPayment(payment)

    var paymentList = dao.getAllPayments().first()
    assertEquals(1, paymentList.size)
    assertEquals(1000.0, paymentList[0].amount, 0.01)

    val updatedPayment = payment.copy(amount = 1200.0, note = "Updated Advance")
    dao.updatePayment(updatedPayment)

    paymentList = dao.getAllPayments().first()
    assertEquals(1, paymentList.size)
    assertEquals(1200.0, paymentList[0].amount, 0.01)
    assertEquals("Updated Advance", paymentList[0].note)
  }
}

