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
import com.example.sixcoachconfectionery.databinding.ActivityMainBinding
import com.example.sixcoachconfectionery.ui.admin.AdminFragment
import com.example.sixcoachconfectionery.ui.auth.LoginFragment
import com.example.sixcoachconfectionery.ui.auth.RegisterFragment
import com.example.sixcoachconfectionery.ui.kitchen.KitchenFragment
import com.example.sixcoachconfectionery.ui.sales.SalesFragment

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
            com.google.firebase.firestore.FirebaseFirestore.getInstance().firestoreSettings = settings
        } catch (e: Exception) {
            e.printStackTrace()
        }
        repository = SixCoachConfectioneryRepository.getInstance(this)
        repository.addChangeListener(repoChangeListener)

        setupHeader()

        // Force fresh start on Login page every time the app runs
        auth.signOut()
        repository.logout()
        navigateToLogin()
    }

    fun onUserLoggedIn(profile: UserProfile) {
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

    fun navigateToRegister() {
        replaceFragment(RegisterFragment())
    }

    override fun onDestroy() {
        super.onDestroy()
        repository.removeChangeListener(repoChangeListener)
    }
}
