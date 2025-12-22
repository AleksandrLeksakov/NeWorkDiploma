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
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentRegisterBinding
import ru.netology.nework.viewmodel.AuthViewModel

@AndroidEntryPoint
class RegisterFragment : Fragment() {
    private val authViewModel: AuthViewModel by activityViewModels()

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

                    authViewModel.setPhoto(fileUri, file)
                }

                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT)
                        .show()
                }

                else -> {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.unknown_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentRegisterBinding.inflate(inflater, container, false)


        binding.pickPhoto.setOnClickListener {
            ImagePicker.Builder(this)
                .crop(1f, 1f)
                .maxResultSize(2048, 2048)
                .createIntent {
                    startForPhotoResult.launch(it)
                }
        }

        authViewModel.dataAuth.observe(viewLifecycleOwner) { state ->
            val token = state.token.toString()

            if (state.id != 0L && token.isNotEmpty()) {
                findNavController().navigateUp()
            }
        }

        authViewModel.photoData.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.preview.setImageURI(it.uri)
            }
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
                binding.nameLayout.error = null
                binding.buttonLogin.isChecked = updateButtonState()
            }
        }
        binding.passwordTextField.addTextChangedListener {
            password = it.toString()
            binding.passLayout.error = null
            binding.apply {
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
            login.trim()
            name.trim()
            password.trim()
            confirmPassword.trim()
            val loginEmpty = login.isEmpty()
            val nameEmpty = name.isEmpty()
            val passwordsMatch = password == confirmPassword
            val passwordEmpty = password.isEmpty()
            val confirmPasswordEmpty = confirmPassword.isEmpty()

            binding.apply {
                loginLayout.error = if (loginEmpty) getString(R.string.empty_login) else null
                nameLayout.error = if (nameEmpty) getString(R.string.name_is_empty) else null

                passLayout.error = if (!passwordsMatch || passwordEmpty) {
                    if (passwordEmpty) getString(R.string.passwords_is_empty) else getString(R.string.passwords_dont_match)
                } else null

                repeatPassLayout.error = if (!passwordsMatch || confirmPasswordEmpty) {
                    if (confirmPasswordEmpty) getString(R.string.passwords_is_empty) else getString(
                        R.string.passwords_dont_match
                    )
                } else null
            }


            if (loginEmpty || nameEmpty || !passwordsMatch || passwordEmpty || confirmPasswordEmpty) {
                return@setOnClickListener
            }

            authViewModel.register(login, name, password)
        }



        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }

    private fun updateButtonState(): Boolean {
        return login.isNotEmpty() && name.isNotEmpty()
                && password.isNotEmpty() && confirmPassword.isNotEmpty()
    }

}