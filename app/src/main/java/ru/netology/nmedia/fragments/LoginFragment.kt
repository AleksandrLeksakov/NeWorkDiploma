package ru.netology.nmedia.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.databinding.FragmentLoginBinding
import ru.netology.nmedia.viewmodel.AuthViewModel

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentLoginBinding.inflate(inflater, container, false)

        binding.loginButton.setOnClickListener {
            val login = binding.loginEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (login.isBlank() || password.isBlank()) {
                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.auth(login, password)
        }

        viewModel.authState.observe(viewLifecycleOwner) { state ->
            if (state.token != null) {
                findNavController().popBackStack()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }
}