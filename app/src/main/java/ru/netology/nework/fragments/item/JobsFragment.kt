package ru.netology.nework.fragments.item

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.CardJobBinding
import ru.netology.nework.databinding.FragmentJobsBinding
import ru.netology.nework.util.AppConst
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.JobViewModel
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class JobsFragment : Fragment() {

    private val jobViewModel: JobViewModel by viewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    private var _binding: FragmentJobsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJobsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = arguments?.getLong(AppConst.USER_ID)
        jobViewModel.getJobs(userId)

        val isOwner = userId == authViewModel.dataAuth.value?.id
        binding.buttonNewJob.isVisible = isOwner

        setupObservers(userId, isOwner)
        setupListeners()
    }

    private fun setupObservers(userId: Long?, isOwner: Boolean) {
        jobViewModel.data.observe(viewLifecycleOwner) { jobs ->
            binding.containerJob.removeAllViews()

            if (jobs.isEmpty()) {
                binding.emptyState.isVisible = true
                return@observe
            }

            binding.emptyState.isVisible = false

            jobs.forEach { job ->
                CardJobBinding.inflate(layoutInflater, binding.containerJob, true).apply {
                    name.text = job.name
                    position.text = job.position

                    // Безопасная обработка nullable finish
                    startFinish.text = buildString {
                        append(job.start.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                        append(" - ")
                        append(
                            job.finish?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                                ?: getString(R.string.present_time)
                        )
                    }

                    // Отображение ссылки если есть
                    link.isVisible = !job.link.isNullOrBlank()
                    link.text = job.link

                    buttonRemoveJob.isVisible = isOwner
                    buttonRemoveJob.setOnClickListener {
                        jobViewModel.deleteJob(job.id)
                    }
                }
            }
        }

        jobViewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is JobViewModel.JobState.Loading -> {
                    binding.progressBar.isVisible = true
                }

                is JobViewModel.JobState.Success -> {
                    binding.progressBar.isVisible = false
                }

                is JobViewModel.JobState.Error -> {
                    binding.progressBar.isVisible = false
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                    jobViewModel.resetState()
                }

                is JobViewModel.JobState.Idle -> {
                    binding.progressBar.isVisible = false
                }
            }
        }
    }

    private fun setupListeners() {
        binding.buttonNewJob.setOnClickListener {
            findNavController().navigate(R.id.action_detailUserFragment_to_newJobFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = JobsFragment()
    }
}