package com.example.sixcoachconfectionery

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.sixcoachconfectionery.data.SixCoachConfectioneryRepository
import com.example.sixcoachconfectionery.databinding.ActivityMainBinding
import com.example.sixcoachconfectionery.ui.admin.AdminFragment
import com.example.sixcoachconfectionery.ui.auth.LoginFragment
import com.example.sixcoachconfectionery.ui.auth.RegisterFragment
import com.example.sixcoachconfectionery.ui.kitchen.KitchenFragment
import com.example.sixcoachconfectionery.ui.sales.SalesFragment

class MainActivity : AppCompatActivity() {

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

        repository = SixCoachConfectioneryRepository.getInstance(this)
        repository.addChangeListener(repoChangeListener)

        setupHeader()
        setupBottomNavigation()

        // Start on Sales Terminal by default
        navigateToSales()
    }

    private fun setupHeader() {
        updateHeaderUserStatus()

        binding.btnLogout.setOnClickListener {
            repository.logout()
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            navigateToLogin()
        }

        binding.btnHeaderAuth.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun updateHeaderUserStatus() {
        val user = repository.getCurrentUser()
        if (user != null) {
            binding.layoutUserInfo.visibility = View.VISIBLE
            binding.btnHeaderAuth.visibility = View.GONE
            binding.tvUserStatus.text = user.username
        } else {
            binding.layoutUserInfo.visibility = View.GONE
            binding.btnHeaderAuth.visibility = View.VISIBLE
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_sales -> {
                    replaceFragment(SalesFragment())
                    true
                }
                R.id.nav_kitchen -> {
                    replaceFragment(KitchenFragment())
                    true
                }
                R.id.nav_admin -> {
                    replaceFragment(AdminFragment())
                    true
                }
                R.id.nav_account -> {
                    if (repository.getCurrentUser() != null) {
                        replaceFragment(SalesFragment())
                        Toast.makeText(this, "Logged in as ${repository.getCurrentUser()?.username}", Toast.LENGTH_SHORT).show()
                    } else {
                        replaceFragment(LoginFragment())
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    fun navigateToSales() {
        binding.bottomNavigation.selectedItemId = R.id.nav_sales
    }

    fun navigateToKitchen() {
        binding.bottomNavigation.selectedItemId = R.id.nav_kitchen
    }

    fun navigateToAdmin() {
        binding.bottomNavigation.selectedItemId = R.id.nav_admin
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
