package com.example.worknet.data.repository

import com.example.worknet.data.model.Job
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class JobRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun jobsCollection(placeId: String) =
        db.collection("places").document(placeId).collection("jobs")

    // ---------------------------------------------------------
    // CREAZIONE JOB
    // ---------------------------------------------------------
    suspend fun createJob(job: Job) {
        jobsCollection(job.placeId).document(job.id).set(job).await()
    }

    // ---------------------------------------------------------
    // LETTURA JOB
    // ---------------------------------------------------------
    suspend fun getJobById(placeId: String, jobId: String): Job? {
        val snapshot = jobsCollection(placeId).document(jobId).get().await()
        return snapshot.toObject(Job::class.java)
    }

    suspend fun getJobsByPlace(placeId: String): List<Job> {
        val snapshot = jobsCollection(placeId).get().await()
        return snapshot.toObjects(Job::class.java)
    }

    // ---------------------------------------------------------
    // AGGIORNAMENTO JOB
    // ---------------------------------------------------------
    suspend fun updateJob(job: Job) {
        jobsCollection(job.placeId).document(job.id).set(job).await()
    }

    suspend fun updateTitle(placeId: String, jobId: String, title: String) {
        jobsCollection(placeId).document(jobId).update("title", title).await()
    }

    suspend fun updateDescription(placeId: String, jobId: String, description: String) {
        jobsCollection(placeId).document(jobId).update("description", description).await()
    }

    suspend fun updateSalary(placeId: String, jobId: String, salary: String?) {
        jobsCollection(placeId).document(jobId).update("salary", salary).await()
    }

    suspend fun updateContractType(placeId: String, jobId: String, contractType: String?) {
        jobsCollection(placeId).document(jobId).update("contractType", contractType).await()
    }

    // ---------------------------------------------------------
    // ELIMINAZIONE JOB
    // ---------------------------------------------------------
    suspend fun deleteJob(placeId: String, jobId: String) {
        jobsCollection(placeId).document(jobId).delete().await()
    }
}
