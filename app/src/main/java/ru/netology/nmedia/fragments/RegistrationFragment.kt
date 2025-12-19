package ru.netology.nmedia.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.databinding.FragmentRegistrationBinding
import ru.netology.nmedia.viewmodel.AuthViewModel

@AndroidEntryPoint
class RegistrationFragment : Fragment() {

    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by activityViewModels()
    private var avatarUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            avatarUri = it
            binding.avatarImageView.setImageURI(it)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pickImageLauncher.launch("image/*")
        } else {
            Snackbar.make(
                binding.root,
                "Для выбора аватара нужны разрешения",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.registrationSuccess.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        viewModel.registrationError.observe(viewLifecycleOwner) { error ->
            binding.progressBar.isVisible = false
            Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun setupListeners() {
        binding.registerButton.setOnClickListener {
            val login = binding.loginEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val name = binding.nameEditText.text.toString()
            val repeatPassword = binding.confirmPasswordEditText.text.toString()

            Log.d("RegistrationFragment", "Нажата кнопка регистрации")
            Log.d("RegistrationFragment", "login=$login, name=$name, password.length=${password.length}")

            if (validateInput(login, password, repeatPassword, name)) {
                binding.progressBar.isVisible = true
                Log.d("RegistrationFragment", "Валидация пройдена, вызываем регистрацию")
                viewModel.register(login, password, name, avatarUri)
            } else {
                Log.d("RegistrationFragment", "Валидация не пройдена")
            }
        }

        // ДОБАВЛЕНЫ ОБРАБОТЧИКИ ДЛЯ КНОПОК АВАТАРА
        binding.selectAvatarButton.setOnClickListener {
            Log.d("RegistrationFragment", "Нажата кнопка выбора аватара")
            requestGalleryPermission()
        }

        binding.avatarImageView.setOnClickListener {
            Log.d("RegistrationFragment", "Нажат ImageView аватара")
            requestGalleryPermission()
        }
    }

    private fun validateInput(
        login: String,
        password: String,
        repeatPassword: String,
        name: String
    ): Boolean {
        // ПРЯМОЕ ПРИВЕДЕНИЕ ТИПА БЕЗ EXTENSION ФУНКЦИЙ
        binding.loginInputLayout.error = if (login.isBlank()) "Введите логин" else null
        binding.nameInputLayout.error = if (name.isBlank()) "Введите имя" else null

        val passwordError = when {
            password.isBlank() -> "Введите пароль"
            password.length < 6 -> "Пароль должен быть не менее 6 символов"
            else -> null
        }
        binding.passwordInputLayout.error = passwordError

        val confirmPasswordError = when {
            repeatPassword.isBlank() -> "Повторите пароль"
            password != repeatPassword -> "Пароли не совпадают"
            else -> null
        }
        binding.confirmPasswordInputLayout.error = confirmPasswordError

        return login.isNotBlank() &&
                name.isNotBlank() &&
                password.isNotBlank() &&
                password.length >= 6 &&
                repeatPassword.isNotBlank() &&
                password == repeatPassword
    }

    private fun requestGalleryPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                pickImageLauncher.launch("image/*")
            }

            shouldShowRequestPermissionRationale(permission) -> {
                Snackbar.make(
                    binding.root,
                    "Приложению нужен доступ к галерее для выбора аватара",
                    Snackbar.LENGTH_LONG
                ).setAction("Дать доступ") {
                    requestPermissionLauncher.launch(permission)
                }.show()
            }

            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.progressBar.isVisible = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}