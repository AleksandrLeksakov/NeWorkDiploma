package ru.netology.nmedia.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentRegistrationBinding
import ru.netology.nmedia.viewmodel.AuthViewModel
import java.io.File
import java.io.FileOutputStream

@AndroidEntryPoint
class RegistrationFragment : Fragment() {

    private lateinit var binding: FragmentRegistrationBinding
    private val viewModel: AuthViewModel by viewModels()
    private var selectedImageUri: Uri? = null
    private var tempImageFile: File? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pickImage()
        } else {
            Toast.makeText(
                requireContext(),
                "Нужно разрешение для выбора фото",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            // Создаем временный файл
            tempImageFile = createTempFile(uri)

            Glide.with(binding.avatarImageView)
                .load(uri)
                .circleCrop()
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(binding.avatarImageView)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Устанавливаем placeholder при создании
        binding.avatarImageView.setBackgroundResource(R.color.purple_500)

        setupClickListeners()
        setupObservers()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Удаляем временный файл
        tempImageFile?.delete()
    }

    private fun setupClickListeners() {
        binding.avatarImageView.setOnClickListener {
            checkPermissionAndPickImage()
        }

        binding.selectAvatarButton.setOnClickListener {
            checkPermissionAndPickImage()
        }

        binding.registerButton.setOnClickListener {
            val login = binding.loginEditText.text.toString()
            val name = binding.nameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val confirmPassword = binding.confirmPasswordEditText.text.toString()

            if (!validateInputs(login, name, password, confirmPassword)) {
                return@setOnClickListener
            }

            viewModel.register(login, name, password, tempImageFile)
        }
    }

    private fun setupObservers() {
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            if (state.token != null) {
                findNavController().popBackStack()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
            binding.registerButton.isEnabled = !isLoading
        }
    }

    private fun checkPermissionAndPickImage() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                pickImage()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                Toast.makeText(
                    requireContext(),
                    "Разрешение нужно для выбора аватара",
                    Toast.LENGTH_SHORT
                ).show()
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun pickImage() {
        pickImageLauncher.launch("image/*")
    }

    private fun createTempFile(uri: Uri): File {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("avatar_", ".jpg", requireContext().cacheDir)

        inputStream?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }

        return tempFile
    }

    private fun validateInputs(
        login: String,
        name: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        var isValid = true

        if (login.isBlank()) {
            binding.loginEditText.error = "Введите логин"
            isValid = false
        }

        if (name.isBlank()) {
            binding.nameEditText.error = "Введите имя"
            isValid = false
        }

        if (password.isBlank()) {
            binding.passwordEditText.error = "Введите пароль"
            isValid = false
        }

        if (confirmPassword.isBlank()) {
            binding.confirmPasswordEditText.error = "Подтвердите пароль"
            isValid = false
        }

        if (password != confirmPassword) {
            binding.confirmPasswordEditText.error = "Пароли не совпадают"
            isValid = false
        }

        if (password.length < 6) {
            binding.passwordEditText.error = "Пароль должен быть не менее 6 символов"
            isValid = false
        }

        return isValid
    }
}