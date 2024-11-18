import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.pamoj.data.model.Post
import com.example.pamoj.data.repository.AuthRepository
import com.example.pamoj.data.repository.FirestoreRepository
import com.example.pamoj.ui.component.PostItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    posts: List<Post>,
    authRepository: AuthRepository,
    firestoreRepository: FirestoreRepository,
    navController: NavController,
    onPostsUpdated: () -> Unit = {}
) {
    val user = authRepository.getCurrentUser()
    val userName = user?.displayName ?: user?.email ?: "Pengguna"
    val scope = rememberCoroutineScope()
    var isUploading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                isUploading = true
                try {
                    when (val result = firestoreRepository.addPostWithPhoto(
                        Post(createdTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            .format(Date())),
                        it,
                        context
                    )) {
                        is FirestoreRepository.Result.Success -> {
                            Toast.makeText(context, "Post berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                            onPostsUpdated()
                        }
                        is FirestoreRepository.Result.Error -> {
                            Toast.makeText(
                                context,
                                "Error: ${result.exception.localizedMessage}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } finally {
                    isUploading = false
                }
            }
        }
    }

    if (isUploading) {
        CircularProgressIndicator()
    }

    Scaffold(
        modifier = Modifier.background(color = Color(0xFFF5F5F5)),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { launcher.launch("image/*") },

                containerColor = Color(0xFF6200EE),
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Item")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Selamat pagi,",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "${userName}!",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold)
                    )
                }

                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { navController.navigate("profile") },
                    color = Color.Gray
                ) {
                    if (user?.photoUrl != null) {
                        Image(
                            painter = rememberAsyncImagePainter(user.photoUrl),
                            contentDescription = "Profile picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Default Profile",
                            tint = Color.White,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }


            Spacer(modifier = Modifier.height(16.dp))
            // penerapan recycle view memakai jetpack compose ada di bawah ini
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(posts) { post ->
                    PostItem(post)
                }
            }
        }
    }
}


