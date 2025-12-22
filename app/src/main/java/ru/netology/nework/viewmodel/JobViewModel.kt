package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nework.dto.Job
import ru.netology.nework.repository.Repository
import java.time.OffsetDateTime
import javax.inject.Inject

@HiltViewModel
class JobViewModel @Inject constructor(
    private val repository: Repository,
) : ViewModel() {
    val data: LiveData<List<Job>> = repository.dataJob


    fun getJobs(userId: Long?) = viewModelScope.launch {
        if (userId == null) {
            repository.getMyJobs()
        } else {
            repository.getJobs(userId)
        }
    }

    fun saveJob(
        name: String,
        position: String,
        link: String?,
        startWork: OffsetDateTime,
        finishWork: OffsetDateTime
    ) = viewModelScope.launch {
        repository.saveJob(
            Job(
                id = 0,
                name = name,
                position = position,
                link = link,
                start = startWork,
                finish = finishWork,
            )
        )
    }

    fun deleteJob(id: Long) = viewModelScope.launch {
        repository.deleteJob(id)
    }


}