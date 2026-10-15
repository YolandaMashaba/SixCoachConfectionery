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
    suspend fun getStaffList(): List<Staff> {
        val snapshot = staffCollection.get().await()
        return snapshot.toObjects(Staff::class.java)
    }

    suspend fun saveStaff(staff: Staff) {
        val id = staff.staffId.ifEmpty { staffCollection.document().id }
        val staffToSave = staff.copy(staffId = id)
        staffCollection.document(id).set(staffToSave).await()
    }

    suspend fun getStaffById(staffId: String): Staff? {
        val doc = staffCollection.document(staffId).get().await()
        return doc.toObject(Staff::class.java)
    }

    // Customers
    suspend fun getCustomers(): List<Customer> {
        val snapshot = customersCollection.get().await()
        return snapshot.toObjects(Customer::class.java)
    }

    suspend fun saveCustomer(customer: Customer) {
        val id = customer.customerId.ifEmpty { customersCollection.document().id }
        val customerToSave = customer.copy(customerId = id)
        customersCollection.document(id).set(customerToSave).await()
    }

    suspend fun searchCustomerByPhone(phone: String): List<Customer> {
        val snapshot = customersCollection.whereEqualTo("phoneNumber", phone).get().await()
        return snapshot.toObjects(Customer::class.java)
    }
}
