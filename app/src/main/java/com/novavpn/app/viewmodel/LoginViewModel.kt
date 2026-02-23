package com.novavpn.app.viewmodel

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.novavpn.app.R
import com.novavpn.app.security.SecureStorage
import com.novavpn.app.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import javax.inject.Inject

sealed class LoginState {
    data object Idle : LoginState()
    data object Loading : LoginState()
    data class Success(val email: String) : LoginState()
    data class Error(val message: String) : LoginState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val secureStorage: SecureStorage
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun signInWithGoogle(activityContext: Context) {
        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            try {
                val webClientId = context.getString(R.string.google_web_client_id)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val credentialManager = CredentialManager.create(activityContext)
                val result = credentialManager.getCredential(
                    request = request,
                    context = activityContext
                )

                val googleCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
                val email = googleCredential.id
                val idToken = googleCredential.idToken

                secureStorage.saveGoogleAuth(email, idToken)
                Logger.i("LoginViewModel: signed in as $email")
                _loginState.value = LoginState.Success(email)

            } catch (e: GetCredentialCancellationException) {
                Logger.d("LoginViewModel: sign-in cancelled by user")
                _loginState.value = LoginState.Idle
            } catch (e: NoCredentialException) {
                Logger.w("LoginViewModel: no Google accounts available on device")
                _loginState.value = LoginState.Error("No Google account found on this device. Please add a Google account in Settings.")
            } catch (e: Exception) {
                Logger.e("LoginViewModel: sign-in failed", e)
                _loginState.value = LoginState.Error(e.localizedMessage ?: "Sign-in failed. Please try again.")
            }
        }
    }

    fun signOut() {
        secureStorage.clearGoogleAuth()
        _loginState.value = LoginState.Idle
    }

    fun resetError() {
        if (_loginState.value is LoginState.Error) {
            _loginState.value = LoginState.Idle
        }
    }
}
