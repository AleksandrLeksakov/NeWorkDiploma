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
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.databinding.ActivityAppBinding
import ru.netology.nmedia.viewmodel.AuthViewModel
import javax.inject.Inject
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.NavController

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var repository: PostRepository

    @Inject
    lateinit var auth: AppAuth
    private val viewModel: AuthViewModel by viewModels()

    private lateinit var binding: ActivityAppBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding = ActivityAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        println("🎯 MainActivity onCreate начат")
        Toast.makeText(this, "MainActivity запущен", Toast.LENGTH_SHORT).show()

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

        viewModel.data.observe(this) {
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

                menu.let {
                    it.setGroupVisible(R.id.unauthenticated, !viewModel.authenticated)
                    it.setGroupVisible(R.id.authenticated, viewModel.authenticated)
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.signin -> {
                        auth.setAuth(5, "x-token")
                        true
                    }

                    R.id.signup -> {
                        auth.setAuth(5, "x-token")
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

    // ВАЖНО: ДОБАВЬТЕ ЭТОТ МЕТОД
    private fun setupBottomNavigation() {
        println("🔧 setupBottomNavigation() вызван")

        if (!this::binding.isInitialized) {
            println("❌ Binding не инициализирован")
            return
        }

        // ВАЖНО: Ждем пока view будет полностью создан
        binding.root.post {
            try {
                println("🔍 Ищем NavController после создания view...")

                // Способ 1: Через FragmentContainerView
                val navHostFragment = supportFragmentManager
                    .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment

                if (navHostFragment == null) {
                    println("❌ NavHostFragment не найден")
                    // Попробуем другой способ
                    try {
                        val navController = findNavController(R.id.nav_host_fragment)
                        setupNavigation(navController)
                    } catch (e: Exception) {
                        println("❌ Не могу найти NavController: ${e.message}")
                        Toast.makeText(this, "Ошибка навигации", Toast.LENGTH_LONG).show()
                    }
                    return@post
                }

                val navController = navHostFragment.navController
                println("✅ NavController найден через NavHostFragment")
                setupNavigation(navController)

            } catch (e: Exception) {
                println("❌ Критическая ошибка: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun setupNavigation(navController: NavController) {
        try {
            // Проверяем BottomNavigationView
            if (binding.bottomNavigation == null) {
                println("❌ BottomNavigationView не найден в binding")

                // Попробуем найти через findViewById
                val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                    R.id.bottom_navigation
                )
                if (bottomNav == null) {
                    println("❌ BottomNavigationView не найден вообще")
                    Toast.makeText(this, "BottomNavigation не найден", Toast.LENGTH_LONG).show()
                    return
                }
                println("✅ BottomNavigationView найден через findViewById")
            } else {
                println("✅ BottomNavigationView найден через binding")
            }

            // Связываем
            binding.bottomNavigation.setupWithNavController(navController)
            println("✅ BottomNavigation связан с NavController")

            // Проверяем что меню загружено
            val menu = binding.bottomNavigation.menu
            println("📋 Меню BottomNavigation (${menu.size()} items):")
            for (i in 0 until menu.size()) {
                val item = menu.getItem(i)
                val idName = try {
                    resources.getResourceEntryName(item.itemId)
                } catch (e: Exception) {
                    "unknown_${item.itemId}"
                }
                println("  - ${item.title}: $idName (${item.itemId})")
            }

            // Настраиваем видимость
            navController.addOnDestinationChangedListener { _, destination, _ ->
                val idName = try {
                    resources.getResourceEntryName(destination.id)
                } catch (e: Exception) {
                    "unknown_${destination.id}"
                }
                println("🎯 Навигация: $idName (${destination.id})")

                val showBottomNav = destination.id in setOf(
                    R.id.feedFragment,
                    R.id.eventsFragment,
                    R.id.usersFragment
                )

                binding.bottomNavigation.visibility = if (showBottomNav) View.VISIBLE else View.GONE
                println("  BottomNavigation виден: $showBottomNav")
            }

            // Проверяем граф
            println("🗺️ Граф навигации:")
            navController.graph.forEach { destination ->
                val destIdName = try {
                    resources.getResourceEntryName(destination.id)
                } catch (e: Exception) {
                    "unknown_${destination.id}"
                }
               // println("  - $destIdName -> ${destination}")
            }

            Toast.makeText(this, "✅ BottomNavigation готов", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            println("❌ Ошибка настройки навигации: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
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