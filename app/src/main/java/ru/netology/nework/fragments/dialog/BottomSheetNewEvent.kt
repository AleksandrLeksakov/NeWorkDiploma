package ru.netology.nework.fragment.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import ru.netology.nework.R
import ru.netology.nework.databinding.BottomSheetNewEventBinding
import ru.netology.nework.dto.EventType
import ru.netology.nework.viewmodel.EventViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

class BottomSheetNewEvent : BottomSheetDialogFragment() {
    private val eventViewModel: EventViewModel by activityViewModels()
    private val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm", Locale.getDefault())
    private var date = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = BottomSheetNewEventBinding.inflate(inflater, container, false)

        val datePicker = MaterialDatePicker.Builder
            .datePicker()
            .setTitleText(getString(R.string.select_date))
            .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
            .setSelection(System.currentTimeMillis())
            .build()

        binding.textField.setEndIconOnClickListener {
            datePicker.show(childFragmentManager, "tag")
        }

        datePicker.addOnPositiveButtonClickListener {
            val instant = Instant.ofEpochMilli(it)
            val dateString = instant.atOffset(ZoneOffset.UTC).format(formatter)
            binding.dateTextField.setText(dateString)
        }

        binding.dateTextField.addTextChangedListener {
            date = it.toString()
        }

        binding.radioButtonOnline.isChecked = true
        binding.radioGroup.setOnCheckedChangeListener { _, id ->
            when (id) {
                (R.id.radioButtonOnline) -> {
                    eventViewModel.setEventType(EventType.ONLINE)
                }

                else -> {
                    eventViewModel.setEventType(EventType.OFFLINE)
                }
            }
        }

        return binding.root
    }

    override fun onDestroy() {
        if (date.isNotEmpty()) {
            try {
                val odt = LocalDateTime.parse(date, formatter).atZone(ZoneId.systemDefault())
                    .toOffsetDateTime()
                eventViewModel.setDateTime(odt)
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.invalid_date_format),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        super.onDestroy()
    }

    companion object {
        const val TAG = "ModalBottomSheet"
    }

}