package com.example.sixcoachconfectionery.data.repository

import com.example.sixcoachconfectionery.data.model.Category
import com.example.sixcoachconfectionery.data.model.InventoryTransaction
import com.example.sixcoachconfectionery.data.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProductRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val categoriesCollection = firestore.collection("categories")
    private val productsCollection = firestore.collection("products")
    private val inventoryCollection = firestore.collection("inventory_transactions")

    // Categories
    suspend fun getCategories(): List<Category> {
        val snapshot = categoriesCollection.get().await()
        return snapshot.toObjects(Category::class.java)
    }

    suspend fun saveCategory(category: Category) {
        val id = category.categoryId.ifEmpty { categoriesCollection.document().id }
        val categoryToSave = category.copy(categoryId = id)
        categoriesCollection.document(id).set(categoryToSave).await()
    }

    // Products
    suspend fun getProducts(): List<Product> {
        val snapshot = productsCollection.get().await()
        return snapshot.toObjects(Product::class.java)
    }

    suspend fun getProductById(productId: String): Product? {
        val doc = productsCollection.document(productId).get().await()
        return doc.toObject(Product::class.java)
    }

    suspend fun saveProduct(product: Product) {
        val id = product.productId.ifEmpty { productsCollection.document().id }
        val productToSave = product.copy(productId = id)
        productsCollection.document(id).set(productToSave).await()
    }

    suspend fun deleteProduct(productId: String) {
        productsCollection.document(productId).delete().await()
    }

    // Inventory Transactions & Stock update
    suspend fun addInventoryTransaction(transaction: InventoryTransaction, product: Product) {
        val transId = transaction.transactionId.ifEmpty { inventoryCollection.document().id }
        val transToSave = transaction.copy(transactionId = transId)

        // Calculate new stock
        val newQuantity = when (transaction.transactionType) {
            "Add", "Return" -> product.quantityInStock + transaction.quantity
            "Sale", "Adjust" -> maxOf(0, product.quantityInStock - transaction.quantity)
            else -> product.quantityInStock
        }
        val newAvailability = if (newQuantity > 0) "In Stock" else "Out of Stock"

        // Firestore Batch write for atomicity
        val batch = firestore.batch()
        batch.set(inventoryCollection.document(transId), transToSave)
        batch.update(
            productsCollection.document(product.productId),
            mapOf(
                "quantityInStock" to newQuantity,
                "availability" to newAvailability
            )
        )
        batch.commit().await()
    }
}
