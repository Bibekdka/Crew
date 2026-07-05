package com.example.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.Expense
import com.example.ui.DashboardStats
import com.example.ui.WorkerFinancials
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ExportUtils {

    private fun formatCurrency(amount: Double): String {
        return "₹%,.2f".format(amount)
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun exportToPdf(
        context: Context,
        stats: DashboardStats,
        workerFinancials: List<WorkerFinancials>,
        expenses: List<Expense>
    ) {
        try {
            val pdfDocument = PdfDocument()
            val sdf = SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.getDefault())
            val generationDate = sdf.format(Date())

            // ------------------ PAGE 1: PAYROLL REPORT ------------------
            var pageNum = 1
            var pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNum).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            val paint = Paint()
            val textPaintRight = Paint().apply {
                textAlign = Paint.Align.RIGHT
            }

            // Draw Header Banner
            val headerPaint = Paint().apply {
                color = Color.parseColor("#1E3A8A") // Deep Royal Navy Blue
            }
            canvas.drawRect(0f, 0f, 595f, 90f, headerPaint)

            paint.color = Color.WHITE
            paint.textSize = 22f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("LABOUR LEDGER", 24f, 40f, paint)

            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Project Payroll & Financial Summary Report", 24f, 60f, paint)
            canvas.drawText("Generated: $generationDate", 24f, 76f, paint)

            // Draw Summary Cards Block
            var y = 110f
            val cardBgPaint = Paint().apply {
                color = Color.parseColor("#F3F4F6") // Cool gray background
            }
            canvas.drawRect(24f, y, 571f, y + 80f, cardBgPaint)

            paint.color = Color.parseColor("#1F2937") // Dark text
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("SITE STATS SUMMARY", 36f, y + 24f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 10f
            canvas.drawText("Total Active Workers: ${stats.totalWorkersCount}", 36f, y + 44f, paint)
            canvas.drawText("Total Labour Earnings: ${formatCurrency(stats.totalEarningsAllTime)}", 36f, y + 62f, paint)

            val netDuesText = if (stats.netFinancialObligation >= 0) "Net Dues to Pay" else "Labour Overpaid"
            canvas.drawText("Total Advances Paid: ${formatCurrency(stats.totalAdvancesPaid)}", 300f, y + 44f, paint)
            canvas.drawText("$netDuesText: ${formatCurrency(Math.abs(stats.netFinancialObligation))}", 300f, y + 62f, paint)

            // Draw Worker Table Title
            y += 115f
            paint.color = Color.parseColor("#1E3A8A")
            paint.textSize = 14f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("WORKER PAYROLL SUMMARY", 24f, y, paint)

            // Draw Worker Table Header
            y += 12f
            val tableHeaderPaint = Paint().apply {
                color = Color.parseColor("#E5E7EB") // Table light header background
            }
            canvas.drawRect(24f, y, 571f, y + 24f, tableHeaderPaint)

            paint.color = Color.parseColor("#374151")
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            
            // X coordinates of columns
            val colNameX = 28f
            val colWageX = 180f
            val colDaysX = 250f
            val colEarnedX = 310f
            val colPaidX = 410f
            val colBalanceX = 510f

            canvas.drawText("Worker Name", colNameX, y + 16f, paint)
            canvas.drawText("Wage (₹)", colWageX, y + 16f, paint)
            canvas.drawText("Days", colDaysX, y + 16f, paint)
            
            textPaintRight.color = Color.parseColor("#374151")
            textPaintRight.textSize = 10f
            textPaintRight.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            
            canvas.drawText("Earned (₹)", colEarnedX + 80f, y + 16f, textPaintRight)
            canvas.drawText("Paid (₹)", colPaidX + 80f, y + 16f, textPaintRight)
            canvas.drawText("Balance (₹)", colBalanceX + 55f, y + 16f, textPaintRight)

            // Draw Worker Rows
            y += 24f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 9f
            textPaintRight.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textPaintRight.textSize = 9f

            val linePaint = Paint().apply {
                color = Color.parseColor("#E5E7EB")
                strokeWidth = 1f
            }

            for (fin in workerFinancials) {
                // Check if page overflows
                if (y > 780f) {
                    pdfDocument.finishPage(page)
                    pageNum++
                    pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNum).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    y = 50f

                    // Redraw Table Header on next page
                    canvas.drawRect(24f, y, 571f, y + 24f, tableHeaderPaint)
                    paint.color = Color.parseColor("#374151")
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas.drawText("Worker Name", colNameX, y + 16f, paint)
                    canvas.drawText("Wage (₹)", colWageX, y + 16f, paint)
                    canvas.drawText("Days", colDaysX, y + 16f, paint)
                    canvas.drawText("Earned (₹)", colEarnedX + 80f, y + 16f, textPaintRight)
                    canvas.drawText("Paid (₹)", colPaidX + 80f, y + 16f, textPaintRight)
                    canvas.drawText("Balance (₹)", colBalanceX + 55f, y + 16f, textPaintRight)
                    y += 24f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                }

                // Draw row line
                canvas.drawLine(24f, y, 571f, y, linePaint)

                // Fill data
                paint.color = Color.parseColor("#111827")
                // Clip name if too long
                val displayName = if (fin.worker.name.length > 22) fin.worker.name.take(20) + ".." else fin.worker.name
                canvas.drawText(displayName, colNameX, y + 18f, paint)
                canvas.drawText("%,.1f".format(fin.worker.dailyWage), colWageX, y + 18f, paint)
                canvas.drawText("%.1f".format(fin.totalPresent), colDaysX, y + 18f, paint)

                canvas.drawText("%,.2f".format(fin.totalEarnings), colEarnedX + 80f, y + 18f, textPaintRight)
                canvas.drawText("%,.2f".format(fin.totalAdvancePaid), colPaidX + 80f, y + 18f, textPaintRight)

                val balance = fin.outstandingBalance
                val balanceColor = if (balance >= 0) "#B91C1C" else "#047857" // Red for due, Green for overpaid
                textPaintRight.color = Color.parseColor(balanceColor)
                canvas.drawText("%,.2f".format(balance), colBalanceX + 55f, y + 18f, textPaintRight)

                y += 26f
            }
            canvas.drawLine(24f, y, 571f, y, linePaint)

            // Total row
            y += 4f
            paint.color = Color.parseColor("#111827")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaintRight.color = Color.parseColor("#111827")
            textPaintRight.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

            canvas.drawText("TOTALS", colNameX, y + 16f, paint)
            canvas.drawText("%,.2f".format(stats.totalEarningsAllTime), colEarnedX + 80f, y + 16f, textPaintRight)
            canvas.drawText("%,.2f".format(stats.totalAdvancesPaid), colPaidX + 80f, y + 16f, textPaintRight)
            
            val totalBalanceColor = if (stats.netFinancialObligation >= 0) "#B91C1C" else "#047857"
            textPaintRight.color = Color.parseColor(totalBalanceColor)
            canvas.drawText("%,.2f".format(stats.netFinancialObligation), colBalanceX + 55f, y + 16f, textPaintRight)

            pdfDocument.finishPage(page)

            // ------------------ PAGE 2: GENERAL EXPENSES REPORT ------------------
            pageNum++
            pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNum).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas

            // Header for Page 2
            canvas.drawRect(0f, 0f, 595f, 65f, headerPaint)
            paint.color = Color.WHITE
            paint.textSize = 16f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("LABOUR LEDGER", 24f, 28f, paint)

            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("General Site Expenses Report | Generated: $generationDate", 24f, 46f, paint)

            y = 90f
            paint.color = Color.parseColor("#1E3A8A")
            paint.textSize = 14f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("SITE GENERAL EXPENSES", 24f, y, paint)

            // Table Header Expenses
            y += 12f
            canvas.drawRect(24f, y, 571f, y + 24f, tableHeaderPaint)

            paint.color = Color.parseColor("#374151")
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

            val colExpDateX = 28f
            val colExpTitleX = 130f
            val colExpCatX = 340f
            val colExpAmtX = 470f

            canvas.drawText("Date", colExpDateX, y + 16f, paint)
            canvas.drawText("Title / Particulars", colExpTitleX, y + 16f, paint)
            canvas.drawText("Category", colExpCatX, y + 16f, paint)

            textPaintRight.color = Color.parseColor("#374151")
            textPaintRight.textSize = 10f
            textPaintRight.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Amount (₹)", colExpAmtX + 95f, y + 16f, textPaintRight)

            // Draw Expense Rows
            y += 24f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 9f
            textPaintRight.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textPaintRight.textSize = 9f

            if (expenses.isEmpty()) {
                canvas.drawText("No site general expenses recorded.", colExpDateX, y + 24f, paint)
                y += 36f
            } else {
                for (exp in expenses) {
                    if (y > 780f) {
                        pdfDocument.finishPage(page)
                        pageNum++
                        pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNum).create()
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        y = 50f

                        // Redraw table header
                        canvas.drawRect(24f, y, 571f, y + 24f, tableHeaderPaint)
                        paint.color = Color.parseColor("#374151")
                        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        canvas.drawText("Date", colExpDateX, y + 16f, paint)
                        canvas.drawText("Title / Particulars", colExpTitleX, y + 16f, paint)
                        canvas.drawText("Category", colExpCatX, y + 16f, paint)
                        canvas.drawText("Amount (₹)", colExpAmtX + 95f, y + 16f, textPaintRight)
                        y += 24f
                        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    }

                    canvas.drawLine(24f, y, 571f, y, linePaint)

                    paint.color = Color.parseColor("#111827")
                    canvas.drawText(formatDate(exp.date), colExpDateX, y + 18f, paint)
                    val displayTitle = if (exp.title.length > 34) exp.title.take(32) + ".." else exp.title
                    canvas.drawText(displayTitle, colExpTitleX, y + 18f, paint)
                    canvas.drawText(exp.category, colExpCatX, y + 18f, paint)

                    canvas.drawText("%,.2f".format(exp.amount), colExpAmtX + 95f, y + 18f, textPaintRight)

                    y += 26f
                }
                canvas.drawLine(24f, y, 571f, y, linePaint)
            }

            // Expense Total Row
            y += 4f
            paint.color = Color.parseColor("#111827")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaintRight.color = Color.parseColor("#111827")
            textPaintRight.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

            canvas.drawText("TOTAL SITE EXPENSES", colExpDateX, y + 16f, paint)
            canvas.drawText("%,.2f".format(stats.totalGeneralExpenses), colExpAmtX + 95f, y + 16f, textPaintRight)

            pdfDocument.finishPage(page)

            // Save and Share PDF
            val fileName = "Labour_Ledger_Report_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
            val cacheFile = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(cacheFile)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()

            shareFile(context, cacheFile, "application/pdf")

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error generating PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun exportToExcel(
        context: Context,
        stats: DashboardStats,
        workerFinancials: List<WorkerFinancials>,
        expenses: List<Expense>
    ) {
        try {
            val csvBuilder = StringBuilder()
            val sdf = SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.getDefault())
            val generationDate = sdf.format(Date())

            // Metadata / Title
            csvBuilder.append("LABOUR LEDGER PROJECT REPORT\n")
            csvBuilder.append("Generated on:,$generationDate\n")
            csvBuilder.append("\n")

            // Summary metrics
            csvBuilder.append("FINANCIAL OVERVIEW SUMMARY\n")
            csvBuilder.append("Metric,Value\n")
            csvBuilder.append("Total Registered Workers,${stats.totalWorkersCount}\n")
            csvBuilder.append("Total Labour Earnings (₹),${stats.totalEarningsAllTime}\n")
            csvBuilder.append("Total Advances Paid (₹),${stats.totalAdvancesPaid}\n")
            csvBuilder.append("Net Dues to Pay (₹),${stats.netFinancialObligation}\n")
            csvBuilder.append("General Site Expenses (₹),${stats.totalGeneralExpenses}\n")
            csvBuilder.append("\n")

            // Worker Details Section
            csvBuilder.append("WORKER PAYROLL SUMMARY TABLE\n")
            csvBuilder.append("Worker Name,Daily Wage (₹),Days Present,Total Earned (₹),Advances Paid (₹),Outstanding Balance (₹)\n")
            for (fin in workerFinancials) {
                // escape commas in names
                val cleanName = fin.worker.name.replace(",", " ")
                csvBuilder.append("$cleanName,${fin.worker.dailyWage},${fin.totalPresent},${fin.totalEarnings},${fin.totalAdvancePaid},${fin.outstandingBalance}\n")
            }
            csvBuilder.append("TOTALS,,,${stats.totalEarningsAllTime},${stats.totalAdvancesPaid},${stats.netFinancialObligation}\n")
            csvBuilder.append("\n")

            // General Site Expenses Section
            csvBuilder.append("GENERAL SITE EXPENSES TABLE\n")
            csvBuilder.append("Date,Title / Particulars,Category,Amount (₹),Note\n")
            val expSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            for (exp in expenses) {
                val cleanTitle = exp.title.replace(",", " ")
                val cleanNote = exp.note.replace(",", " ").replace("\n", " ")
                val dateStr = expSdf.format(Date(exp.date))
                csvBuilder.append("$dateStr,$cleanTitle,${exp.category},${exp.amount},$cleanNote\n")
            }
            csvBuilder.append("TOTAL SITE EXPENSES,,,${stats.totalGeneralExpenses},\n")

            // Save and Share CSV
            val fileName = "Labour_Ledger_Report_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.csv"
            val cacheFile = File(context.cacheDir, fileName)
            cacheFile.writeText(csvBuilder.toString())

            shareFile(context, cacheFile, "text/csv")

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error generating CSV: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun shareFile(context: Context, file: File, mimeType: String) {
        val authority = "${context.packageName}.fileprovider"
        val fileUri = FileProvider.getUriForFile(context, authority, file)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, fileUri)
            putExtra(Intent.EXTRA_SUBJECT, "Labour Ledger Report")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        // Trigger share sheet
        val chooserIntent = Intent.createChooser(intent, "Share/Save Ledger Report").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooserIntent)
    }
}
