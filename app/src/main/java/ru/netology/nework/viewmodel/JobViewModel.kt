package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

    private val _state = MutableLiveData<JobState>(JobState.Idle)
    val state: LiveData<JobState> = _state

    fun getJobs(userId: Long?) {
        viewModelScope.launch {
            _state.value = JobState.Loading
            try {
                if (userId == null) {
                    repository.getMyJobs()
                } else {
                    repository.getJobs(userId)
                }
                _state.value = JobState.Success
            } catch (e: Exception) {
                _state.value = JobState.Error(e.message ?: "Ошибка загрузки вакансий")
            }
        }
    }

    fun saveJob(
        name: String,
        position: String,
        link: String?,
        startWork: OffsetDateTime,
        finishWork: OffsetDateTime?  // Теперь nullable
    ) {
        viewModelScope.launch {
            _state.value = JobState.Loading
            try {
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
                _state.value = JobState.Success
            } catch (e: Exception) {
                _state.value = JobState.Error(e.message ?: "Ошибка сохранения")
            }
        }
    }

    fun deleteJob(id: Long) {
        viewModelScope.launch {
            _state.value = JobState.Loading
            try {
                repository.deleteJob(id)
                _state.value = JobState.Success
            } catch (e: Exception) {
                _state.value = JobState.Error(e.message ?: "Ошибка удаления")
            }
        }
    }

    fun resetState() {
        _state.value = JobState.Idle
    }

    sealed class JobState {
        object Idle : JobState()
        object Loading : JobState()
        object Success : JobState()
        data class Error(val message: String) : JobState()
    }
}