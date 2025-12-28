package ru.netology.nework.fragments.authorisation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentLoginBinding
import ru.netology.nework.viewmodel.AuthViewModel

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val authViewModel: AuthViewModel by activityViewModels()

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private var login = ""
    private var password = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        setupObservers()
    }

    private fun setupObservers() {
        authViewModel.dataAuth.observe(viewLifecycleOwner) { state ->
            val token = state.token.toString()
            if (state.id != 0L && token.isNotEmpty()) {
                findNavController().navigateUp()
            }
        }

        authViewModel.loginState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthViewModel.LoginState.Loading -> {
                    binding.apply {
                        progressBar.isVisible = true
                        buttonLogin.isEnabled = false
                        buttonRegister.isEnabled = false
                    }
                }

                is AuthViewModel.LoginState.Success -> {
                    binding.apply {
                        progressBar.isVisible = false
                        buttonLogin.isEnabled = true
                        buttonRegister.isEnabled = true
                    }
                }

                is AuthViewModel.LoginState.Error -> {
                    binding.apply {
                        progressBar.isVisible = false
                        buttonLogin.isEnabled = true
                        buttonRegister.isEnabled = true
                    }
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                    authViewModel.resetLoginState()
                }

                is AuthViewModel.LoginState.Idle -> {
                    binding.apply {
                        progressBar.isVisible = false
                        buttonLogin.isEnabled = true
                        buttonRegister.isEnabled = true
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.loginTextField.addTextChangedListener {
            login = it.toString()
            binding.apply {
                loginLayout.error = null
                buttonLogin.isChecked = updateStateButtonLogin()
            }
        }

        binding.passwordTextField.addTextChangedListener {
            password = it.toString()
            binding.apply {
                passwordLayout.error = null
                buttonLogin.isChecked = updateStateButtonLogin()
            }
        }

        binding.buttonLogin.setOnClickListener {
            login = login.trim()
            password = password.trim()

            when {
                password.isEmpty() && login.isEmpty() -> {
                    binding.apply {
                        loginLayout.error = getString(R.string.empty_login)
                        passwordLayout.error = getString(R.string.empty_password)
                    }
                }

                password.isEmpty() -> {
                    binding.passwordLayout.error = getString(R.string.empty_password)
                }

                login.isEmpty() -> {
                    binding.loginLayout.error = getString(R.string.empty_login)
                }

                else -> {
                    authViewModel.login(login, password)
                }
            }
        }

        binding.buttonRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun updateStateButtonLogin(): Boolean {
        return login.isNotEmpty() && password.isNotEmpty()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}