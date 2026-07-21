package com.liulkovich.florapoint.domain.cloud

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.liulkovich.florapoint.domain.UserPoints
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import com.google.firebase.firestore.ListenerRegistration

@Singleton
class FirestoreRepository @Inject constructor() {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun uploadPoint(point: UserPoints): Result<String> {

        return try {

            val doc =
                firestore.collection("public_points").document()

            val cloudPoint = CloudPoint(
                cloudId = doc.id,
                ownerUid = point.ownerUid ?: "",

                speciesId = point.speciesId,
                userName = point.userName,
                description = point.description,

                latitude = point.latitude,
                longitude = point.longitude,

                category = point.category ?: "custom",

                timestamp = point.timestamp,

                temperature = point.temperature,
                humidity = point.humidity,
                avgTemp5Days = point.avgTemp5Days,
                avgHumidity5Days = point.avgHumidity5Days
            )

            doc.set(cloudPoint).await()

            Result.success(doc.id)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    suspend fun updatePoint(
        point: UserPoints
    ): Result<Unit> {

        return try {

            val cloudId =
                point.cloudId
                    ?: return Result.failure(
                        IllegalArgumentException("cloudId is null")
                    )

            val cloudPoint = CloudPoint(
                cloudId = cloudId,
                ownerUid = point.ownerUid ?: "",

                speciesId = point.speciesId,
                userName = point.userName,
                description = point.description,

                latitude = point.latitude,
                longitude = point.longitude,

                category = point.category ?: "custom",

                timestamp = point.timestamp,

                temperature = point.temperature,
                humidity = point.humidity,
                avgTemp5Days = point.avgTemp5Days,
                avgHumidity5Days = point.avgHumidity5Days
            )

            firestore.collection("public_points")
                .document(cloudId)
                .set(cloudPoint)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    suspend fun deletePoint(
        cloudId: String
    ): Result<Unit> {

        return try {

            firestore.collection("public_points")
                .document(cloudId)
                .delete()
                .await()

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    suspend fun downloadPublicPoints(): List<UserPoints> {

        return try {

            firestore.collection("public_points")
                .get()
                .await()
                .documents
                .mapNotNull { doc ->

                    doc.toObject<CloudPoint>()
                        ?.copy(cloudId = doc.id)
                        ?.toUserPoint(isPublic = true)
                }

        } catch (e: Exception) {

            emptyList()
        }
    }

    suspend fun downloadUserPoints(uid: String): List<UserPoints> {
        return try {
            firestore.collection("public_points")
                .whereEqualTo("ownerUid", uid)
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    doc.toObject<CloudPoint>()
                        ?.copy(cloudId = doc.id)
                        ?.toUserPoint(isPublic = true)
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun uploadPrivatePoint(point: UserPoints): Result<String> {
        return try {
            val uid = point.ownerUid ?: return Result.failure(IllegalArgumentException("uid is null"))
            val doc = firestore.collection("users").document(uid)
                .collection("private_points").document()

            val cloudPoint = CloudPoint(
                cloudId = doc.id,
                ownerUid = uid,
                speciesId = point.speciesId,
                userName = point.userName,
                description = point.description,
                latitude = point.latitude,
                longitude = point.longitude,
                category = point.category ?: "custom",
                timestamp = point.timestamp,
                temperature = point.temperature,
                humidity = point.humidity,
                avgTemp5Days = point.avgTemp5Days,
                avgHumidity5Days = point.avgHumidity5Days
            )
            doc.set(cloudPoint).await()
            Result.success(doc.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadPrivatePoints(uid: String): List<UserPoints> {
        return try {
            firestore.collection("users").document(uid)
                .collection("private_points")
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    doc.toObject<CloudPoint>()
                        ?.copy(cloudId = doc.id)
                        ?.toUserPoint(isPublic = false)
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun deleteAllUserPoints(uid: String): Result<Unit> {
        return try {
            val batch = firestore.batch()
            firestore.collection("public_points")
                .whereEqualTo("ownerUid", uid)
                .get().await()
                .documents
                .forEach { batch.delete(it.reference) }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAllPrivatePoints(uid: String): Result<Unit> {
        return try {
            val batch = firestore.batch()
            firestore.collection("users").document(uid)
                .collection("private_points")
                .get().await()
                .documents
                .forEach { batch.delete(it.reference) }
            batch.delete(firestore.collection("users").document(uid))
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observePublicPoints(
        onUpdate: (List<UserPoints>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return firestore.collection("public_points")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val points = snapshot?.documents
                    ?.mapNotNull { doc ->
                        doc.toObject<CloudPoint>()
                            ?.copy(cloudId = doc.id)
                            ?.toUserPoint(isPublic = true)
                    } ?: emptyList()
                onUpdate(points)
            }
    }

    suspend fun deletePrivatePoint(uid: String, cloudId: String): Result<Unit> {
        return try {
            firestore.collection("users").document(uid)
                .collection("private_points")
                .document(cloudId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}