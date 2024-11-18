package com.example.pamoj.data.repository

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.example.pamoj.data.model.Post
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class FirestoreRepository(
    private val storage: FirebaseStorage,
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) {
    sealed class Result<out T> {
        data class Success<T>(val data: T) : Result<T>()
        data class Error(val exception: Exception) : Result<Nothing>()
    }


    private suspend fun uploadImage(uri: Uri, context: Context): String {
        return try {
            val timestamp = System.currentTimeMillis()
            val extension = MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(context.contentResolver.getType(uri))
                ?: "jpg"
            val fileName = "posts/$timestamp.$extension"

            val storageRef = storage.reference.child(fileName)

            context.contentResolver.openInputStream(uri)?.use { inputStream ->

                val metadata = StorageMetadata.Builder()
                    .setContentType(context.contentResolver.getType(uri))
                    .build()

                val uploadTask = storageRef.putStream(inputStream, metadata)

                uploadTask.await()

                return storageRef.downloadUrl.await().toString()
            } ?: throw IOException("Failed to open input stream")
        } catch (e: Exception) {
            throw IOException("Upload failed: ${e.message}", e)
        }
    }

    suspend fun addPostWithPhoto(
        post: Post,
        photoUri: Uri,
        context: Context
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val currentUser = authRepository.getCurrentUser()
                ?: throw SecurityException("User not authenticated")

            val downloadUrl = uploadImage(photoUri, context)

            val postWithPhoto = Post(
                id = UUID.randomUUID().toString(),
                photoUrl = downloadUrl,
                author = currentUser.email ?: throw IllegalStateException("User email not available"),
                createdTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(Date())
            )

            firestore.collection(POSTS_COLLECTION)
                .document(postWithPhoto.id)
                .set(postWithPhoto)
                .await()

            Result.Success(postWithPhoto.id)
        } catch (e: SecurityException) {
            Result.Error(e)
        } catch (e: IOException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getPosts(): Result<List<Post>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection(POSTS_COLLECTION)
                .orderBy("createdTime", Query.Direction.DESCENDING)
                .get()
                .await()

            val posts = snapshot.documents.mapNotNull { document ->
                document.toObject(Post::class.java)?.copy(id = document.id)
            }

            Result.Success(posts)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun deletePost(postId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {

            val currentUser = authRepository.getCurrentUser()
                ?: throw SecurityException("User not authenticated")

            val post = firestore.collection(POSTS_COLLECTION)
                .document(postId)
                .get()
                .await()
                .toObject(Post::class.java)
                ?: throw IllegalStateException("Post not found")

            if (post.author != currentUser.email) {
                throw SecurityException("User not authorized to delete this post")
            }

            if (post.photoUrl.isNotEmpty()) {
                val photoRef = storage.getReferenceFromUrl(post.photoUrl)
                photoRef.delete().await()
            }

            firestore.collection(POSTS_COLLECTION)
                .document(postId)
                .delete()
                .await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    companion object {
        private const val POSTS_COLLECTION = "posts"
    }
}