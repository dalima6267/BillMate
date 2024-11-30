package com.example.billmate.utils

import android.content.Context
import com.example.billmate.database.Bill
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

object ExcelExporter {
    fun exportBillsToExcel(context: Context, bills: List<Bill>, fileName: String) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Bills")

        // Create the header row
        val headerRow = sheet.createRow(0)
        val headers = listOf("ID", "Name", "Date", "Type", "Amount", "Image URIs")
        for ((index, header) in headers.withIndex()) {
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
        }

        // Populate the sheet with data
        for ((rowIndex, bill) in bills.withIndex()) {
            val row = sheet.createRow(rowIndex + 1)
            row.createCell(0).setCellValue(bill.id.toDouble())
            row.createCell(1).setCellValue(bill.name ?: "")
            row.createCell(2).setCellValue(bill.date ?: "")
            row.createCell(3).setCellValue(bill.type ?: "")
            row.createCell(4).setCellValue(bill.amount ?: 0.0)
            row.createCell(5).setCellValue(bill.imageUri.joinToString(", ") { it.toString() })
        }

        // Save the file
        val file = File(context.getExternalFilesDir(null), fileName)
        FileOutputStream(file).use { outputStream ->
            workbook.write(outputStream)
        }

        workbook.close()
        println("Excel file created at: ${file.absolutePath}")
    }
}