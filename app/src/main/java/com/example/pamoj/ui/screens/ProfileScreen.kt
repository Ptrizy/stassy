package com.example.pamoj.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.pamoj.R
import com.example.pamoj.data.repository.AuthRepository
import com.example.pamoj.data.repository.FirestoreRepository
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    authRepository: AuthRepository,
    firestoreRepository: FirestoreRepository
) {
    var username by remember { mutableStateOf(authRepository.getCurrentUser()?.displayName ?: "") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val user = authRepository.getCurrentUser()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    val storageRef = FirebaseStorage.getInstance().reference
                    val photoRef = storageRef.child("profile_photos/${user?.uid}/profile.jpg")

                    try {
                        val uploadTask = photoRef.putFile(uri)
                        uploadTask.await()

                        val downloadUrl = photoRef.downloadUrl.await()

                        val profileUpdates = userProfileChangeRequest {
                            photoUri = downloadUrl
                        }
                        user?.updateProfile(profileUpdates)?.await()
                        Toast.makeText(context, "Foto profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Gagal memperbarui foto profil: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Gagal mengupload foto: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.background(color = Color(0xFFF5F5F5)),
        topBar = {
            TopAppBar(
                title = {  Text(
                    "Ubah Profil",
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.Black)
                ) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .clickable { launcher.launch("image/*") },
                color = Color.Gray
            ) {
                if (user?.photoUrl != null) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = user.photoUrl,
                            error = rememberVectorPainter(Icons.Default.Person)
                        ),
                        contentDescription = "Profile picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Default Profile",
                        tint = Color.White,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = {  Text(
                    "Username Baru",
                    style = MaterialTheme.typography.bodyMedium
                ) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = {  Text(
                    "Password Baru",
                    style = MaterialTheme.typography.bodyMedium
                ) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            painter = rememberVectorPainter(
                                image = if (passwordVisible) ImageVector.vectorResource(R.drawable.eye_line_slash) else ImageVector.vectorResource(
                                    R.drawable.eye_line)
                            ),
                            contentDescription = if (passwordVisible) "Sembunyikan kata sandi" else "Tampilkan kata sandi",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = {  Text(
                    "Konfirmasi Password Baru",
                    style = MaterialTheme.typography.bodyMedium
                ) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible}) {
                        Icon(
                            painter = rememberVectorPainter(
                                image = if (confirmPasswordVisible) ImageVector.vectorResource(R.drawable.eye_line_slash) else ImageVector.vectorResource(
                                    R.drawable.eye_line)
                            ),
                            contentDescription = if (confirmPasswordVisible) "Sembunyikan kata sandi" else "Tampilkan kata sandi",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.launch {
                        try {
                            var isSuccess = true

                            if (username.isNotEmpty() && username != user?.displayName) {
                                try {
                                    val profileUpdates = userProfileChangeRequest {
                                        displayName = username
                                    }
                                    user?.updateProfile(profileUpdates)?.await()
                                } catch (e: Exception) {
                                    isSuccess = false
                                    Toast.makeText(context, "Gagal mengupdate username: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }

                            if (newPassword.isNotEmpty() && newPassword == confirmPassword) {
                                try {
                                    user?.updatePassword(newPassword)?.await()
                                } catch (e: Exception) {
                                    isSuccess = false
                                    Toast.makeText(context, "Gagal mengupdate password: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            } else if (newPassword.isNotEmpty() && newPassword != confirmPassword) {
                                isSuccess = false
                                Toast.makeText(context, "Password tidak cocok", Toast.LENGTH_SHORT).show()
                            }

                            if (isSuccess) {
                                Toast.makeText(context, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                                navController.navigateUp()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE),
                    contentColor = Color(0xFFFFFFFF),
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                )
            ) {
                Text(
                    text = "Simpan Perubahan",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}