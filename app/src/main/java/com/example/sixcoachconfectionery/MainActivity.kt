package com.example.sixcoachconfectionery

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.fragment.app.Fragment
import com.example.sixcoachconfectionery.data.SixCoachConfectioneryRepository
import com.example.sixcoachconfectionery.data.model.UserProfile
import com.example.sixcoachconfectionery.data.models.User
import com.example.sixcoachconfectionery.databinding.ActivityMainBinding
import com.example.sixcoachconfectionery.ui.admin.AdminFragment
import com.example.sixcoachconfectionery.ui.auth.LoginFragment
import com.example.sixcoachconfectionery.ui.kitchen.KitchenFragment
import com.example.sixcoachconfectionery.ui.sales.SalesFragment
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: SixCoachConfectioneryRepository

    private val repoChangeListener = {
        updateHeaderUserStatus()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase Auth & Firestore defensively
        auth = Firebase.auth
        try {
            val settings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
            FirebaseFirestore.getInstance().firestoreSettings = settings
        } catch (e: Exception) {
            e.printStackTrace()
        }
        repository = SixCoachConfectioneryRepository.getInstance(this)
        repository.addChangeListener(repoChangeListener)

        setupHeader()

        // Persistent Login State Check: Route immediately based on email/role
        if (savedInstanceState == null) {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val email = currentUser.email ?: ""
                if (email.equals("admin@sixcoachconfectionery.co.za", ignoreCase = true) || email.equals("ST10385107@rcconnect.edu.za", ignoreCase = true) || email.endsWith("@sixcoachconfectionery.co.za", ignoreCase = true)) {
                    // Check role in background or route default
                    navigateToSales()
                    checkUserRoleAndRoute(currentUser.uid)
                } else {
                    auth.signOut()
                    navigateToLogin()
                }
            } else {
                navigateToLogin()
            }
        }
    }

    private fun checkUserRoleAndRoute(uid: String) {
        CoroutineScope(Dispatchers.IO).launch {
            var targetRoute = 0 // 0 = Sales, 1 = Kitchen, 2 = Admin
            try {
                val doc = FirebaseFirestore.getInstance()
                    .collection("users").document(uid).get().await()
                val profile = doc.toObject(UserProfile::class.java)
                val email = auth.currentUser?.email ?: ""
                val isAdmin = profile?.isAdmin == true || profile?.role == "Admin" || email.equals("admin@sixcoachconfectionery.co.za", ignoreCase = true) || email.equals("ST10385107@rcconnect.edu.za", ignoreCase = true)
                val isKitchen = profile?.role == "Kitchen Staff"

                if (profile != null) {
                    repository.setCurrentUser(
                        User(
                            id = profile.uid,
                            username = profile.username.ifEmpty { profile.email },
                            passwordHash = "",
                            isAdmin = isAdmin
                        )
                    )
                } else if (isAdmin) {
                    repository.setCurrentUser(
                        User(
                            id = uid,
                            username = "SystemAdmin",
                            passwordHash = "",
                            isAdmin = true
                        )
                    )
                }

                targetRoute = when {
                    isAdmin -> 2
                    isKitchen -> 1
                    else -> 0
                }
            } catch (e: Exception) {
                val email = auth.currentUser?.email ?: ""
                targetRoute = if (email.equals("admin@sixcoachconfectionery.co.za", ignoreCase = true) || email.equals("ST10385107@rcconnect.edu.za", ignoreCase = true)) 2 else 0
            }

            withContext(Dispatchers.Main) {
                updateHeaderUserStatus()
                when (targetRoute) {
                    2 -> navigateToAdmin()
                    1 -> navigateToKitchen()
                    else -> navigateToSales()
                }
            }
        }
    }

    fun onUserLoggedIn(profile: UserProfile) {
        repository.setCurrentUser(
            User(
                id = profile.uid,
                username = profile.username.ifEmpty { profile.email },
                passwordHash = "",
                isAdmin = profile.isAdmin || profile.role == "Admin"
            )
        )
        updateHeaderUserStatus()
        when {
            profile.isAdmin || profile.role == "Admin" -> navigateToAdmin()
            profile.role == "Kitchen Staff" -> navigateToKitchen()
            else -> navigateToSales()
        }
    }

    fun onUserLoggedOut() {
        auth.signOut()
        repository.logout()
        navigateToLogin()
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
    }

    private fun setupHeader() {
        updateHeaderUserStatus()

        binding.btnLogout.setOnClickListener {
            onUserLoggedOut()
        }

        binding.btnHeaderAuth.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun updateHeaderUserStatus() {
        val user = repository.getCurrentUser()
        val authUser = auth.currentUser
        if (authUser != null) {
            binding.layoutUserInfo.visibility = View.VISIBLE
            binding.btnHeaderAuth.visibility = View.GONE
            binding.tvUserStatus.text = user?.username ?: authUser.email ?: "Staff"
        } else {
            binding.layoutUserInfo.visibility = View.GONE
            binding.btnHeaderAuth.visibility = View.VISIBLE
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    fun navigateToSales() {
        replaceFragment(SalesFragment())
    }

    fun navigateToKitchen() {
        replaceFragment(KitchenFragment())
    }

    fun navigateToAdmin() {
        replaceFragment(AdminFragment())
    }

    fun navigateToLogin() {
        replaceFragment(LoginFragment())
    }

    override fun onDestroy() {
        super.onDestroy()
        repository.removeChangeListener(repoChangeListener)
    }
}
