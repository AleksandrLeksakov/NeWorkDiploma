package ru.netology.nmedia.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.ActivityAppBinding
import ru.netology.nmedia.fragments.NewPostFragment.Companion.textArg
import ru.netology.nmedia.viewmodel.AuthViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var auth: AppAuth

    private val authViewModel: AuthViewModel by viewModels() // ИСПРАВЛЕНО имя

    private lateinit var binding: ActivityAppBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding = ActivityAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        intent?.let {
            if (it.action != Intent.ACTION_SEND) {
                return@let
            }

            val text = it.getStringExtra(Intent.EXTRA_TEXT)
            if (text?.isNotBlank() != true) {
                return@let
            }

            intent.removeExtra(Intent.EXTRA_TEXT)
            findNavController(R.id.nav_host_fragment)
                .navigate(
                    R.id.action_feedFragment_to_newPostFragment,
                    Bundle().apply {
                        textArg = text
                    }
                )
        }

        setupBottomNavigation()

        // ИСПРАВЛЕНО: используем authViewModel
        authViewModel.authState.observe(this) { state ->
            invalidateOptionsMenu()
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                println("some stuff happened: ${task.exception}")
                return@addOnCompleteListener
            }

            val token = task.result
            println(token)
        }

        checkGoogleApiAvailability()

        requestNotificationsPermission()

        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_actionbar, menu)

                // ИСПРАВЛЕНО: проверка авторизации
                val isAuthenticated = auth.authStateFlow.value.token != null

                menu.let {
                    it.setGroupVisible(R.id.unauthenticated, !isAuthenticated)
                    it.setGroupVisible(R.id.authenticated, isAuthenticated)
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.signin -> {
                        // TODO: Переход на экран входа
                        findNavController(R.id.nav_host_fragment).navigate(R.id.loginFragment)
                        true
                    }

                    R.id.signup -> {
                        // TODO: Переход на экран регистрации
                        findNavController(R.id.nav_host_fragment).navigate(R.id.registrationFragment)
                        true
                    }

                    R.id.signout -> {
                        auth.removeAuth()
                        true
                    }

                    else -> false
                }
        })
    }

    private fun setupBottomNavigation() {
        binding.root.post {
            try {
                val navHostFragment = supportFragmentManager
                    .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment

                if (navHostFragment == null) {
                    try {
                        val navController = findNavController(R.id.nav_host_fragment)
                        setupNavigation(navController)
                    } catch (e: Exception) {
                        println("❌ Не могу найти NavController: ${e.message}")
                    }
                    return@post
                }

                val navController = navHostFragment.navController
                setupNavigation(navController)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupNavigation(navController: NavController) {
        try {
            binding.bottomNavigation.setupWithNavController(navController)

            navController.addOnDestinationChangedListener { _, destination, _ ->
                val showBottomNav = destination.id in setOf(
                    R.id.feedFragment,
                    R.id.eventsFragment,
                    R.id.usersFragment
                )

                binding.bottomNavigation.visibility = if (showBottomNav) View.VISIBLE else View.GONE
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun requestNotificationsPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }

        val permission = Manifest.permission.POST_NOTIFICATIONS

        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            return
        }

        requestPermissions(arrayOf(permission), 1)
    }

    private fun checkGoogleApiAvailability() {
        with(GoogleApiAvailability.getInstance()) {
            val code = isGooglePlayServicesAvailable(this@MainActivity)
            if (code == ConnectionResult.SUCCESS) {
                return@with
            }
            if (isUserResolvableError(code)) {
                getErrorDialog(this@MainActivity, code, 9000)?.show()
                return
            }
            Toast.makeText(this@MainActivity, R.string.google_play_unavailable, Toast.LENGTH_LONG)
                .show()
        }
    }
}