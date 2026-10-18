package com.example.sixcoachconfectionery.data.repository

import com.example.sixcoachconfectionery.data.model.Customer
import com.example.sixcoachconfectionery.data.model.Staff
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class StaffCustomerRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val staffCollection = firestore.collection("staff")
    private val customersCollection = firestore.collection("customers")

    // Staff
    suspend fun getStaffList(): Result<List<Staff>> {
        return try {
            val snapshot = staffCollection.get().await()
            Result.success(snapshot.toObjects(Staff::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveStaff(staff: Staff): Result<Unit> {
        return try {
            val id = staff.staffId.ifEmpty { staffCollection.document().id }
            val staffToSave = staff.copy(staffId = id)
            staffCollection.document(id).set(staffToSave).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStaffById(staffId: String): Result<Staff?> {
        return try {
            val doc = staffCollection.document(staffId).get().await()
            Result.success(doc.toObject(Staff::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Customers
    suspend fun getCustomers(): Result<List<Customer>> {
        return try {
            val snapshot = customersCollection.get().await()
            Result.success(snapshot.toObjects(Customer::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveCustomer(customer: Customer): Result<Unit> {
        return try {
            val id = customer.customerId.ifEmpty { customersCollection.document().id }
            val customerToSave = customer.copy(customerId = id)
            customersCollection.document(id).set(customerToSave).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchCustomerByPhone(phone: String): Result<List<Customer>> {
        return try {
            val snapshot = customersCollection.whereEqualTo("phoneNumber", phone).get().await()
            Result.success(snapshot.toObjects(Customer::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
