package ru.netology.nework.fragments.authorisation

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentRegisterBinding
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.RegisterViewModel

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private val registerViewModel: RegisterViewModel by viewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private var login = ""
    private var name = ""
    private var password = ""
    private var confirmPassword = ""

    private val startForPhotoResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val fileUri = data?.data!!
                    val file = fileUri.toFile()
                    registerViewModel.setPhoto(fileUri, file)
                }

                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT)
                        .show()
                }

                else -> {
                    // Пользователь отменил выбор
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        setupObservers()
    }

    private fun setupObservers() {
        // Следим за успешной авторизацией (после регистрации)
        authViewModel.dataAuth.observe(viewLifecycleOwner) { state ->
            val token = state.token.toString()
            if (state.id != 0L && token.isNotEmpty()) {
                findNavController().navigateUp()
            }
        }

        // Следим за фото
        registerViewModel.photoData.observe(viewLifecycleOwner) { photo ->
            binding.apply {
                if (photo != null) {
                    preview.setImageURI(photo.uri)
                    preview.isVisible = true
                    removePhoto.isVisible = true
                } else {
                    preview.setImageURI(null)
                    preview.isVisible = false
                    removePhoto.isVisible = false
                }
            }
        }

        // Следим за состоянием регистрации
        registerViewModel.registerState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RegisterViewModel.RegisterState.Loading -> {
                    binding.apply {
                        progressBar.isVisible = true
                        buttonLogin.isEnabled = false
                    }
                }

                is RegisterViewModel.RegisterState.Success -> {
                    binding.apply {
                        progressBar.isVisible = false
                        buttonLogin.isEnabled = true
                    }
                    // Навигация произойдёт через authViewModel.dataAuth
                }

                is RegisterViewModel.RegisterState.Error -> {
                    binding.apply {
                        progressBar.isVisible = false
                        buttonLogin.isEnabled = true
                    }
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                    registerViewModel.resetState()
                }

                is RegisterViewModel.RegisterState.Idle -> {
                    binding.apply {
                        progressBar.isVisible = false
                        buttonLogin.isEnabled = true
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.pickPhoto.setOnClickListener {
            ImagePicker.Builder(this)
                .crop(1f, 1f)
                .maxResultSize(2048, 2048)
                .createIntent {
                    startForPhotoResult.launch(it)
                }
        }

        binding.removePhoto.setOnClickListener {
            registerViewModel.clearPhoto()
        }

        binding.loginTextField.addTextChangedListener {
            login = it.toString()
            binding.apply {
                loginLayout.error = null
                buttonLogin.isChecked = updateButtonState()
            }
        }

        binding.nameTextField.addTextChangedListener {
            name = it.toString()
            binding.apply {
                nameLayout.error = null
                buttonLogin.isChecked = updateButtonState()
            }
        }

        binding.passwordTextField.addTextChangedListener {
            password = it.toString()
            binding.apply {
                passLayout.error = null
                repeatPassLayout.error = null
                buttonLogin.isChecked = updateButtonState()
            }
        }

        binding.repeatPasswordTextField.addTextChangedListener {
            confirmPassword = it.toString()
            binding.apply {
                passLayout.error = null
                repeatPassLayout.error = null
                buttonLogin.isChecked = updateButtonState()
            }
        }

        binding.buttonLogin.setOnClickListener {
            login = login.trim()
            name = name.trim()
            password = password.trim()
            confirmPassword = confirmPassword.trim()

            val loginEmpty = login.isEmpty()
            val nameEmpty = name.isEmpty()
            val passwordsMatch = password == confirmPassword
            val passwordEmpty = password.isEmpty()
            val confirmPasswordEmpty = confirmPassword.isEmpty()

            binding.apply {
                loginLayout.error = if (loginEmpty) getString(R.string.empty_login) else null
                nameLayout.error = if (nameEmpty) getString(R.string.name_is_empty) else null

                passLayout.error = when {
                    passwordEmpty -> getString(R.string.passwords_is_empty)
                    !passwordsMatch -> getString(R.string.passwords_dont_match)
                    else -> null
                }

                repeatPassLayout.error = when {
                    confirmPasswordEmpty -> getString(R.string.passwords_is_empty)
                    !passwordsMatch -> getString(R.string.passwords_dont_match)
                    else -> null
                }
            }

            if (loginEmpty || nameEmpty || !passwordsMatch || passwordEmpty || confirmPasswordEmpty) {
                return@setOnClickListener
            }

            registerViewModel.register(login, name, password)
        }

        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun updateButtonState(): Boolean {
        return login.isNotEmpty() && name.isNotEmpty()
                && password.isNotEmpty() && confirmPassword.isNotEmpty()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}