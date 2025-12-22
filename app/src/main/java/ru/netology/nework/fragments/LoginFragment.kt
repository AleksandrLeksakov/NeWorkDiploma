package ru.netology.nework.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.databinding.FragmentLoginBinding
import ru.netology.nework.viewmodel.AuthViewModel

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.authSuccess.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        viewModel.authError.observe(viewLifecycleOwner) { error ->
            Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun setupListeners() {
        binding.loginButton.setOnClickListener {
            val login = binding.loginEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (validateInput(login, password)) {
                viewModel.authenticate(login, password)
            }
        }
    }

    private fun validateInput(login: String, password: String): Boolean {
        var isValid = true

        // ПРЯМОЕ ПРИВЕДЕНИЕ ТИПА БЕЗ EXTENSION ФУНКЦИЙ
        binding.loginEditText.error = if (login.isBlank()) "Введите логин" else null
        binding.passwordEditText.error = if (password.isBlank()) "Введите пароль" else null

        isValid = login.isNotBlank() && password.isNotBlank()

        return isValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}