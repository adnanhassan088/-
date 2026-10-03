package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.InvoiceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY createdAt DESC")
    fun getAllInvoices(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
    fun getInvoiceById(id: Long): Flow<InvoiceEntity?>

    @Query("SELECT * FROM invoices WHERE merchantName LIKE '%' || :query || '%' OR invoiceNumber LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchInvoices(query: String): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE category = :category ORDER BY createdAt DESC")
    fun getInvoicesByCategory(category: String): Flow<List<InvoiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoices(invoices: List<InvoiceEntity>)

    @Update
    suspend fun updateInvoice(invoice: InvoiceEntity)

    @Delete
    suspend fun deleteInvoice(invoice: InvoiceEntity)

    @Query("DELETE FROM invoices WHERE id = :id")
    suspend fun deleteInvoiceById(id: Long)

    @Query("SELECT COUNT(*) FROM invoices")
    fun getInvoiceCount(): Flow<Int>

    @Query("SELECT SUM(totalAmount) FROM invoices")
    fun getTotalSpent(): Flow<Double?>

    @Query("SELECT SUM(taxAmount) FROM invoices")
    fun getTotalTax(): Flow<Double?>
}
