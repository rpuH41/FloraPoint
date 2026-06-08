package com.liulkovich.florapoint.presentation.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.liulkovich.florapoint.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor() {

    private val auth = FirebaseAuth.getInstance()

    fun isAuthorized(): Boolean {
        return auth.currentUser != null &&
                auth.currentUser?.isAnonymous == false
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun getCurrentUserName(): String? {
        return auth.currentUser?.displayName
    }

    fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }

    fun isAnonymous(): Boolean {
        return auth.currentUser?.isAnonymous == true
    }

    fun isRegistered(): Boolean {
        return auth.currentUser != null &&
                auth.currentUser?.isAnonymous == false
    }
    fun deleteAccount(onSuccess: () -> Unit, onError: (String) -> Unit) {
        auth.currentUser?.delete()
            ?.addOnSuccessListener { onSuccess() }
            ?.addOnFailureListener { onError(it.message ?: "Error") }
    }
    fun signInAnonymously(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        if (auth.currentUser != null) {
            onSuccess()
            return
        }

        auth.signInAnonymously()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onError(it.message ?: "Auth error")
            }
    }

    fun logout() {
        auth.signOut()
    }
    fun getGoogleSignInIntent(context: Context): Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))  // ← важно!
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(context, gso).signInIntent
    }

    fun handleGoogleSignInResult(
        intent: Intent?,
        onSuccess: (anonymousUid: String?) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            val account = task.result
            val idToken = account.idToken

            if (idToken != null) {
                val anonymousUid = if (auth.currentUser?.isAnonymous == true) {
                    auth.currentUser?.uid
                } else null

                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential)
                    .addOnSuccessListener { onSuccess(anonymousUid) }
                    .addOnFailureListener { onError(it.message ?: "Firebase auth failed") }
            } else {
                onError("ID Token is null")
            }
        } catch (e: Exception) {
            onError(e.message ?: "Google Sign-In failed")
        }
    }

    fun firebaseAuthWithGoogle(
        idToken: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onError(it.message ?: "Google auth failed")
            }
    }
}