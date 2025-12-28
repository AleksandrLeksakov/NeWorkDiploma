package ru.netology.nework.repository.interfaces

import androidx.lifecycle.LiveData
import ru.netology.nework.dto.Job

interface JobRepository {

    val dataJob: LiveData<List<Job>>

    suspend fun getMyJobs()

    suspend fun getJobs(userId: Long)

    suspend fun saveJob(job: Job)

    suspend fun deleteJob(id: Long)
}