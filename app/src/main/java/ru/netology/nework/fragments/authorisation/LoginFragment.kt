package ru.netology.nework.fragments.authorisation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentLoginBinding
import ru.netology.nework.viewmodel.AuthViewModel

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private val authViewModel: AuthViewModel by activityViewModels()

    private var login = ""
    private var password = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentLoginBinding.inflate(inflater, container, false)

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
            login.trim()
            password.trim()
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

        authViewModel.dataAuth.observe(viewLifecycleOwner) { state ->
            val token = state.token.toString()

            if (state.id != 0L && token.isNotEmpty()) {
                findNavController().navigateUp()
            }
        }

        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }

    private fun updateStateButtonLogin(): Boolean {
        return login.isNotEmpty() && password.isNotEmpty()
    }

}