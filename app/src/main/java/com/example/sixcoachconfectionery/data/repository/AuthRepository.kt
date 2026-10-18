package com.example.sixcoachconfectionery.data.repository

import com.example.sixcoachconfectionery.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val usersCollection = firestore.collection("users")

    /**
     * Staff self-registration: creates Firebase Auth account and stores user profile in Firestore with role = "Staff", isAdmin = false.
     */
    suspend fun registerStaff(email: String, pass: String, username: String): Result<UserProfile> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val firebaseUser = authResult.user ?: throw IllegalStateException("Firebase user is null after registration")
            
            val userProfile = UserProfile(
                uid = firebaseUser.uid,
                email = email,
                username = username,
                role = "Staff",
                isAdmin = false,
                createdAt = System.currentTimeMillis()
            )

            usersCollection.document(firebaseUser.uid).set(userProfile).await()
            Result.success(userProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Login for both Staff and Admin: signs in via Firebase Auth and fetches their Firestore user profile to resolve role/isAdmin status.
     */
    suspend fun loginUser(email: String, pass: String): Result<UserProfile> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, pass).await()
            val firebaseUser = authResult.user ?: throw IllegalStateException("Firebase user is null after sign in")

            val docSnapshot = usersCollection.document(firebaseUser.uid).get().await()
            val profile = docSnapshot.toObject(UserProfile::class.java)
                ?: UserProfile(uid = firebaseUser.uid, email = email, username = email.substringBefore("@"), role = "Staff", isAdmin = false)

            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches all system users for Admin User Management.
     */
    suspend fun getAllUsers(): Result<List<UserProfile>> {
        return try {
            val snapshot = usersCollection.get().await()
            Result.success(snapshot.toObjects(UserProfile::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates staff permissions (Admin feature).
     */
    suspend fun updateUserRole(uid: String, role: String, isAdmin: Boolean): Result<Unit> {
        return try {
            usersCollection.document(uid).update(
                mapOf(
                    "role" to role,
                    "isAdmin" to isAdmin
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Programmatically seeds the default Admin account (email: admin@email.com, password: admin@123)
     * in Firebase Authentication and ensures its Firestore document exists with isAdmin = true and role = "Admin".
     */
    suspend fun seedAdminAccount(adminEmail: String = "admin@email.com", adminPass: String = "admin@123"): Result<UserProfile> {
        return try {
            val signInResult = try {
                auth.signInWithEmailAndPassword(adminEmail, adminPass).await()
                true
            } catch (e: Exception) {
                false
            }

            val firebaseUser = if (signInResult) {
                auth.currentUser
            } else {
                val createResult = auth.createUserWithEmailAndPassword(adminEmail, adminPass).await()
                createResult.user
            } ?: throw IllegalStateException("Failed to seed admin user auth")

            val adminProfile = UserProfile(
                uid = firebaseUser.uid,
                email = adminEmail,
                username = "SystemAdmin",
                role = "Admin",
                isAdmin = true,
                createdAt = System.currentTimeMillis()
            )

            usersCollection.document(firebaseUser.uid).set(adminProfile).await()
            Result.success(adminProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
