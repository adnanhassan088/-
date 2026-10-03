package com.example.data.repository

import com.example.data.local.InvoiceDao
import com.example.data.model.InvoiceEntity
import com.example.data.model.SampleInvoicesProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class InvoiceRepository(private val invoiceDao: InvoiceDao) {
    val allInvoices: Flow<List<InvoiceEntity>> = invoiceDao.getAllInvoices()

    fun getInvoiceById(id: Long): Flow<InvoiceEntity?> = invoiceDao.getInvoiceById(id)

    fun searchInvoices(query: String): Flow<List<InvoiceEntity>> = invoiceDao.searchInvoices(query)

    fun getInvoicesByCategory(category: String): Flow<List<InvoiceEntity>> = invoiceDao.getInvoicesByCategory(category)

    val invoiceCount: Flow<Int> = invoiceDao.getInvoiceCount()
    val totalSpent: Flow<Double?> = invoiceDao.getTotalSpent()
    val totalTax: Flow<Double?> = invoiceDao.getTotalTax()

    suspend fun insertInvoice(invoice: InvoiceEntity): Long = invoiceDao.insertInvoice(invoice)

    suspend fun updateInvoice(invoice: InvoiceEntity) = invoiceDao.updateInvoice(invoice)

    suspend fun deleteInvoice(invoice: InvoiceEntity) = invoiceDao.deleteInvoice(invoice)

    suspend fun deleteInvoiceById(id: Long) = invoiceDao.deleteInvoiceById(id)

    suspend fun seedInitialDataIfEmpty() {
        val count = invoiceDao.getInvoiceCount().first()
        if (count == 0) {
            val presets = SampleInvoicesProvider.presets.take(3)
            val sampleEntities = presets.mapIndexed { index, preset ->
                SampleInvoicesProvider.toEntity(
                    preset,
                    dateString = "2026-09-${20 - index}"
                )
            }
            invoiceDao.insertInvoices(sampleEntities)
        }
    }
}
