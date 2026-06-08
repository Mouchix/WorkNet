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

    suspend fun getJobsByPlace(placeId: String): List<Job> {
        val snapshot = jobsCollection(placeId).get().await()
        return snapshot.toObjects(Job::class.java)
    }

    // ---------------------------------------------------------
    // ELIMINAZIONE JOB
    // ---------------------------------------------------------
    suspend fun deleteJob(placeId: String, jobId: String) {
        jobsCollection(placeId).document(jobId).delete().await()
    }

    suspend fun deleteJobsByPlace(placeId: String) {
        try {
            val snapshot = jobsCollection(placeId).get().await()
            for (doc in snapshot.documents) {
                val jobId = doc.id
                deleteJob(placeId, jobId)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
