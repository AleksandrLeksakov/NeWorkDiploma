package ru.netology.nework.fragments.newitem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentNewJobBinding
import ru.netology.nework.viewmodel.JobViewModel
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@AndroidEntryPoint
class NewJobFragment : Fragment() {

    private val jobViewModel: JobViewModel by activityViewModels()

    private var _binding: FragmentNewJobBinding? = null
    private val binding get() = _binding!!

    private val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.getDefault())

    private var name = ""
    private var position = ""
    private var link = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewJobBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
    }

    private fun setupListeners() {
        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.nameTextField.addTextChangedListener {
            name = it.toString()
            binding.apply {
                nameLayout.error = null
                buttonJobCreate.isChecked = isFormValid()
            }
        }

        binding.positionTextField.addTextChangedListener {
            position = it.toString()
            binding.apply {
                positionLayout.error = null
                buttonJobCreate.isChecked = isFormValid()
            }
        }

        binding.linkTextField.addTextChangedListener {
            link = it.toString()
        }

        binding.buttonJobCreate.setOnClickListener {
            if (!validateForm()) {
                return@setOnClickListener
            }

            val startWork = binding.startWork.text.toString()
            val finishWork = binding.finishWork.text.toString()

            try {
                val dateStart = parseDate(startWork)

                // finish может быть null если "по настоящее время"
                val dateFinish: OffsetDateTime? = if (
                    finishWork.isEmpty() ||
                    finishWork == getString(R.string.present_time)
                ) {
                    null
                } else {
                    parseDate(finishWork)
                }

                jobViewModel.saveJob(
                    name = name.trim(),
                    position = position.trim(),
                    link = link.trim().ifEmpty { null },
                    startWork = dateStart,
                    finishWork = dateFinish  // Теперь передаём null, а не emptyOffsetDateTime
                )

                findNavController().navigateUp()

            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.invalid_date_format),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.datePicker.setOnClickListener {
            showDatePickerDialog()
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        name = name.trim()
        position = position.trim()
        link = link.trim()

        if (name.isEmpty()) {
            binding.nameLayout.error = getString(R.string.empty_field)
            isValid = false
        }

        if (position.isEmpty()) {
            binding.positionLayout.error = getString(R.string.empty_field)
            isValid = false
        }

        val startWork = binding.startWork.text.toString()
        if (startWork.isEmpty()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.select_start_date),
                Toast.LENGTH_SHORT
            ).show()
            isValid = false
        }

        return isValid
    }

    private fun parseDate(dateString: String): OffsetDateTime {
        return LocalDate.parse(dateString, formatter)
            .atTime(0, 0)
            .atOffset(ZoneOffset.UTC)
    }

    private fun showDatePickerDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_date_picker, null)
        val dateStart = dialogView.findViewById<TextInputEditText>(R.id.dateStart)
        val dateFinish = dialogView.findViewById<TextInputEditText>(R.id.dateFinish)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.select_dates)
            .setView(dialogView)
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                val start = dateStart.text.toString().trim()
                val finish = dateFinish.text.toString().trim()

                binding.startWork.text = start
                binding.finishWork.text = finish.ifEmpty {
                    getString(R.string.present_time)
                }
                binding.buttonJobCreate.isChecked = isFormValid()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun isFormValid(): Boolean {
        return name.isNotEmpty() && position.isNotEmpty()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}